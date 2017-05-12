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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;

import com.alibaba.cloudapi.sdk.core.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.core.constant.SdkConstant;
import com.alibaba.cloudapi.sdk.core.exception.SdkClientException;
import com.alibaba.cloudapi.sdk.core.model.ApiCallBack;
import com.alibaba.cloudapi.sdk.core.model.ApiRequest;
import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.cloudapi.sdk.core.model.BuilderParams;
import com.alibaba.cloudapi.sdk.core.util.SignUtil;

import com.google.common.base.Joiner;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpMethod;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * apiClient基类
 *
 * @author VK.Gao
 * @date 2017/03/02
 */
public abstract class BaseApiClient {

    private final String appKey;
    private final String appSecret;
    private final SdkHttpClient httpClient;

    public BaseApiClient(BuilderParams builderParams) {
        this.appKey = builderParams.getAppKey();
        this.appSecret = builderParams.getAppSecret();
        this.httpClient = new SdkHttpClient(builderParams);
    }

    private Request buildHttpRequest(ApiRequest apiReq) {

        /*
         * 将pathParams中的value替换掉path中的动态参数
         * 比如 path=/v2/getUserInfo/[userId]，pathParams 字典中包含 key:userId , value:10000003
         * 替换后path会变成/v2/getUserInfo/10000003
         */
        String pathWithPathParameter = combinePathParam(apiReq.getPath(), apiReq.getPathParams());


        /*
         *  拼接URL
         *  HTTP + HOST + PATH(With pathparameter) + Query Parameter
         */
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme(apiReq.getScheme().name());
        urlBuilder.host(apiReq.getHost());
        urlBuilder.encodedPath(pathWithPathParameter);
        if (MapUtils.isNotEmpty(apiReq.getQuerys())) {
            for (Map.Entry<String, String> entry : apiReq.getQuerys().entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Date current = new Date();
        /*
         * 设置请求头中的时间戳
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE, getHttpDateHeaderValue(current));

        /*
         * 设置请求头中的时间戳，以timeIntervalSince1970的形式
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_TIMESTAMP, String.valueOf(current.getTime()));

        /*
         * 请求放重放Nonce,15分钟内保持唯一,建议使用UUID
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_NONCE, UUID.randomUUID().toString());

        /*
         * 设置请求头中的UserAgent
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_USER_AGENT, SdkConstant.CLOUDAPI_USER_AGENT);

        /*
         * 设置请求头中的主机地址
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_HOST, apiReq.getHost());

        /*
         * 设置请求头中的Api绑定的的AppKey
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_KEY, appKey);

        /*
         * 设置签名版本号
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_VERSION, SdkConstant.CLOUDAPI_CA_VERSION_VALUE);

        /*
         * 设置请求数据类型
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE,
            apiReq.getMethod().getRequestContentType());

        /*
         * 设置应答数据类型
         */
        apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT, apiReq.getMethod().getAcceptContentType());

        RequestBody requestBody = null;
        if (MapUtils.isNotEmpty(apiReq.getFormParams())) {
            /*
             *  如果formParams不为空
             *  将Form中的内容以urlQueryParams的格式存放在body中(k1=v1&k2=v2&k3=v3)
             */
            Map<String, String> encodedMap = new HashMap<String, String>();
            for (Entry<String, String> entry : apiReq.getFormParams().entrySet()) {
                try {
                    String encodedValue = URLEncoder.encode(entry.getValue(),
                        SdkConstant.CLOUDAPI_ENCODING.displayName());
                    encodedMap.put(entry.getKey(), encodedValue);
                } catch (UnsupportedEncodingException e) {
                    throw new SdkClientException("format forParams to UrlEncode failed", e);
                }
            }
            Joiner.MapJoiner mapJoiner = Joiner.on('&').withKeyValueSeparator('=');
            requestBody = RequestBody.create(MediaType.parse(apiReq.getMethod().getRequestContentType()),
                mapJoiner.join(encodedMap));
        } else if (ArrayUtils.isNotEmpty(apiReq.getBody())) {
            /*
             *  如果类型为byte数组的body不为空
             *  将body中的内容MD5算法加密后再采用BASE64方法Encode成字符串，放入HTTP头中
             *  做内容校验，避免内容在网络中被篡改
             */
            requestBody = RequestBody.create(MediaType.parse(apiReq.getMethod().getRequestContentType()),
                apiReq.getBody());
            apiReq.getHeaders().put(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5,
                getMD5WithBase64Encode(apiReq.getBody()));
        }

        //modified by VK.Gao @2017-04-25: allows POST/PUT/DELETE/etc method with empty body
        if (requestBody == null && HttpMethod.permitsRequestBody(apiReq.getMethod().getName())) {
            requestBody = RequestBody.create(null, Util.EMPTY_BYTE_ARRAY);
        }

        /*
         *  将Request中的httpMethod、headers、path、queryParam、formParam合成一个字符串用hmacSha256算法双向加密进行签名
         *  签名内容放到Http头中，用作服务器校验
         */
        apiReq.getHeaders().put(SdkConstant.CLOUDAPI_X_CA_SIGNATURE, SignUtil
            .sign(apiReq.getMethod().getName(), appSecret, apiReq.getHeaders(), pathWithPathParameter,
                apiReq.getQuerys(), apiReq.getFormParams()));

        /*
         *  因http协议头使用ISO-8859-1字符集，不支持中文，所以需要将header中的中文通过UTF-8.encode()，再使用ISO-8859-1.decode()后传输
         *  对应的，服务器端需要将所有header使用ISO-8859-1.encode()，再使用UTF-8.decode()，以还原中文
         */
        for (Map.Entry<String, String> entry : apiReq.getHeaders().entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                entry.setValue(new String(entry.getValue().getBytes(SdkConstant.CLOUDAPI_ENCODING),
                    SdkConstant.CLOUDAPI_HEADER_ENCODING));
            }
        }
        Headers headers = Headers.of(apiReq.getHeaders());
        return new Request.Builder().method(apiReq.getMethod().getName(), requestBody).url(urlBuilder.build())
            .headers(headers).build();
    }

    private String combinePathParam(String path, Map<String, String> pathParams) {
        if (pathParams == null) {
            return path;
        }

        for (String key : pathParams.keySet()) {
            path = path.replace("[" + key + "]", pathParams.get(key));
        }
        return path;
    }

    private String getHttpDateHeaderValue(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    /**
     * 先进行MD5再进行Base64编码获取摘要字符串
     */
    private String getMD5WithBase64Encode(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes can not be null");
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(bytes);
            byte[] md5Result = md.digest();
            String base64Result = Base64.encodeBase64String(md5Result);
            /*
             * 正常情况下，base64的结果为24位，因与服务器有约定，在超过24位的情况下，截取前24位
             */
            return base64Result.length() > 24 ? base64Result.substring(0, 23) : base64Result;
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown algorithm MD5");
        }
    }

    protected final ApiResponse syncInvoke(ApiRequest apiRequest) {
        Request httpRequest = buildHttpRequest(apiRequest);
        try {
            Response httpResponse = httpClient.syncInvoke(httpRequest);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.parseFrom(httpResponse);
            return apiResponse;
        } catch (IOException e) {
            throw new SdkClientException(e);
        }
    }

    protected final void asyncInvoke(final ApiRequest apiRequest, final ApiCallBack callback) {
        try {
            Request httpRequest = buildHttpRequest(apiRequest);
            httpClient.asyncInvoke(httpRequest, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(apiRequest, e);
                }

                @Override
                public void onResponse(Call call, Response httpResponse) {
                    ApiResponse apiResponse = new ApiResponse();
                    try {
                        apiResponse.parseFrom(httpResponse);
                        callback.onResponse(apiRequest, apiResponse);
                    } catch (IOException e) {
                        callback.onFailure(apiRequest, e);
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(apiRequest, e);
        }

    }

}
