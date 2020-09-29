package top.itning.yunshunas.video.config;

import ch.qos.logback.core.OutputStreamAppender;
import top.itning.yunshunas.video.socket.LogWebSocket;


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
