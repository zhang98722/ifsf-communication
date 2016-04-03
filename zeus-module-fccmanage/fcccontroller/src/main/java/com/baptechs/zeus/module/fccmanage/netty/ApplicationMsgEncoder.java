package com.baptechs.zeus.module.fccmanage.netty;

import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by warden on 2015/8/25.
 */
public class ApplicationMsgEncoder extends MessageToByteEncoder<ApplicationMessage> {
    Logger                      logger                  = LoggerFactory.getLogger(ApplicationMsgEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ApplicationMessage applicationMessage, ByteBuf out) throws Exception {
        try {
            ByteBuffer byteBuffer=applicationMessage.encode();
            if(byteBuffer!=null){
                out.writeBytes(byteBuffer);
            }
        }catch (Exception ex){
            logger.error("encode err",ex);
//            RemotingUtil.closeChannel(channelHandlerContext.channel());
        }
    }
}
