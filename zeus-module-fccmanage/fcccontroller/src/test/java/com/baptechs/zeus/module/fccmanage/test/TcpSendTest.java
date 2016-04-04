package com.baptechs.zeus.module.fccmanage.test;

import com.alibaba.fastjson.JSONObject;
import com.baptechs.logistics.guardian.util.StringUtil;
import com.baptechs.zeus.module.fccmanage.netty.RemotingHelper;
import com.baptechs.zeus.module.fccmanage.netty.RemotingUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


/**
 * Created by warden on 2016/3/29.
 */
public class TcpSendTest {
    Logger                  logger                      = LoggerFactory.getLogger(TcpSendTest.class);
    String                  host                        ="192.168.1.10";
    int                     port                        =4368;

    Bootstrap               bootstrap;

    ChannelHandlerContext   commonctx;

    public TcpSendTest(){

    }

    @Test
    public void nettyTest(){
        //初始化bootstrap
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class);
        b.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new ByteArrayDecoder());
                pipeline.addLast("frameEncoder", new ByteArrayEncoder());
                pipeline.addLast("decoder", new ByteArrayDecoder());
                pipeline.addLast("encoder", new ByteArrayEncoder());
            }
        });
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, false);
        bootstrap=b;
        Channel channel = null;
        try {
            ChannelFuture future=bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            System.out.println("请输入：（如：02.01.01.03.00.21.00.05.01.00.04.01.0A.）");
            String str=null;
            try {
                str = buffer.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(StringUtil.isEmpty(str)){
                continue;
            }
            str=str.trim();
            String[] list=str.split("\\.");
            byte[] bytes=new byte[list.length];
            for(int i=0;i<list.length;i++){
                int temp=0;
                temp=temp | Byte.parseByte(String.valueOf(list[i].charAt(0)),16);
                temp=temp<<4;
                temp=temp | Byte.parseByte(String.valueOf(list[i].charAt(1)),16);
                bytes[i]=(byte)temp;
            }
            commonctx.writeAndFlush(bytes);
        }
    }

    public void socketTest(){
        try {
            Socket socket=new Socket(host,port);

            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                System.out.println("请输入：（如：02.01.01.03.00.21.00.05.01.00.04.01.0A.）");
                String str=null;
                try {
                    str = buffer.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(StringUtil.isEmpty(str)){
                    continue;
                }
                str=str.trim();
                String[] list=str.split("\\.");
                byte[] bytes=new byte[list.length];
                for(int i=0;i<list.length;i++){
                    int temp=0;
                    temp=temp | Byte.parseByte(String.valueOf(list[i].charAt(0)),16);
                    temp=temp<<4;
                    temp=temp | Byte.parseByte(String.valueOf(list[i].charAt(1)),16);
                    bytes[i]=(byte)temp;
                }
                socket.getOutputStream().write(bytes);
                socket.getOutputStream().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        TcpSendTest test = new TcpSendTest();
        test.socketTest();
        System.out.println(Byte.parseByte("0A",16));
    }
}
