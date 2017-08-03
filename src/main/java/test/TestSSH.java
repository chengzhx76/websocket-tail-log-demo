package test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import websocket.Config;
import websocket.SessionPool;

import java.io.*;

/**
 * Desc:
 * Author: hp
 * Date: 2017/6/26
 */
public class TestSSH {
    public static void main(String[] args) throws IOException, JSchException {
        test01();
    }
    private static void test() throws JSchException, IOException {
        String buf = "";
        Session SSHSession = SessionPool.getSession(Config.serverIp, Config.serverUsername, Config.serverPwd);
        ChannelExec openChannel = (ChannelExec) SSHSession.openChannel("exec");
        openChannel.setCommand(Config.dbrepPath + "s.sh");
        openChannel.setInputStream(null);
        openChannel.setErrStream(System.err);
        openChannel.connect();

        InputStream stream = openChannel.getInputStream();
        BufferedReader exec = new BufferedReader(new InputStreamReader(stream, "GBK"));
        while ((buf = exec.readLine()) != null) {
            System.out.println(buf);
        }
    }


    public static void test01() {
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream(1024);

        PrintStream cacheStream = new PrintStream(baoStream);
//        PrintStream oldStream = System.out;

        System.setErr(cacheStream);

        System.err.print("hello world!");

//        System.setOut(oldStream);
        System.out.println("<-- " + baoStream + " -->");
    }
}
