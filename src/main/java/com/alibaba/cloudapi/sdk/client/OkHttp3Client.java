package com.alibaba.cloudapi.sdk.client;

import com.alibaba.cloudapi.sdk.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.enums.HttpConnectionModel;
import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.model.*;

import com.alibaba.cloudapi.sdk.util.ApiRequestMaker;
import com.alibaba.cloudapi.sdk.util.HttpCommonUtil;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttp3Client  extends BaseApiClient {
    OkHttpClient client;
    protected OkHttp3Client(){}
    public void init(HttpClientBuilderParams buildParam){
        if(null == buildParam){
            throw new SdkException("buildParam must not be null");
        }

        buildParam.check();

        this.appKey = buildParam.getAppKey();
        this.appSecret = buildParam.getAppSecret();
        host = buildParam.getHost();
        scheme = buildParam.getScheme();

        OkHttpClient.Builder builder = new  OkHttpClient.Builder();
        if(null != buildParam.getX509TrustManager() && null != buildParam.getSslSocketFactory()){
            builder.sslSocketFactory(buildParam.getSslSocketFactory() , buildParam.getX509TrustManager());
        }
        if(null != buildParam.getHostnameVerifier()){
            builder.hostnameVerifier(buildParam.getHostnameVerifier());
        }

        ConnectionPool connectionPool = new ConnectionPool(buildParam.getMaxIdleConnections() , buildParam.getMaxIdleTimeMillis() , TimeUnit.MILLISECONDS);
        Dispatcher dispatcher;
        if(null != buildParam.getExecutorService()){
            dispatcher = new Dispatcher(buildParam.getExecutorService());
        }
        else{
            dispatcher = new Dispatcher();
        }

        if(null != buildParam.getIdleCallback()){
            dispatcher.setIdleCallback(buildParam.getIdleCallback());
        }

        dispatcher.setMaxRequests(buildParam.getDispatchMaxRequests());
        dispatcher.setMaxRequestsPerHost(buildParam.getDispatchMaxRequestsPerHost());

        client = builder
                .connectionPool(connectionPool).dispatcher(dispatcher)
                .readTimeout(buildParam.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(buildParam.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(buildParam.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }



    @Override
    public ApiResponse sendSyncRequest(ApiRequest apiRequest)
    {
        Request request = this.buildRequest(apiRequest);
        Call call = client.newCall(request);

        try {
            return getApiResponse(apiRequest , call.execute());
        }
        catch (IOException ex){
            return new ApiResponse(500 , "Read response occur error" , ex);
        }
    }

    @Override
    public void sendAsyncRequest(final ApiRequest apiRequest , final ApiCallback apiCallback){
        final Request request = this.buildRequest(apiRequest);
        final long start  = System.currentTimeMillis();
        Call call = client.newCall(request);
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                apiCallback.onFailure(apiRequest , e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long latency = System.currentTimeMillis() - start;

                ApiResponse apiResponse = getApiResponse(apiRequest , response);
                apiResponse.addHeader("X-CA-LATENCY" , String.valueOf(latency));
                apiCallback.onResponse(apiRequest , apiResponse);
            }
        };
        call.enqueue(callback);

    }


    private Request buildRequest(ApiRequest request){
        if(request.getHttpConnectionMode() == HttpConnectionModel.SINGER_CONNECTION){
            request.setHost(host);
            request.setScheme(scheme);
        }

        ApiRequestMaker.make(request , appKey , appSecret);
        RequestBody requestBody = null;
        if(null != request.getFormParams() && request.getFormParams().size() > 0){
            requestBody = RequestBody.create(MediaType.parse(request.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE)) , HttpCommonUtil.buildParamString(request.getFormParams()));
        }
        /**
         *  如果类型为byte数组的body不为空
         *  将body中的内容MD5算法加密后再采用BASE64方法Encode成字符串，放入HTTP头中
         *  做内容校验，避免内容在网络中被篡改
         */
        else if(null != request.getBody() && request.getBody().length >0){
            requestBody = RequestBody.create(MediaType.parse(request.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE)) , request.getBody());
        }
        return new Request.Builder()
                .method(request.getMethod().getValue() , requestBody)
                .url(request.getUrl())
                .headers(getHeadersFromMap(request.getHeaders()))
                .build();
    }

    private Headers getHeadersFromMap(Map<String , List<String>> map){
        List<String> nameAndValues = new ArrayList<String>();
        for(Map.Entry<String , List<String>> entry : map.entrySet()){
            for(String value : entry.getValue()){
                nameAndValues.add(entry.getKey());
                nameAndValues.add(value);
            }
        }

        return Headers.of(nameAndValues.toArray(new String[nameAndValues.size()]));

    }

    private ApiResponse getApiResponse(ApiRequest request , Response response)throws IOException{
        ApiResponse apiResponse = new ApiResponse(response.code());
        apiResponse.setHeaders(response.headers().toMultimap());
        apiResponse.setBody(response.body().bytes());
        apiResponse.setContentType(response.header(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE , ""));

        return apiResponse;
    }

    /**
     * 暂时不支持同名header
     * @param headers
     * @return
     */
    private Map<String , String> getSimpleMapFromRequest(Headers headers){
        Map<String , List<String>> complexMap = headers.toMultimap();
        Map<String , String> simpleMap = new HashMap<String , String>();
        for(Map.Entry<String , List<String>> entry : complexMap.entrySet()){
            simpleMap.put(entry.getKey() , entry.getValue().get(0));
        }

        return simpleMap;
    }

}
