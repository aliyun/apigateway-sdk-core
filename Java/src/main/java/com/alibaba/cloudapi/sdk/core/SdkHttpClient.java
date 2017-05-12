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

import com.alibaba.cloudapi.sdk.core.model.BuilderParams;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * sdk的httpclient，基于okHttp3封装
 *
 * @author VK.Gao
 * @date 2017/03/01
 */
final class SdkHttpClient {

    private final OkHttpClient httpClient;

    SdkHttpClient(BuilderParams builderParams) {

        /*
         * prepare connectionPool
         */
        ConnectionPool connectionPool = new ConnectionPool(builderParams.getMaxIdleConnections(), builderParams.getKeepAliveDurationMillis(), TimeUnit.MILLISECONDS);

        /*
         * prepare dispatcher
         */
        Dispatcher dispatcher = new Dispatcher(builderParams.getExecutorService());
        dispatcher.setMaxRequests(builderParams.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(builderParams.getMaxRequestsPerHost());
        if (builderParams.getIdleCallback() != null) {
            dispatcher.setIdleCallback(builderParams.getIdleCallback());
        }

        /*
         * build httpclient
         */
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectionPool(connectionPool);
        builder.dispatcher(dispatcher);
        builder.readTimeout(builderParams.getReadTimeoutMillis(), TimeUnit.MILLISECONDS);
        builder.writeTimeout(builderParams.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);
        builder.connectTimeout(builderParams.getConnectionTimeoutMillis(), TimeUnit.MILLISECONDS);

        if (builderParams.getSslSocketFactory() != null && builderParams.getX509TrustManager() != null) {
            builder.sslSocketFactory(builderParams.getSslSocketFactory(), builderParams.getX509TrustManager());
        }
        if (builderParams.getHostnameVerifier() != null) {
            builder.hostnameVerifier(builderParams.getHostnameVerifier());
        }

        httpClient = builder.build();
    }

    Response syncInvoke(Request request) throws IOException {
        Call call = httpClient.newCall(request);
        return call.execute();
    }

    void asyncInvoke(Request request, Callback callback) {
        Call call = httpClient.newCall(request);
        call.enqueue(callback);
    }
}
