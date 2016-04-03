package com.baptechs.zeus.module.fccmanage.test;

import com.alibaba.fastjson.JSONObject;
import com.baptechs.zeus.module.fccmanage.message.HeartbeatMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by warden on 2016/2/18.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= "classpath:spring-config-test.xml")
public class TEST {

    @Test
    public void heartBeatReceive() throws Exception {


        final DatagramSocket socket=new DatagramSocket(3486);
        byte[] recvBuf=new byte[10];
        DatagramPacket packet=new DatagramPacket(recvBuf,recvBuf.length);

        while (true){
            socket.receive(packet);
            HeartbeatMessage message=HeartbeatMessage.decode(packet.getData());
            System.out.println(new Date().toString()+":"+JSONObject.toJSONString(message));
        }

    }

    @Test
    public void heartBeatSend() throws InterruptedException {
        ScheduledExecutorService sender= Executors.newSingleThreadScheduledExecutor();

        final ByteBuffer buffer=ByteBuffer.allocate(10);
        buffer.putInt(ipToInt("192.168.0.104"));
        buffer.putShort(Short.valueOf("4368"));
        buffer.put(Byte.parseByte("2"));
        buffer.put(Byte.parseByte("1"));
        byte a=1;
        byte b=0;
        buffer.put(a);
        buffer.put(b);
        DatagramPacket heartBeatPacket=new DatagramPacket(buffer.array(),10);
        final StringBuilder stringBuilder=new StringBuilder();
        for(int i=0;i<buffer.array().length;i++){
            stringBuilder.append(" "+buffer.get(i));
        }

        sender.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket ds = new DatagramSocket();// 创建用来发送数据报包的套接字
                    DatagramPacket dp = new DatagramPacket(buffer.array(),
                            10,
                            InetAddress.getByName("192.168.0.255"), 3486);
                    ds.send(dp);
                    System.out.println("sent:" + stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
        Thread.sleep(1000 * 3600);
    }


    @Test
    public void tcpServerTest() throws IOException {
        ServerSocket listen = new ServerSocket(4368);
        Socket server  = listen.accept();
        while (true){
            InputStream inStream = server.getInputStream();
            int length=inStream.available();
            byte[] buffer = new byte[length];
            inStream.read(buffer);
            final StringBuilder stringBuilder=new StringBuilder();
            for(int i=0;i<buffer.length;i++){
                stringBuilder.append(" "+buffer[i]);
            }
            System.out.println("receive:"+stringBuilder.toString());
        }
    }

    /**
     * 把int->ip地址
     * @param ipInt
     * @return String
     */
    public static String intToIp(int ipInt) {
        return new StringBuilder().append(((ipInt >> 24) & 0xff)).append('.')
                .append((ipInt >> 16) & 0xff).append('.').append(
                        (ipInt >> 8) & 0xff).append('.').append((ipInt & 0xff))
                .toString();
    }

    /**
     * 把IP地址转化为int
     * @param ipAddr
     * @return int
     */
    public static int ipToInt(String ipAddr) {
        try {
            return bytesToInt(ipToBytesByInet(ipAddr));
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }

    /**
     * 根据位运算把 byte[] -> int
     * @param bytes
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }

    /**
     * 把IP地址转化为字节数组
     * @param ipAddr
     * @return byte[]
     */
    public static byte[] ipToBytesByInet(String ipAddr) {
        try {
            return InetAddress.getByName(ipAddr).getAddress();
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }
}
