package com.baptechs.zeus.module.fccmanage.impl;

import com.alibaba.fastjson.JSONObject;
import com.baptechs.logistics.guardian.util.NamedThreadFactory;
import com.baptechs.zeus.domain.common.redis.RedisKeyHelper;
import com.baptechs.zeus.domain.station.hardware.Device;
import com.baptechs.zeus.domain.station.hardware.Dispenser;
import com.baptechs.zeus.domain.station.hardware.FuelTank;
import com.baptechs.zeus.module.fccmanage.IFSFModuleService;
import com.baptechs.zeus.module.fccmanage.ifsf.DeviceDataBase;
import com.baptechs.zeus.module.fccmanage.ifsf.FPDataId;
import com.baptechs.zeus.module.fccmanage.ifsf.MessageCodeType;
import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import com.baptechs.zeus.module.fccmanage.message.HeartbeatMessage;
import com.baptechs.zeus.module.fccmanage.ifsf.SubnetType;
import com.baptechs.zeus.module.fccmanage.netty.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Created by warden on 2016/3/22.
 */

@Service("ifsfModuleService")
public class DefaultIFSFModuleService implements IFSFModuleService,InitializingBean{
    Logger                      logger                  = LoggerFactory.getLogger(DefaultIFSFModuleService.class);

    //本地设备里列表，key为subnet+nodeId
    ConcurrentHashMap<String,Device>
                                deviceMap               =new ConcurrentHashMap<>();
    ConcurrentHashMap<String,Bootstrap>
                                connectionMap           =new ConcurrentHashMap<>();
    ThreadPoolExecutor          poolExecutor            =new ThreadPoolExecutor(4,10,10,TimeUnit.SECONDS,new SynchronousQueue<Runnable>());



    ScheduledExecutorService    heartbeatThread         =Executors.newSingleThreadScheduledExecutor();
    ExecutorService             heartbeatReceiver       =Executors.newSingleThreadExecutor();

    @Setter
    int                         heartBeatFrequency      =1000*10;
    @Setter
    String                      localIpFilter           ="192.168.1.";


    @Autowired
    NettyServerConfig           nettyServerConfig;

    @Override
    public void afterPropertiesSet() throws Exception {

        //udp心跳发送
        final String localIp=RemotingUtil.getLocalAddress(localIpFilter);
        String[] localIpSplit=localIp.split("\\.");
        final String broadcastIp=localIpSplit[0]+"."+localIpSplit[1]+"."+localIpSplit[2]+".255";
        final HeartbeatMessage heartbeatNeedSent=new HeartbeatMessage(localIp,nettyServerConfig.getListenPort(), SubnetType.CONTROLLER_DEVICE,1);
        final DatagramSocket sender = new DatagramSocket();// 创建用来发送数据报包的套接字
        heartbeatThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramPacket dp = new DatagramPacket(heartbeatNeedSent.encode().array(),10,
                            InetAddress.getByName(broadcastIp), nettyServerConfig.getBroadcastPort());
                    sender.send(dp);
                    logger.debug("heartbeat send:" + JSONObject.toJSONString(heartbeatNeedSent));
                }catch (Exception ex){
                    logger.error("heartBeat send err",ex);
                }
            }
        },heartBeatFrequency,heartBeatFrequency, TimeUnit.MILLISECONDS);

        //udp心跳接收
        final DatagramSocket receiver=new DatagramSocket(nettyServerConfig.getBroadcastPort());
        byte[] recvBuf=new byte[10];
        final DatagramPacket receivePacket=new DatagramPacket(recvBuf,recvBuf.length);
        heartbeatReceiver.submit(new Runnable() {
            @Override
            public void run() {
                while (true){
                   try {
                       receiver.receive(receivePacket);
                       HeartbeatMessage message=HeartbeatMessage.decode(receivePacket.getData());
                       logger.debug("receive heartbeat:"+JSONObject.toJSONString(message));
                       onReceiveHeartbeat(message);
                   }catch (Exception ex){
                       logger.error("udp heartbeat receive err");
                   }
                }
            }
        });


        //server端tcp连接
        try {
            ServerBootstrap bootstrap=new ServerBootstrap();
            EventLoopGroup bossGroup=new NioEventLoopGroup(nettyServerConfig.getServerSelectorThreads(),new NamedThreadFactory("ifsf-boss"));
            EventLoopGroup workerGroup=new NioEventLoopGroup(nettyServerConfig.getServerWorkerThreads(),new NamedThreadFactory("ifsf-child"));
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 65536);
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.localAddress(new InetSocketAddress(nettyServerConfig.getListenPort()));
            final IFSFModuleService me=this;
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new DefaultEventExecutorGroup(nettyServerConfig.getServerWorkerThreads(), new NamedThreadFactory("ifsf-worker-")),
                            new ApplicationMsgEncoder(),
                            new ApplicationMsgDecoder(),
                            new IdleStateHandler(nettyServerConfig.getReaderIdleTimeSeconds(),
                                    nettyServerConfig.getWriterIdleTimeSeconds(), nettyServerConfig.getServerChannelMaxIdleTimeSeconds()),
                            new NettyConnectManageHandler(),
                            new NettyServerHandler(me)
                    );
                }
            });
            bootstrap.bind().sync();
        }catch (Exception ex){
            logger.error("IFSF MODULE INIT ERROR! EXIT",ex);
            System.exit(0);
        }
    }

    @Override
    public void onReceiveHeartbeat(final HeartbeatMessage message){
        String key=message.getDeviceKey();
        if(!deviceMap.containsKey(key)){
            synchronized (this){
                if(!deviceMap.containsKey(key)){
                    switch (message.getSubnet()){
                        case SubnetType.Dispenser:
                            final Dispenser dispenser=new Dispenser();
                            dispenser.setLnao(message.getLnao());
                            dispenser.setLastHeartBeat(new Date());
                            deviceMap.put(key, dispenser);
                            poolExecutor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    initDispenser(dispenser,message,null);
                                }
                            });
                        break;
                        case SubnetType.TANK_LEVEL_GAUGE:
                            FuelTank fuelTank=new FuelTank();
                            fuelTank.setLnao(message.getLnao());
                            fuelTank.setLastHeartBeat(new Date());
                            deviceMap.put(key,fuelTank);
                        break;
                    }
                }
            }
        }
        Device device=deviceMap.get(key);
        if(device==null){
            return;
        }
        device.setLastHeartBeat(new Date());
    }

    /**
     * 收到应用消息
     * @param channelHandlerContext
     * @param message
     */
    @Override
    public void onApplicationMessage(ChannelHandlerContext channelHandlerContext, ApplicationMessage message) {
        if(message==null){
            return;
        }
        switch (message.getMessageType()){
            case MessageCodeType.MESSAGE_CODE_APPLICATION_MSG:
                break;
            case MessageCodeType.MESSAGE_CODE_COMMUNICATION_MSG:
                break;
        }
    }

    private void processApplicationMessage(ChannelHandlerContext channelHandlerContext, ApplicationMessage message){
        if(message.getDbAdLength()==1){
            switch (message.getDbAd()[0]){
                case DeviceDataBase.FUELLING_POINT_DATABASE_1:
                case DeviceDataBase.FUELLING_POINT_DATABASE_2:
                case DeviceDataBase.FUELLING_POINT_DATABASE_3:
                case DeviceDataBase.FUELLING_POINT_DATABASE_4:
                    for(byte dataId:message.getDataMap().keySet()){
                        byte[] dataValue=message.getDataMap().get(dataId);
                        if(dataValue==null||dataValue.length==0){
                            return;
                        }
                        switch (dataId){
                            case FPDataId.FP_State:
                                Device device=deviceMap.get(getDeviceKeyByLogicalNode(message.getOriginatorSubnet(),message.getOriginatorNodeId()));
                                if(device==null){
                                    logger.warn("cant update dispenser status as no device info");
                                    return;
                                }
                                Dispenser dispenser=(Dispenser)device;
                                break;
                            case FPDataId.Log_Noz_State:
                                break;
                            default:
                                break;
                        }
                    }
                    break ;
            }
        }
    }

    private void initDispenser(Dispenser dispenser,HeartbeatMessage heartbeatMessage,ApplicationMessage applicationMessage){

        //0.建立连接
        //1.设置心跳间隔
        //2.设置加油模式数量
        //3.读取FP(加油点)数量
        //4.读取所有FP的状态
        //5.读取脱机交易

        switch (dispenser.getInitStatus()){
            case Dispenser.INIT_STATUS_NONE:
                if(ConnectionHolder.putConnect(dispenser.getIp(),dispenser.getPort())){
                    dispenser.setInitStatus(Dispenser.INIT_STATION_CONNECT);
                }else {
                    logger.error("establish connection to dispenser false,message:"+JSONObject.toJSONString(heartbeatMessage));
                    deviceMap.remove(heartbeatMessage.getDeviceKey());
                    return;
                }
                break;
            case Dispenser.INIT_STATUS_CHECK_HEARTBEAT:
                break;
        }


    }

    private String getDeviceKeyByLogicalNode(int subnet,int node){
        return RedisKeyHelper.buildKey(String.valueOf(subnet),String.valueOf(node));
    }
    private String getConnectionKey(String host,int port){
        return RedisKeyHelper.buildKey(host,String.valueOf(port));
    }

    private Bootstrap createConnection(String ip,int port){
        return null;
    }
}
