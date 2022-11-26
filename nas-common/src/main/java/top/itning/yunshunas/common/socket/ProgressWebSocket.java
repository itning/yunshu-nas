package top.itning.yunshunas.common.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志输出
 *
 * @author itning
 */
@Component
@ServerEndpoint(value = "/p")
public final class ProgressWebSocket {
    private static final Logger logger = LoggerFactory.getLogger(ProgressWebSocket.class);
    /**
     * 存放Session
     */
    private static final Map<String, Session> SESSION_MAP = new HashMap<>(16);

    public static void sendMessage(String msg) {
        clearSessionMap();
        SESSION_MAP.forEach((k, v) -> {
            try {
                v.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                logger.debug(e.getMessage());
            }
        });
    }

    /**
     * 清理SessionMap
     */
    private synchronized static void clearSessionMap() {
        SESSION_MAP.values().stream()
                .filter(session -> !session.isOpen()).toList()
                .forEach(session -> SESSION_MAP.remove(session.getId()));
    }


    @OnOpen
    public void onOpen(Session session) {
        SESSION_MAP.put(session.getId(), session);
    }

    @OnClose
    public void onClose() {
        logger.debug("on close");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        logger.debug("onMessage {}", message);
        //回复用户
        session.getBasicRemote().sendText("收到消息 ");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSION_MAP.remove(session.getId());
        logger.error("onError ", error);
        error.printStackTrace();
    }
}
