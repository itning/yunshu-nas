package top.itning.yunshu.yunshunas.down;

import top.itning.utils.uuid.UUIDs;

import java.util.List;

/**
 * @author itning
 * @date 2019/7/17 14:20
 */
public class RpcRequestData {
    private String jsonrpc = "2.0";
    private String id;
    private RpcMethods method;
    private List<Object> params;


    public RpcRequestData(RpcMethods method, List<Object> params) {
        this.id = method.name() + "-" + UUIDs.get();
        this.method = method;
        this.params = params;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RpcMethods getMethod() {
        return method;
    }

    public void setMethod(RpcMethods method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
