package com.baptechs.zeus.module.fccmanage.message;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by warden on 2016/3/18.
 */
public class ApplicationMessage extends IFSFMsg{
    static Logger                               logger                                          = LoggerFactory.getLogger(ApplicationMessage.class);

    public static final Integer                 MESSAGE_TYPE_READ                               =0;
    public static final Integer                 MESSAGE_TYPE_ANSWER                             =1;
    public static final Integer                 MESSAGE_TYPE_WRITE                              =2;
    public static final Integer                 MESSAGE_TYPE_UNSOLICTIED_DATA_MSG_WITH_ACK      =3;
    public static final Integer                 MESSAGE_TYPE_UNSOLICATED_DATA_MSG_WITHOUT_ACK   =4;
    public static final Integer                 MESSAGE_TYPE_ACK                                =7;

    @Getter Integer                             lnar;
    @Getter @Setter Integer                     recipientSubnet;
    @Getter @Setter Integer                     recipientNodeId;

    @Getter Integer                             lnao;
    @Getter @Setter Integer                     originatorSubnet;
    @Getter @Setter Integer                     originatorNodeId;

    @Getter @Setter Integer                     messageCode;

    @Getter  Integer                            messageStatus;
    @Getter @Setter Integer                     messageType;
    @Getter @Setter Integer                     token;

    @Getter @Setter Integer                     msgLength;
    @Getter @Setter Integer                     dbAdLength;

    @Getter @Setter byte[]                      dbAd;
    @Getter byte[]                              data;
    @Getter Map<Byte,byte[]>                    dataMap                                         =new HashMap<>();


    /**
     * messageStatus包含两部分，前3个字节为消息类型，后5个字节是token
     * @param messageStatus
     */
    public void setMessageStatus(Integer messageStatus) {
        this.messageStatus = messageStatus;
        messageType=messageStatus>>5;
        token=messageStatus<<29;
        token=token>>29;
    }

    public ByteBuffer encode(){
        return null;
    }

    public void setLnar(Integer lnar) {
        this.lnar = lnar;
        this.recipientSubnet=lnar>>8;
        this.recipientNodeId=lnar<<24>>24;
    }

    public void setLnao(Integer lnao) {
        this.lnao = lnao;
        this.originatorSubnet=lnao>>8;
        this.originatorNodeId=lnao<<24>>24;
    }

    /**
     * 解码Application message
     * 结构：
     *     LNAR····LNAO····MC··M_ST··M_LG····DB_AD_LG··DB_AD········data
     *     0·······2·······4···5·····6·······8·········9················9+DB_AD_LG
     *
     • LNAR - Logical Node Address Recipient
     – Broken into two parts, Subnet and Node address
     – Subnet 1-255, Node 1-127
     – Usually matches LonTalk Subnet/Node of Recipient
     • LNAO - Logical Node Address Originator
     – Usually matches LonTalk Subnet/Node of Originator
     • MC - Message Code
     – 0 = Application Message, 1 = Heartbeat Message
     2 = Communications Message
     • M_St - Message Status indicates the type of
     message and holds the message token
     • M_Lg - Message Length from position 8 to end
     • DB_Ad_Lg - Database Address Length, 1 to 8 bytes
     • DB_Ad - The full database address

     * @param array
     * @return
     */
    public static ApplicationMessage decode(final byte[] array){
        ApplicationMessage result=new ApplicationMessage();
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(array);

            byte[] data=new byte[2];
            byteBuffer.get(data);
            result.setLnar(getIntFromByte(data));

            data=new byte[2];
            byteBuffer.get(data);
            result.setLnao(getIntFromByte(data));

            data=new byte[1];
            byteBuffer.get(data);
            result.setMessageCode(getIntFromByte(data));

            data=new byte[1];
            byteBuffer.get(data);
            result.setMessageStatus(getIntFromByte(data));

            data=new byte[2];
            byteBuffer.get(data);
            result.setMsgLength(getIntFromByte(data));

            data=new byte[1];
            byteBuffer.get(data);
            result.setDbAdLength(getIntFromByte(data));

            data=new byte[result.getDbAdLength()];
            byteBuffer.get(data);
            result.setDbAd(data);

            data=new byte[byteBuffer.array().length-byteBuffer.position()];
            byteBuffer.get(data);
            result.setData(data);
            byteBuffer.flip();

            logger.debug("decode success:"+ getByteString(byteBuffer.array()));
        }catch (Exception ex){
            throw new RuntimeException("decode err",ex);
        }

        return result;
    }

    private static int getIntFromByte(byte[] bytes){
        int result=0;

        if(bytes.length==1){
            return result|bytes[0];
        }

        if(bytes.length==2){
            result=result|(bytes[0]<<8);
            result=result|bytes[1];
            return result;
        }

        return result;
    }

    public static String getByteString(byte[] data){
        StrBuilder strBuilder=new StrBuilder(data.length);
        for(byte item:data){
            String hex=Integer.toHexString(item & 0xff);
            if(hex.length()==1){
                strBuilder.append("0");
            }
            strBuilder.append(hex.toUpperCase());
            strBuilder.append(".");
        }
        return strBuilder.toString();
    }

    public void setData(byte[] data) {
        this.data = data;
        int potion=0;
        while (potion<data.length){
            byte dataId=data[potion];
            potion++;
            byte dataLength=data[potion];
            potion++;
            byte[] dataValue=new byte[dataLength];
            for(int i=0;i<dataLength;i++){
                dataValue[i]=data[potion];
                potion++;
            }
            dataMap.put(dataId,dataValue);
        }
    }

    public static void main(String[] args) {
        byte[] bytes=new byte[1];
        bytes[0]=100;
        System.out.println(bytes[0]);
        System.out.format(Integer.toHexString(bytes[0]&0xff));
    }
}
