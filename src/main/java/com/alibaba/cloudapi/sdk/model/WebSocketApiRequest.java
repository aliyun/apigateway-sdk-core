package com.alibaba.cloudapi.sdk.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fred on 2017/7/31.
 */
public class WebSocketApiRequest {
    String method;
    String host;
    String path;
    Map<String , String> querys = new HashMap<String, String>();
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    int isBase64 = 0;
    String body;



    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Map<String, String> getQuerys() {
        return querys;
    }

    public void setQuerys(Map<String, List<String>> querys) {
        if(querys != null){
            for(Map.Entry<String , List<String>> entry : querys.entrySet()){
                this.querys.put(entry.getKey() , entry.getValue().get(0));
            }
        }
    }

    public int getIsBase64() {
        return isBase64;
    }

    public void setIsBase64(int isBase64) {
        this.isBase64 = isBase64;
    }
}
