package top.itning.yunshu.yunshunas.down;


import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author itning
 * @date 2019/7/17 1:24
 */
public class DownloadHelper extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(DownloadHelper.class);
    private static final Gson GSON = new Gson();

    public DownloadHelper() {
        super(URI.create("ws://localhost:6800/jsonrpc"));
    }

    public static void main(String[] args) {
        DownloadHelper downloadHelper = new DownloadHelper();
        downloadHelper.connect();
        while (!downloadHelper.getReadyState().equals(ReadyState.OPEN)) {
            //ignore
        }
        System.out.println("打开了");

        RpcRequestData rpcRequestData = new RpcRequestData(RpcMethods.tellStopped, Lists.newArrayList(0, 1000));
        String json = GSON.toJson(rpcRequestData);
        System.out.println(json);
        downloadHelper.send(json);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("onOpen");
        logger.debug("HttpStatus: {}", handshakedata.getHttpStatus());
        logger.debug("HttpStatusMessage: {}", handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        logger.debug("onMessage: {}", message);
//        RpcResponseData<GetGlobalStat> rpcResponseData = RpcResponseData.fromJson(message, GetGlobalStat.class);
//        System.out.println(rpcResponseData);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("onClose {} {} {}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.debug("onError");
        logger.debug(ex.getMessage());
    }
}
