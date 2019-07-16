package top.itning.yunshu.yunshunas.config;

import ch.qos.logback.core.OutputStreamAppender;
import top.itning.yunshu.yunshunas.socket.LogWebSocket;

/**
 * WebSocket OutputStreamAppender
 *
 * @author itning
 */
public class WebSocketOutputStreamAppender<E> extends OutputStreamAppender<E> {
    @Override
    public void start() {
        setOutputStream(LogWebSocket.getOutputStream());
        super.start();
    }
}
