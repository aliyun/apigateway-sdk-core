package com.alibaba.cloudapi.sdk.model;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by fred on 2017/7/14.
 */
public class WebSocketClientBuilderParams extends BaseClientInitialParam {
    int requestExpiredTime = 10000;
    int callbackThreadPoolCount = 1;
    ApiWebSocketListner apiWebSocketListner;
    boolean isSkipSsCheck = false;



    public ApiWebSocketListner getApiWebSocketListner() {
        return apiWebSocketListner;
    }

    public void setApiWebSocketListner(ApiWebSocketListner apiWebSocketListner) {
        this.apiWebSocketListner = apiWebSocketListner;
    }

    public int getRequestExpiredTime() {
        return requestExpiredTime;
    }

    public void setRequestExpiredTime(int requestExpiredTime) {
        this.requestExpiredTime = requestExpiredTime;
    }

    public int getCallbackThreadPoolCount() {
        return callbackThreadPoolCount;
    }

    public void setCallbackThreadPoolCount(int callbackThreadPoolCount) {
        this.callbackThreadPoolCount = callbackThreadPoolCount;
    }

    public boolean isSkipSsCheck() {
        return isSkipSsCheck;
    }

    public void setSkipSsCheck(boolean skipSsCheck) {
        isSkipSsCheck = skipSsCheck;
    }
}
