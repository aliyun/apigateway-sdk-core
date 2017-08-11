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

package com.alibaba.cloudapi.sdk.core;

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import com.alibaba.cloudapi.sdk.core.exception.SdkClientException;
import com.alibaba.cloudapi.sdk.core.model.ApiCallBack;
import com.alibaba.cloudapi.sdk.core.model.ApiRequest;
import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.cloudapi.sdk.core.model.BuilderParams;

import org.apache.commons.lang3.StringUtils;

/**
 * builder基类
 *
 * @author VK.Gao
 * @date 2017/03/01
 */
public abstract class BaseApiClientBuilder<Subclass extends BaseApiClientBuilder, TypeToBuild extends BaseApiClient> {

    private BuilderParams params = new BuilderParams();

    /**
     * get your app key from apigateway.console.aliyun.com -> API网关 -> 调用API -> 应用管理 -> 应用详情 -> appKey
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> appKey(String appKey) {
        params.setAppKey(appKey);
        return this;
    }

    /**
     * get your app appSecret from apigateway.console.aliyun.com -> API网关 -> 调用API -> 应用管理 -> 应用详情 -> appKey
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> appSecret(String appSecret) {
        params.setAppSecret(appSecret);
        return this;
    }

    /**
     * Sets maxIdleConnections property of the connectionPool
     *
     * <p>This method is deprecated since 1.0.3, use {@link #maxIdleTimeMills(long)} instead</>
     *
     * @param maxIdleConnections The maximum number of idle connections for each address
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    @Deprecated()
    public BaseApiClientBuilder<Subclass, TypeToBuild> maxIdleConnections(int maxIdleConnections) {
        params.setMaxIdleConnections(maxIdleConnections);
        return this;
    }

    /**
     * Sets maxIdleTimeMills property of the connectionPool
     *
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> maxIdleTimeMills(long maxIdleTimeMillis) {
        params.setMaxIdleTimeMillis(maxIdleTimeMillis);
        return this;
    }

    /**
     * Sets keepAliveDuration property of the connectionPool
     *
     * @param keepAliveDurationMillis A value of 0 means no timeout, otherwise
     *                                values must be between 1 and {@link Long#MAX_VALUE}
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> keepAliveDurationMillis(long keepAliveDurationMillis) {
        params.setKeepAliveDurationMillis(keepAliveDurationMillis);
        return this;
    }

    /**
     * Sets the default connect timeout for new connections
     *
     * @param connectionTimeoutMillis A value of 0 means no timeout, otherwise
     *                                values must be between 1 and {@link Long#MAX_VALUE}
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> connectionTimeoutMillis(long connectionTimeoutMillis) {
        params.setConnectionTimeoutMillis(connectionTimeoutMillis);
        return this;
    }

    /**
     * Sets the default read timeout for new connections.
     *
     * @param readTimeoutMillis A value of 0 means no timeout, otherwise
     *                          values must be between 1 and {@link Long#MAX_VALUE}
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> readTimeoutMillis(long readTimeoutMillis) {
        params.setReadTimeoutMillis(readTimeoutMillis);
        return this;
    }

    /**
     * Sets the default write timeout for new connections.
     *
     * @param writeTimeoutMillis A value of 0 means no timeout, otherwise
     *                           values must be between 1 and {@link Long#MAX_VALUE}
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> writeTimeoutMillis(long writeTimeoutMillis) {
        params.setWriteTimeoutMillis(writeTimeoutMillis);
        return this;
    }

    /**
     * Each client uses an {@link ExecutorService} to run calls internally. If you supply your
     * own executor, it should be able to run {@linkplain #maxRequests(int) the configured maximum} number
     * of calls concurrently.
     *
     * @param executorService your own executor
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> executorService(ExecutorService executorService) {
        params.setExecutorService(executorService);
        return this;
    }

    /**
     * Sets the socket factory used to secure HTTPS connections. If unset, the system default will
     * be used.
     *
     * @param sslSocketFactory custom SSLSocketFactory
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        params.setSslSocketFactory(sslSocketFactory);
        return this;
    }

    /**
     * Set the maximum number of requests to execute concurrently. Above this requests queue in
     * memory, waiting for the running calls to complete.
     * <p>
     * <p>If more than {@code maxRequests} requests are in flight when this is invoked, those requests
     * will remain in flight.
     *
     * @param maxRequests
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> maxRequests(int maxRequests) {
        params.setMaxRequests(maxRequests);
        return this;
    }

    /**
     * Set the maximum number of requests for each host to execute concurrently. This limits requests
     * by the URL's host name. Note that concurrent requests to a single IP address may still exceed
     * this limit: multiple hostnames may share an IP address or be routed through the same HTTP
     * proxy.
     * <p>
     * <p>If more than {@code maxRequestsPerHost} requests are in flight when this is invoked, those
     * requests will remain in flight.
     *
     * @param maxRequestsPerHost connect time out threshold in millisecond
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> maxRequestsPerHost(int maxRequestsPerHost) {
        params.setMaxRequestsPerHost(maxRequestsPerHost);
        return this;
    }

    /**
     * Set a callback to be invoked each time the dispatcher becomes idle (when the number of running
     * calls returns to zero).
     *
     * <p>Note: The time at which a call is considered idle is different depending on whether it was
     * run {@linkplain BaseApiClient#asyncInvoke asynchronously} or
     * {@linkplain BaseApiClient#syncInvoke synchronously}. Asynchronous calls become idle after the
     * {@link ApiCallBack#onResponse onResponse} or {@link ApiCallBack#onFailure onFailure} callback
     * has returned. Synchronous calls become idle once {@link BaseApiClient#syncInvoke(ApiRequest) syncInvoke}
     * returns. This means that if you are doing synchronous calls the network layer will not truly
     * be idle until every returned {@link ApiResponse} has been closed.
     *
     * @param idleCallback
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> idleCallback(Runnable idleCallback) {
        params.setIdleCallback(idleCallback);
        return this;
    }

    /**
     * Sets the socket factory and trust manager used to secure HTTPS connections. If unset, the
     * system defaults will be used.
     *
     * <p>Most applications should not call this method, and instead use the system defaults. Those
     * classes include special optimizations that can be lost if the implementations are decorated.
     *
     * <p>If necessary, you can create and configure the defaults yourself.
     *
     * @param x509TrustManager
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> x509TrustManager(X509TrustManager x509TrustManager) {
        return x509TrustManagers(new X509TrustManager[] {x509TrustManager});
    }

    /**
     * Sets the socket factory and trust manager used to secure HTTPS connections. If unset, the
     * system defaults will be used.
     *
     * <p>Most applications should not call this method, and instead use the system defaults. Those
     * classes include special optimizations that can be lost if the implementations are decorated.
     *
     * <p>If necessary, you can create and configure the defaults yourself.
     *
     * @param x509TrustManagers
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> x509TrustManagers(X509TrustManager[] x509TrustManagers) {
        params.setX509TrustManagers(x509TrustManagers);
        return this;
    }

    public BaseApiClientBuilder<Subclass, TypeToBuild> keyManagers(KeyManager[] keyManagers) {
        params.setKeyManagers(keyManagers);
        return this;
    }

    public BaseApiClientBuilder<Subclass, TypeToBuild> secureRandom(SecureRandom secureRandom) {
        params.setSecureRandom(secureRandom);
        return this;
    }

    /**
     * Sets the verifier used to confirm that response certificates apply to requested hostnames for
     * HTTPS connections.
     *
     * <p>If unset, a default hostname verifier will be used.
     *
     * @param hostnameVerifier
     * @return @{@linkplain BaseApiClientBuilder this builder}
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> hostnameVerifier(HostnameVerifier hostnameVerifier) {
        params.setHostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * Sets the extra params while you implements a {@linkplain HttpClient httpClient} yourself.
     *
     * <p>This map will transfer to the constructor of the implements of {@linkplain HttpClient HttpClient}
     *
     * <p>By default, you can set key="apache.httpclient.builder" with a {@link org.apache.http.impl.client.HttpClientBuilder HttpClientBuilder} as value
     * to defined the base builder in {@link com.alibaba.cloudapi.sdk.core.http.ApacheHttpClient#ApacheHttpClient(BuilderParams) ApacheHttpClient constructor}
     *
     * @param key
     * @param value
     * @return
     */
    public BaseApiClientBuilder<Subclass, TypeToBuild> setExtParams(String key, Object value){
        params.setExtParam(key, value);
        return this;
    }

    public final TypeToBuild build() {
        if (StringUtils.isEmpty(this.params.getAppKey()) || StringUtils.isEmpty(this.params.getAppSecret())) {
            throw new SdkClientException("appKey or appSecret must not be null");
        }
        TypeToBuild apiClient = build(params);
        BaseApiClient.instanceMap.put(apiClient.getClass(), apiClient);
        return apiClient;
    }

    protected abstract TypeToBuild build(BuilderParams params);

}
