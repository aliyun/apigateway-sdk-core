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

package com.alibaba.cloudapi.sdk.core.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import com.alibaba.cloudapi.sdk.core.HttpClient;
import com.alibaba.cloudapi.sdk.core.exception.SdkClientException;
import com.alibaba.cloudapi.sdk.core.model.ApiCallBack;
import com.alibaba.cloudapi.sdk.core.model.ApiRequest;
import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.cloudapi.sdk.core.model.BuilderParams;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * @author VK.Gao
 * @date 2017/05/15
 */
public class ApacheHttpClient extends HttpClient {

    private static final String EXT_PARAM_KEY_BUILDER = "apache.httpclient.builder";
    private static final int DEFAULT_THREAD_KEEP_ALIVE_TIME = 60;

    private ExecutorService executorService;
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    public ApacheHttpClient(BuilderParams params) {
        super(params);
    }

    @Override
    protected void init(final BuilderParams params) {
        HttpClientBuilder builder;
        if (params.containsExtParam(EXT_PARAM_KEY_BUILDER)) {
            builder = (HttpClientBuilder)params.getExtParam(EXT_PARAM_KEY_BUILDER);
        } else {
            builder = HttpClientBuilder.create();
        }

        // connPool
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(params.getMaxRequests());
        connectionManager.setDefaultMaxPerRoute(params.getMaxRequestsPerHost());
        builder.setConnectionManager(connectionManager);
        ApacheIdleConnectionCleaner.registerConnectionManager(connectionManager, params.getMaxIdleTimeMillis());

        // default request config
        RequestConfig defaultConfig = RequestConfig.custom()
            .setConnectTimeout((int)params.getConnectionTimeoutMillis())
            .setSocketTimeout((int)params.getReadTimeoutMillis())
            .setConnectionRequestTimeout((int)params.getWriteTimeoutMillis())
            .build();
        builder.setDefaultRequestConfig(defaultConfig);

        // https
        if (params.getSslSocketFactory() != null) {
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(params.getSslSocketFactory(), params.getHostnameVerifier());
            builder.setSSLSocketFactory(sslConnectionSocketFactory);
        }
        if (params.getKeyManagers() != null || params.getX509TrustManagers() != null || params.getSecureRandom() != null) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(params.getKeyManagers(), params.getX509TrustManagers(), params.getSecureRandom());
                builder.setSSLContext(sslContext);
            } catch (NoSuchAlgorithmException e1) {
                throw new SSLInitializationException(e1.getMessage(), e1);
            } catch (KeyManagementException e2) {
                throw new SSLInitializationException(e2.getMessage(), e2);
            }
        }

        // async
        if (params.getExecutorService() == null) {
            executorService = new ThreadPoolExecutor(0, params.getMaxRequests(), DEFAULT_THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                new DeafultAsyncThreadFactory());
        } else {
            executorService = params.getExecutorService();
        }

        // keepAlive
        if (params.getKeepAliveDurationMillis() > 0) {
            builder.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    long duration = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response, context);

                    if (duration > 0 && duration < params.getKeepAliveDurationMillis()) {
                        return duration;
                    } else {
                        return params.getKeepAliveDurationMillis();
                    }
                }
            });
        }

        httpClient = builder.build();

    }

    private HttpUriRequest parseToHttpRequest(ApiRequest apiReq) {
        RequestBuilder builder = RequestBuilder.create(apiReq.getMethod().getName());

        /*
         *  拼接URL
         *  HTTP + HOST + PATH(With pathparameter) + Query Parameter
         */
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(apiReq.getScheme().name());
            uriBuilder.setHost(apiReq.getHost());
            uriBuilder.setPath(apiReq.getPath());
            if (MapUtils.isNotEmpty(apiReq.getQuerys())) {
                for (Map.Entry<String, String> entry : apiReq.getQuerys().entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            builder.setUri(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new SdkClientException("build http request uri failed", e);
        }

        EntityBuilder bodyBuilder = EntityBuilder.create();
        bodyBuilder.setContentType(ContentType.parse(apiReq.getMethod().getRequestContentType()));
        if (MapUtils.isNotEmpty(apiReq.getFormParams())) {
            /*
             *  如果formParams不为空
             *  将Form中的内容以urlQueryParams的格式存放在body中(k1=v1&k2=v2&k3=v3)
             */
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (Entry<String, String> entry : apiReq.getFormParams().entrySet()) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            bodyBuilder.setParameters(paramList);
            builder.setEntity(bodyBuilder.build());
        } else if (ArrayUtils.isNotEmpty(apiReq.getBody())) {
            bodyBuilder.setBinary(apiReq.getBody());
            builder.setEntity(bodyBuilder.build());
        }

        for (Map.Entry<String, String> entry : apiReq.getHeaders().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    private ApiResponse parseToApiResponse(HttpResponse httpResponse) throws IOException {
        ApiResponse result = new ApiResponse();

        // status code
        result.setStatusCode(httpResponse.getStatusLine().getStatusCode());

        if(httpResponse.getEntity() != null){
            // content type
            Header contentType = httpResponse.getEntity().getContentType();
            result.setContentType(contentType.getValue());

            // body
            result.setBody(EntityUtils.toByteArray(httpResponse.getEntity()));
        }else{
            Header contentType = httpResponse.getFirstHeader("Content-Type");
            result.setContentType(contentType.getValue());
        }

        // headers
        result.setHeaders(new HashMap<String, String>());
        for (Header header : httpResponse.getAllHeaders()) {
            result.getHeaders().put(header.getName(), header.getValue());
        }

        // message
        result.setMessage(httpResponse.getStatusLine().getReasonPhrase());

        return result;

    }

    @Override
    public final ApiResponse syncInvoke(ApiRequest apiRequest) {
        HttpUriRequest httpRequest = parseToHttpRequest(apiRequest);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpRequest);
            return parseToApiResponse(httpResponse);
        } catch (IOException e) {
            throw new SdkClientException(e);
        } finally {
            IOUtils.closeQuietly(httpResponse);
        }
    }

    @Override
    public final Future<ApiResponse> asyncInvoke(final ApiRequest apiRequest, final ApiCallBack callback) {
        return executorService.submit(new Callable<ApiResponse>() {
            @Override
            public ApiResponse call() throws Exception {
                ApiResponse result;
                try {
                    result = syncInvoke(apiRequest);
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onFailure(apiRequest, e);
                    }
                    throw e;
                }

                if (callback != null) {
                    callback.onResponse(apiRequest, result);
                }
                return result;
            }
        });
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        ApacheIdleConnectionCleaner.removeConnectionManager(connectionManager);
        connectionManager.shutdown();
        IOUtils.closeQuietly(httpClient);
    }

    private class DeafultAsyncThreadFactory implements ThreadFactory {

        private AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Aliyun_SDK_Async_ThreadPool_" + counter.incrementAndGet());
        }
    }
}
