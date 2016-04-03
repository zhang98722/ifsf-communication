package com.baptechs.zeus.module.fccmanage.ifsf;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by warden on 2016/4/1.
 */
public class SessionFactory {
    private static ConcurrentHashMap<Integer,Session>  sessionMap           =new ConcurrentHashMap<>();

    private static volatile int                         sessionWheel        =1;

    public static Session getSession(int sessionId){
        return sessionMap.get(sessionId);
    }

    public static int putSession(Session session){
        sessionMap.put(session.getId(),session);
        return sessionMap.size();
    }

    public static Session generateSession(){
        Session session= new Session();
        session.id=generateSessionId();
        putSession(session);
        return session;
    }

    private static synchronized int generateSessionId(){
        sessionWheel++;
        if(sessionWheel>31){
            sessionWheel=1;
        }
        return sessionWheel;
    }
}
