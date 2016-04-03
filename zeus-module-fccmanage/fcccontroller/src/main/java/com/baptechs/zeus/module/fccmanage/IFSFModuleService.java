package com.baptechs.zeus.module.fccmanage;

import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import com.baptechs.zeus.module.fccmanage.message.HeartbeatMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by warden on 2016/3/22.
 */
public interface IFSFModuleService {

    void onReceiveHeartbeat(HeartbeatMessage heartbeatMessage);
    void onApplicationMessage(ChannelHandlerContext channelHandlerContext, ApplicationMessage message);

}
