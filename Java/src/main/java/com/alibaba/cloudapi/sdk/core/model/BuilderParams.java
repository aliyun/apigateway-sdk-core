/*
 * Copyright 2017 Alibaba Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloudapi.sdk.core.model;


import com.alibaba.cloudapi.sdk.core.enums.Scheme;

import javax.net.ssl.*;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;

/**
 * 用于构建sdkClient的Builder所需的参数
 *
 * @author VK.Gao
 * @date 2017/03/01
 */
public final class BuilderParams implements Serializable, Cloneable {

    /**
     * verify
     */
    private String appKey;
    private String appSecret;

    /**
     * connectionPool
     **/
    private int maxIdleConnections = 5;
    private long keepAliveDurationMillis = 5L;

    /**
     * timeout
     **/
    private long connectionTimeoutMillis = 15000L;
    private long readTimeoutMillis = 15000L;
    private long writeTimeoutMillis = 15000L;

    /**
     * schema
     */
    private Scheme schema;

    /**
     * https
     **/
    private SSLSocketFactory sslSocketFactory = null;
    private X509TrustManager x509TrustManager = null;
    private HostnameVerifier hostnameVerifier = null;

    /**
     * dispatcher
     **/
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private Runnable idleCallback = null;
    private ExecutorService executorService = null;

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

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getKeepAliveDurationMillis() {
        return keepAliveDurationMillis;
    }

    public void setKeepAliveDurationMillis(long keepAliveDurationMillis) {
        this.keepAliveDurationMillis = keepAliveDurationMillis;
    }

    public long getConnectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    public void setConnectionTimeoutMillis(long connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    public long getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(long readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public long getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public void setWriteTimeoutMillis(long writeTimeoutMillis) {
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public X509TrustManager getX509TrustManager() {
        return x509TrustManager;
    }

    public void setX509TrustManager(X509TrustManager x509TrustManager) {
        this.x509TrustManager = x509TrustManager;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
    }

    public Runnable getIdleCallback() {
        return idleCallback;
    }

    public void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Scheme getSchema() {
        return schema;
    }

    public void setSchema(Scheme schema) {
        this.schema = schema;
    }
}
