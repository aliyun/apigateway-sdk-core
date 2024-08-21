package com.alibaba.cloudapi.sdk.model;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;

/**
 * Created by fred on 2017/7/14.
 */
public class HttpClientBuilderParams extends BaseClientInitialParam {

    /**
     * for okhttp
     */
    KeyManager[] keyManagers = null;
    SSLSocketFactory sslSocketFactory = null;
    SSLContext sslContext = null;
    X509TrustManager x509TrustManager = null;
    HostnameVerifier hostnameVerifier = null;
    SecureRandom secureRandom = null;
    DefaultHttpRequestRetryStrategy requestRetryHandler = null;


    /**
     * for apacheclient
     */
    Registry<ConnectionSocketFactory> registry;



    /**
     * connectionPool
     **/
    private int maxIdleConnections = 5;
    private long maxIdleTimeMillis = 10 * 1000L;
    private long keepAliveDurationMillis = -1L;
    private long intevictIdleConnectionsIntervalMillis = 2000L;



    /**
     * dispatcher
     **/
    private int dispatchMaxRequests = 64;
    private int dispatchMaxRequestsPerHost = 5;
    private Runnable idleCallback = null;
    private ExecutorService executorService = null;



    public void check() {
        super.check();


    }

    public DefaultHttpRequestRetryStrategy getRequestRetryHandler() {
        return requestRetryHandler;
    }

    public void setRequestRetryHandler(DefaultHttpRequestRetryStrategy requestRetryHandler) {
        this.requestRetryHandler = requestRetryHandler;
    }

    public Registry<ConnectionSocketFactory> getRegistry() {
        return registry;
    }

    public void setRegistry(Registry<ConnectionSocketFactory> registry) {
        this.registry = registry;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
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

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getMaxIdleTimeMillis() {
        return maxIdleTimeMillis;
    }

    public void setMaxIdleTimeMillis(long maxIdleTimeMillis) {
        this.maxIdleTimeMillis = maxIdleTimeMillis;
    }

    public long getKeepAliveDurationMillis() {
        return keepAliveDurationMillis;
    }

    public void setKeepAliveDurationMillis(long keepAliveDurationMillis) {
        this.keepAliveDurationMillis = keepAliveDurationMillis;
    }


    public int getDispatchMaxRequests() {
        return dispatchMaxRequests;
    }

    public void setDispatchMaxRequests(int dispatchMaxRequests) {
        this.dispatchMaxRequests = dispatchMaxRequests;
    }

    public int getDispatchMaxRequestsPerHost() {
        return dispatchMaxRequestsPerHost;
    }

    public void setDispatchMaxRequestsPerHost(int dispatchMaxRequestsPerHost) {
        this.dispatchMaxRequestsPerHost = dispatchMaxRequestsPerHost;
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

    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(KeyManager[] keyManagers) {
        this.keyManagers = keyManagers;
    }

    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public long getIntevictIdleConnectionsIntervalMillis() {
        return intevictIdleConnectionsIntervalMillis;
    }

    public void setIntevictIdleConnectionsIntervalMillis(long intevictIdleConnectionsIntervalMillis) {
        this.intevictIdleConnectionsIntervalMillis = intevictIdleConnectionsIntervalMillis;
    }
}

