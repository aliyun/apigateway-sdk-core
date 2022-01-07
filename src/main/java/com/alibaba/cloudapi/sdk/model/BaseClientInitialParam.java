package com.alibaba.cloudapi.sdk.model;

import com.alibaba.cloudapi.sdk.enums.Scheme;
import com.alibaba.cloudapi.sdk.exception.SdkException;

/**
 * Created by fred on 2017/7/19.
 */
public class BaseClientInitialParam {
    String appKey;
    String appSecret;
    String host;
    Scheme scheme;
    long connectionTimeout = 10000l;
    long readTimeout = 10000l;
    long writeTimeout = 10000l;

    public void check(){
        if(isEmpty(appKey) || isEmpty(appKey)){
            throw new SdkException("app key or app secret must be initialed");
        }

    }

    protected boolean isEmpty(String str){
        return str == null || str.equals("");
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }
}
