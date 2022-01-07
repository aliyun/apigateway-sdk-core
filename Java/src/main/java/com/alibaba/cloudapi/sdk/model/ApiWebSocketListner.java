package com.alibaba.cloudapi.sdk.model;

/**
 * Created by fred on 2017/7/19.
 */
public interface ApiWebSocketListner {
    void onNotify(String message);
    void onFailure(Throwable t, ApiResponse response);
}
