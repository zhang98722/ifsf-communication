package com.baptechs.zeus.module.fccmanage.netty;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by warden on 2015/8/26.
 */
public class NettyServerConfig {

    @Getter @Setter int                     listenPort                          =4368;
    @Getter @Setter int                     broadcastPort                       =3486;
    @Getter @Setter int                     serverWorkerThreads                 =32;
    @Getter @Setter int                     serverCallbackExecutorThreads       =Runtime.getRuntime().availableProcessors();
    @Getter @Setter int                     serverSelectorThreads               =serverCallbackExecutorThreads * 2;


    @Getter @Setter int                     readerIdleTimeSeconds               =0;
    @Getter @Setter int                     writerIdleTimeSeconds               =0;
    @Getter @Setter int                     serverChannelMaxIdleTimeSeconds     =120;
}
