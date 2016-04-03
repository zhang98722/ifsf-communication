package com.baptechs.zeus.module.fccmanage.netty;

import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by warden on 2016/3/18.
 */
public class ApplicationMsgDecoder extends LengthFieldBasedFrameDecoder {
    Logger                      logger                  = LoggerFactory.getLogger(ApplicationMsgDecoder.class);

    private static final int    FRAME_MAX_LENGTH = 1024 * 1024 * 8;

    public ApplicationMsgDecoder(){
        super(FRAME_MAX_LENGTH,6,2,0,0,true);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        try {
            ByteBuf frame = (ByteBuf) super.decode(ctx, in);
            if (frame == null) {
                return null;
            }

            byte[] tmpBuf = new byte[frame.capacity()];
            frame.getBytes(0, tmpBuf);
            frame.release();

            return ApplicationMessage.decode(tmpBuf);
        }catch (Exception ex){
            logger.error("decode err",ex);
        }

        return null;
    }
}
