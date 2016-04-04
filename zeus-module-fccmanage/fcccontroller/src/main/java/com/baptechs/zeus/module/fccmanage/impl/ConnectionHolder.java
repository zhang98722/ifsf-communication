package com.baptechs.zeus.module.fccmanage.impl;

import com.baptechs.zeus.domain.common.redis.RedisKeyHelper;
import com.baptechs.zeus.module.fccmanage.message.ApplicationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by warden on 2016/4/4.
 */
public class ConnectionHolder {
    static Logger                       logger                  = LoggerFactory.getLogger(ConnectionHolder.class);

    private static Object               locker                  =new Object();
    //key=ip-socket
    private static Map<String,Socket>   socketMap               =new HashMap<>();
    private static Map<String,Object>   lockerMap               =new HashMap<>();

    public static boolean putConnect(String host,int port){
        String key= getHostKey(host,port);
        synchronized (locker){
            if(socketMap.containsKey(key)){
                return true;
            }
            try {
                Socket socket=new Socket(host,port);
                socketMap.put(key,socket);
                logger.info("create connection to {}:{}",host,port);
            } catch (IOException e) {
                logger.error("open socket err,host"+host+",port:"+port,e);
                return false;
            }
        }

        return true;
    }

    public static boolean writeAndFlush(String host,int port,byte[] data){
        String key=getHostKey(host,port);

        //1.获取锁，通过每个连接一个锁锁保证每一个连接是线程安全的
        Object socketLock=null;
        synchronized (locker){
            if(!socketMap.containsKey(key)){
                return false;
            }
            if(!lockerMap.containsKey(key)){
                lockerMap.put(key,new Object());
            }
            socketLock=lockerMap.get(key);
        }

        //2.获取连接并写入数据，如果连接已经关闭或者无法联通，则创建一个新的连接
        Socket socket=null;
        synchronized (socketLock){
            socket=socketMap.get(key);
            if(socket==null){
                return false;
            }
            //自动重连
            if(!socket.isClosed()||!socket.isConnected()){
                logger.warn("dead connect find,{}:{}",host,port);
                String[] hostInfo=key.split("-");
                try {
                    socket.close();
                }catch (Exception ex){
                    logger.error("close socket err,key:{}",key);
                }
                try {
                    socket=new Socket(hostInfo[0],Integer.parseInt(hostInfo[1]));
                    logger.warn("reconnect to {}:{}",host,port);
                } catch (IOException e) {
                    logger.error("create connection err,key:"+key,e);
                    return false;
                }
            }
            try {
                socket.getOutputStream().write(data);
                socket.getOutputStream().flush();
                logger.debug("write data success,data:"+ ApplicationMessage.getByteString(data));
            }catch (Exception ex){
                logger.error("write data err,key:"+key,ex);
                return false;
            }
        }
        return true;
    }

    private static String getHostKey(String host,int port){
        return RedisKeyHelper.buildKey(host,String.valueOf(port));
    }
}
