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

package com.alibaba.cloudapi.sdk.client;

import com.alibaba.cloudapi.sdk.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.enums.HttpConnectionModel;
import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.model.ApiCallback;
import com.alibaba.cloudapi.sdk.model.ApiRequest;
import com.alibaba.cloudapi.sdk.model.ApiResponse;
import com.alibaba.cloudapi.sdk.model.HttpClientBuilderParams;
import com.alibaba.cloudapi.sdk.util.ApiRequestMaker;
import com.alibaba.cloudapi.sdk.util.HttpCommonUtil;
import com.alibaba.cloudapi.sdk.util.SignUtil;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ApacheHttpClient extends BaseApiClient {
    //protected static final Logger logger = LoggerFactory.getLogger(ApacheHttpClient.class);

    private static final int DEFAULT_THREAD_KEEP_ALIVE_TIME = 60;

    private ExecutorService executorService;
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;
    private DefaultHttpRequestRetryStrategy retryStrategy;

    protected ApacheHttpClient(){}

    public void init(final HttpClientBuilderParams params) {
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true).setSoReuseAddress(true)
                .setSoTimeout(Timeout.ofMilliseconds(params.getReadTimeout())).build();


        Registry<ConnectionSocketFactory> registry = getRegistry();

        if(params.getRegistry() != null){
            registry = params.getRegistry();
        }

        connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom().build());
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setMaxTotal(params.getDispatchMaxRequests());
        connectionManager.setDefaultMaxPerRoute(params.getDispatchMaxRequestsPerHost());

        retryStrategy = params.getRequestRetryHandler();
        if(null == retryStrategy){
            retryStrategy = new DefaultHttpRequestRetryStrategy() {
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount > 2) {
                        return false;
                    }

                    if (exception instanceof NoHttpResponseException) {
                        return true;
                    }
                    return false;
                }
            };
        }


        RequestConfig defaultConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(params.getReadTimeout()))
                .build();
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(params.getConnectionTimeout()))
                .setSocketTimeout(Timeout.ofMilliseconds(params.getReadTimeout()))
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);





        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(defaultConfig)
                .setRetryStrategy(retryStrategy)
                .evictIdleConnections(TimeValue.ofMilliseconds(params.getIntevictIdleConnectionsIntervalMillis()))
                .build();
        this.appKey = params.getAppKey();
        this.appSecret = params.getAppSecret();
        host = params.getHost();
        scheme = params.getScheme();


        if (params.getExecutorService() == null) {
            executorService = new ThreadPoolExecutor(0, params.getDispatchMaxRequests(), DEFAULT_THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                    new DeafultAsyncThreadFactory());
        } else {
            executorService = params.getExecutorService();
        }


    }

    private static Registry<ConnectionSocketFactory> getRegistry() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        try {
            registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE).build();
            registryBuilder.register("https",new SSLConnectionSocketFactory(SSLContext.getDefault(), new DefaultHostnameVerifier()));

        } catch (Exception e) {
            throw new RuntimeException("HttpClientUtil init failure !", e);
        }
        return registryBuilder.build();
    }

    private static Registry<ConnectionSocketFactory> getNoVerifyRegistry() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        try {
            registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE).build();
            registryBuilder.register(
                    "https",
                    new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(
                            KeyStore.getInstance(KeyStore.getDefaultType()), new TrustStrategy() {
                                @Override
                                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                    return true;
                                }
                            }).build(),
                            new HostnameVerifier() {
                                @Override
                                public boolean verify(String paramString, SSLSession paramSSLSession) {
                                    return true;
                                }
                            }));

        } catch (Exception e) {
            throw new RuntimeException("HttpClientUtil init failure !", e);
        }
        return registryBuilder.build();
    }


    private ClassicHttpRequest buildRequest(ApiRequest apiRequest) {
        if(apiRequest.getHttpConnectionMode() == HttpConnectionModel.SINGER_CONNECTION){
            apiRequest.setHost(host);
            apiRequest.setScheme(scheme);
        }

        ApiRequestMaker.make(apiRequest , appKey , appSecret);
        ClassicRequestBuilder builder = ClassicRequestBuilder.create(apiRequest.getMethod().name());

        /*
         *  拼接URL
         *  HTTP + HOST + PATH(With pathparameter) + Query Parameter
         */
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(apiRequest.getScheme().name());
            uriBuilder.setHost(apiRequest.getHost());
            uriBuilder.setPath(apiRequest.getPath());
            if (!HttpCommonUtil.isEmpty(apiRequest.getQuerys())) {
                for (Map.Entry<String, List<String>> entry : apiRequest.getQuerys().entrySet()) {
                    for(String value : entry.getValue()){
                        uriBuilder.addParameter(entry.getKey(), value);
                    }

                }
            }
            builder.setUri(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new SdkException("build http request uri failed", e);
        }

        EntityBuilder bodyBuilder = EntityBuilder.create();

        //设置请求数据类型
        if(null == apiRequest.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE)) {
            bodyBuilder.setContentType(ContentType.APPLICATION_JSON);
        }
        else{
            bodyBuilder.setContentType(ContentType.parse(apiRequest.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE)));
        }


        if (!HttpCommonUtil.isEmpty(apiRequest.getFormParams())) {
            /*
             *  如果formParams不为空
             *  将Form中的内容以urlQueryParams的格式存放在body中(k1=v1&k2=v2&k3=v3)
             */
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();

            for (Entry<String, List<String>> entry : apiRequest.getFormParams().entrySet()) {
                for(String value : entry.getValue()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            bodyBuilder.setParameters(paramList);
            builder.setEntity(bodyBuilder.build());
        } else if (!HttpCommonUtil.isEmpty(apiRequest.getBody())) {
            bodyBuilder.setBinary(apiRequest.getBody());
            builder.setEntity(bodyBuilder.build());
        }

        for (Map.Entry<String, List<String>> entry : apiRequest.getHeaders().entrySet()) {
            for(String value : entry.getValue()){
                builder.addHeader(entry.getKey(), value);
            }
        }

        return builder.build();
    }

    private ApiResponse parseToApiResponse(HttpResponse httpResponse) throws IOException {
        ApiResponse result = new ApiResponse(httpResponse.getCode());

        // headers
        result.setHeaders(new HashMap<String, List<String>>());
        for (Header header : httpResponse.getHeaders()) {
            List<String> values = result.getHeaders().get(header.getName());

            if(values == null){
                values = new ArrayList<String>();
            }

            values.add(header.getValue());
            //logger.info("header.getName().toLowerCase() : " + header.getName().toLowerCase());
            //logger.info("header.getValue() : " + header.getValue());
            result.getHeaders().put(header.getName().toLowerCase() , values);
        }

        // message
        result.setMessage(httpResponse.getReasonPhrase());


        if(((CloseableHttpResponse)httpResponse).getEntity() != null){
            // content type
            String contentType = ((CloseableHttpResponse)httpResponse).getEntity().getContentType();
            if(contentType != null){
                result.setContentType(contentType);
            }
            else
            {
                result.setContentType(HttpConstant.CLOUDAPI_CONTENT_TYPE_TEXT);
            }

            // body
            result.setBody(EntityUtils.toByteArray(((CloseableHttpResponse)httpResponse).getEntity()));

            String contentMD5 = result.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CA_CONTENT_MD5);
            if(null != contentMD5 && !"".equals(contentMD5)){
                String localContentMd5 = SignUtil.base64AndMD5(result.getBody());
                if(!contentMD5.equalsIgnoreCase(localContentMd5)){
                    throw new SdkException("Server Content MD5 does not match body content , server md5 is " + contentMD5 + "  local md5 is " + localContentMd5 + " body is " + new String(result.getBody()));
                }
            }
        }else{
            String contentTypeStr = result.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE);
            if(null == contentTypeStr){
                contentTypeStr = HttpConstant.CLOUDAPI_CONTENT_TYPE_TEXT;
            }
            result.setContentType(contentTypeStr);
        }


        return result;

    }

    @Override
    public final ApiResponse sendSyncRequest(ApiRequest apiRequest) {
        ClassicHttpRequest httpRequest = buildRequest(apiRequest);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpRequest);
            return parseToApiResponse(httpResponse);
        } catch (IOException e) {
            throw new SdkException(e);
        } finally {
            HttpCommonUtil.closeQuietly(httpResponse);
        }
    }

    @Override
    public final void sendAsyncRequest(final ApiRequest apiRequest, final ApiCallback apiCallback) {
        final long start  = System.currentTimeMillis();
        executorService.submit(new Callable<ApiResponse>() {
            @Override
            public ApiResponse call() throws Exception {
                ApiResponse apiResponse;
                try {
                    apiResponse = sendSyncRequest(apiRequest);
                } catch (Exception e) {
                    if (apiCallback != null) {
                        apiCallback.onFailure(apiRequest, e);
                    }
                    throw e;
                }

                if (apiCallback != null) {
                    long latency = System.currentTimeMillis() - start;
                    apiResponse.addHeader("X-CA-LATENCY" , String.valueOf(latency));
                    apiCallback.onResponse(apiRequest , apiResponse);

                }

                return apiResponse;
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
        HttpCommonUtil.closeQuietly(httpClient);
    }

    private class DeafultAsyncThreadFactory implements ThreadFactory {

        private AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Aliyun_SDK_Async_ThreadPool_" + counter.incrementAndGet());
        }
    }
}
