package websocket;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Desc:
 * Author: hp
 * Date: 2017/6/26
 */
public class SessionPool {
    private final static Map<String, Session> sessionPool = new HashMap<String, Session>();

    public static Session getSession(String host, String user, String pwd) {
        Session session = sessionPool.get(host + "_" + user);
        try {
            if (session == null || !session.isConnected()) {
                JSch jsch=new JSch();
                session = jsch.getSession(user, host);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(pwd);
                session.connect();
                sessionPool.put(host + "_" + user, session);
            }
        }catch (JSchException e) {
            closeSession(session);
        }

        return session;
    }
    private static void closeSession(Session session) {
        if(session != null
                && session.isConnected()){
            session.disconnect();
        }
    }
}
