package com.baptechs.zeus.module.fccmanage.netty;

import com.alibaba.fastjson.JSONObject;
import com.baptechs.zeus.module.fccmanage.IFSFModuleService;
import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by warden on 2015/8/26.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<ApplicationMessage>{
    Logger                                  logger                          = LoggerFactory.getLogger(NettyServerHandler.class);

    IFSFModuleService                       ifsfModuleService;

    public NettyServerHandler(IFSFModuleService ifsfModuleService){
        this.ifsfModuleService=ifsfModuleService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ApplicationMessage message) throws Exception {
        ifsfModuleService.onApplicationMessage(channelHandlerContext,message);
        logger.debug("read:"+JSONObject.toJSONString(message));
    }
}
