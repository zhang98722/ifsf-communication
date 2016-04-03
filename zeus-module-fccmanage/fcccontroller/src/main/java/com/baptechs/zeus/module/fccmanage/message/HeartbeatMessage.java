package com.baptechs.zeus.module.fccmanage.message;

import com.baptechs.logistics.guardian.util.StringUtil;
import com.baptechs.zeus.module.fccmanage.ifsf.MessageCodeType;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by warden on 2016/3/22.
 * 数据结构：
 * hostIp····port····lnao····mc··status
 * 4byte     2byte  2byte    1b    1b
 *
 */
public class HeartbeatMessage extends IFSFMsg{

    @Getter @Setter Integer             ip;
    String                              ipString;
    @Getter @Setter Integer             port;
    @Getter @Setter Integer             lnao;
    @Getter @Setter Integer             messageCode                     = MessageCodeType.MESSAGE_CODE_HEARTBEAT_MSG;
    @Getter @Setter Integer             status                          =0;

    @Getter @Setter Integer             subnet;
    @Getter @Setter Integer             nodeId;

    private HeartbeatMessage(){}

    public HeartbeatMessage(String ip,int port,int subnet,int nodeId){
        this.ip=ipToInt(ip);
        this.port=port;
        this.subnet=subnet;
        this.nodeId=nodeId;
        this.lnao=(this.subnet<<8) | this.nodeId;
    }

    public ByteBuffer encode(){
        final ByteBuffer buffer=ByteBuffer.allocate(10);

        buffer.putInt(ip);
        buffer.putShort(port.shortValue());
        buffer.putShort(lnao.shortValue());
        buffer.put(messageCode.byteValue());
        buffer.put(status.byteValue());

        return buffer;
    }

    public String getIpString(){
        if(ip==null||ip==0){
            return null;
        }
        if(StringUtil.isEmpty(ipString)){
            ipString=intToIp(this.ip);
        }
        return ipString;
    }

    public static HeartbeatMessage decode(final byte[] array){
        HeartbeatMessage message=new HeartbeatMessage();

        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        message.ip=byteBuffer.getInt();
        message.port=Integer.parseInt(String.valueOf(byteBuffer.getShort()));
        message.lnao=Integer.parseInt(String.valueOf(byteBuffer.getShort()));
        message.subnet=message.lnao>>8;
        message.nodeId=message.lnao<<24>>24;

        return message;
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
