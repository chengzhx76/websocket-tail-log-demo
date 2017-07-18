package websocket;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Desc:
 * Author: hp
 * Date: 2017/6/26
 */
@ServerEndpoint("/tailLog4JLog")
public class Log4JLog {
    ChannelExec openChannel = null;
    com.jcraft.jsch.Session SSHSession = null;
    InputStream stream = null;

    @OnOpen
    public void onOpen(Session session) throws IOException, InterruptedException {
        session.getBasicRemote().sendText("---开始打印log4j_daily日志---");
        SSHSession = SessionPool.getSession(Config.serverIp, Config.serverUsername, Config.serverPwd);

        try {
            openChannel = (ChannelExec) SSHSession.openChannel("exec");
            openChannel.setCommand("tail -f  " + Config.dbrepPath + "log/log4j_daily.log");
            openChannel.setInputStream(null);
            openChannel.setErrStream(System.err);
            openChannel.connect();
            stream = openChannel.getInputStream();
            BufferedReader exec = new BufferedReader(new InputStreamReader(stream, "GBK"));
            String buf = "";
            while ((buf = exec.readLine()) != null) {
                Thread.sleep(30L);
                session.getBasicRemote().sendText(buf);
            }
        } catch (JSchException e) {
            closeInputStream();
            closeSession();
            closeChannelExec();
            e.printStackTrace();
        }

        session.getBasicRemote().sendText("---打印log4j_daily结束---");
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
        closeInputStream();
        closeSession();
        closeChannelExec();
        System.out.println("WebSocket请求关闭");
    }

    @OnError
    public void onError(Throwable thr) {
        closeInputStream();
        closeSession();
        closeChannelExec();
        System.out.println("连接关闭");
        thr.printStackTrace();
    }

    private void closeInputStream() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void closeChannelExec() {
        if(openChannel != null
                && !openChannel.isClosed()){
            openChannel.disconnect();
        }
    }

    private void closeSession() {
        if(SSHSession != null
                && SSHSession.isConnected()){
            SSHSession.disconnect();
        }
    }
}
