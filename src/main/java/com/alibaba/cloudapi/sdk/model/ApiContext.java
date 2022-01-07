package com.alibaba.cloudapi.sdk.model;

import java.util.Date;

/**
 * Created by fred on 2017/8/3.
 */
public class ApiContext {
    ApiCallback callback;
    ApiRequest request;
    long startTime;

    public ApiContext(ApiCallback callback , ApiRequest request){
        this.callback = callback;
        this.request = request;
        this.startTime = (new Date()).getTime();
    }

    public ApiCallback getCallback() {
        return callback;
    }

    public void setCallback(ApiCallback callback) {
        this.callback = callback;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public void setRequest(ApiRequest request) {
        this.request = request;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
