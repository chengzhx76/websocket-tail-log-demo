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


@ServerEndpoint("/test")
public class LogWebSocketHandle {
	ChannelExec openChannel = null;
	com.jcraft.jsch.Session session = null;
	InputStream in = null;

	@OnOpen
	public void onOpen(Session session) throws IOException, InterruptedException {
		// 1.先启动
		session.getBasicRemote().sendText("---开始启动---");
		execCommand(Config.serverIp, Config.serverUsername, Config.serverPwd, Config.dbrepPath + "start.sh");
		session.getBasicRemote().sendText("---启动完毕---");

		// 2.查看nohub日志（先判断是否存在-备份）
		session.getBasicRemote().sendText("---开始打印nohup.out日志---");
		BufferedReader nohubLog = tailLog(Config.serverIp, Config.serverUsername, Config.serverPwd, "cat " + Config.dbrepPath + "nohup.out");
		String buf = "";
		while ((buf = nohubLog.readLine()) != null) {
			Thread.sleep(10L);
			session.getBasicRemote().sendText(buf);
		}
		session.getBasicRemote().sendText("---打印nohup.out结束---");

		// 3.再打印log4j日志（先判断是否存在-备份）
		session.getBasicRemote().sendText("---开始打印log4j_daily日志---");
		BufferedReader log4jLog = tailLog(Config.serverIp, Config.serverUsername, Config.serverPwd, "tail -f  " + Config.dbrepPath + "log/log4j_daily.log");
		while ((buf = log4jLog.readLine()) != null) {
			Thread.sleep(10L);
			session.getBasicRemote().sendText(buf);
		}
	}

	/**
	 * WebSocket请求关闭
	 */
	@OnClose
	public void onClose() {
		System.out.println("WebSocket请求关闭");
		closeInputStream();
		closeSession();
		closeChannelExec();
	}

	@OnError
	public void onError(Throwable thr) {
		System.out.println("连接关闭");
		closeInputStream();
		closeSession();
		closeChannelExec();
		thr.printStackTrace();
	}

	/**
	 * 执行命令
	 * @param host
	 * @param user
	 * @param psw
	 * @param command
	 * @return
	 */
	private BufferedReader tailLog(String host, String user, String psw, String command) {
		try {
			openChannel = execCommand(host, user, psw, command);
			if (openChannel != null) {
				in = openChannel.getInputStream();
				return new BufferedReader(new InputStreamReader(in, "GBK"));
			}
		} catch (IOException e) {
			closeInputStream();
			e.printStackTrace();

		}
		return null;
	}

	private ChannelExec execCommand(String host, String user, String psw, String command) {
		System.out.println("HOST = " + host + " USER = " + user + " PSW = " + psw + " COMMAND = " + command);

		session = SessionPool.getSession(host, user, psw);

		try {
			openChannel = (ChannelExec) session.openChannel("exec");
			openChannel.setCommand(command);
			openChannel.setInputStream(null);
			openChannel.setErrStream(System.err);
//      int exitStatus = openChannel.getExitStatus();
//      System.out.println(exitStatus);
			openChannel.connect();
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return openChannel;
	}

	private void closeInputStream() {
		if (in != null) {
			try {
				in.close();
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
		if(session != null
				&& session.isConnected()){
			session.disconnect();
		}
	}
}
