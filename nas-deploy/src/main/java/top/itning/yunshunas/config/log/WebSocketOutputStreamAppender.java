package top.itning.yunshunas.config.log;

import ch.qos.logback.core.OutputStreamAppender;


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
