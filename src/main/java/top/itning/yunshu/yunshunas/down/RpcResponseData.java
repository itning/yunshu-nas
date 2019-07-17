package top.itning.yunshu.yunshunas.down;

import com.google.gson.Gson;
import ikidou.reflect.TypeBuilder;

import java.lang.reflect.Type;

/**
 * @author itning
 * @date 2019/7/17 14:39
 */
public class RpcResponseData<T extends RpcResultType> {
    private static final Gson GSON = new Gson();

    private String id;
    private String jsonrpc;
    private T result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public static <R extends RpcResultType> RpcResponseData<R> fromJson(String json, Class<R> resultType) {
        Type type = TypeBuilder
                .newInstance(RpcResponseData.class)
                .addTypeParam(resultType)
                .build();
        return GSON.fromJson(json, type);
    }

    @Override
    public String toString() {
        return "RpcResponseData{" +
                "id='" + id + '\'' +
                ", jsonrpc='" + jsonrpc + '\'' +
                ", result=" + result +
                '}';
    }
}
