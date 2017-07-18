package websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Desc:
 * Author: hp
 * Date: 2017/6/26
 */
@ServerEndpoint("/testWeb")
public class TestWebSocketHandle {
    @OnOpen
    public void onOpen(Session session) throws IOException, InterruptedException {
        System.out.println("---开始启动---");
        session.getBasicRemote().sendText("---开始启动---");
        for (int i=0; i<150; i++) {
            Thread.sleep(500L);
            session.getBasicRemote().sendText("---启动ing---"+i);
        }
        session.getBasicRemote().sendText("---启动完毕---");
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
        System.out.println("WebSocket请求关闭");
    }

    @OnError
    public void onError(Throwable thr) {
        System.out.println("连接关闭");
        thr.printStackTrace();
    }

}
