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

package com.alibaba.cloudapi.sdk.core.util;

import com.alibaba.cloudapi.sdk.core.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.core.constant.SdkConstant;
import com.alibaba.cloudapi.sdk.core.exception.SdkClientException;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 签名工具类
 *
 * @author fred
 * @date 16/9/7
 */
public class SignUtil {

    /**
     * 签名方法
     * 本方法将Request中的httpMethod、headers、path、queryParam、formParam合成一个字符串用hmacSha256算法双向加密进行签名
     *
     * @param method                http method
     * @param appSecret             your app secret
     * @param headerParams          http headers
     * @param pathWithParams        params builted in http path
     * @param queryParams           http query params
     * @param formParams            form params
     * @return signResults
     */
    public static String sign(String method, String appSecret, Map<String, String> headerParams, String pathWithParams, Map<String, String> queryParams, Map<String, String> formParams) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = appSecret.getBytes(SdkConstant.CLOUDAPI_ENCODING);
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));

            //将Request中的httpMethod、headers、path、queryParam、formParam合成一个字符串
            String signString = combineParamsTogether(method, headerParams, pathWithParams, queryParams, formParams);

            //对字符串进行hmacSha256加密，然后再进行BASE64编码
            byte[] signResult = hmacSha256.doFinal(signString.getBytes(SdkConstant.CLOUDAPI_ENCODING));
            return Base64.encodeBase64String(signResult);
        } catch (Exception e) {
            throw new SdkClientException("create signature failed.", e);
        }
    }

    /**
     * 将Request中的httpMethod、headers、path、queryParam、formParam合成一个字符串
     *
     */
    private static String combineParamsTogether(String method, Map<String, String> headerParams, String pathWithParams, Map<String, String> queryParams, Map<String, String> formParams) {

        StringBuilder sb = new StringBuilder();
        sb.append(method).append(SdkConstant.CLOUDAPI_LF);

        //如果有@"Accept"头，这个头需要参与签名
        if (headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT) != null) {
            sb.append(headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT));
        }
        sb.append(SdkConstant.CLOUDAPI_LF);

        //如果有@"Content-MD5"头，这个头需要参与签名
        if (headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5) != null) {
            sb.append(headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5));
        }
        sb.append(SdkConstant.CLOUDAPI_LF);

        //如果有@"Content-Type"头，这个头需要参与签名
        if (headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE) != null) {
            sb.append(headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE));
        }
        sb.append(SdkConstant.CLOUDAPI_LF);

        //签名优先读取HTTP_CA_HEADER_DATE，因为通过浏览器过来的请求不允许自定义Date（会被浏览器认为是篡改攻击）
        if (headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE) != null) {
            sb.append(headerParams.get(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE));
        }
        sb.append(SdkConstant.CLOUDAPI_LF);

        //将headers合成一个字符串
        sb.append(buildHeaders(headerParams));
        sb.append(SdkConstant.CLOUDAPI_LF);

        //将path、queryParam、formParam合成一个字符串
        sb.append(buildResource(pathWithParams, queryParams, formParams));
        return sb.toString();
    }

    /**
     * 将path、queryParam、formParam合成一个字符串
     *
     */
    private static String buildResource(String pathWithParams, Map<String, String> queryParams, Map<String, String> formParams) {
        StringBuilder result = new StringBuilder();
        result.append(pathWithParams);

        //使用TreeMap,默认按照字母排序
        TreeMap<String, String> parameter = new TreeMap<String, String>();
        if (MapUtils.isNotEmpty(queryParams)) {
            parameter.putAll(queryParams);
        }
        if (MapUtils.isNotEmpty(formParams)) {
            parameter.putAll(formParams);
        }

        if (parameter.size() > 0) {
            result.append("?");

            // bugfix by VK.Gao@2017-05-03: "kv separator should be ignored while value is empty, ex. k1=v1&k2&k3=v3&k4"
            List<String> comboMap = new ArrayList<String>();
            for(Entry<String, String> entry : parameter.entrySet()){
                String comboResult = entry.getKey() + (StringUtils.isNotEmpty(entry.getValue())? "="+entry.getValue() : StringUtils.EMPTY);
                comboMap.add(comboResult);
            }
            Joiner joiner = Joiner.on("&");
            result.append(joiner.join(comboMap));
        }
        return result.toString();
    }

    /**
     * 将headers合成一个字符串
     * 需要注意的是，HTTP头需要按照字母排序加入签名字符串
     * 同时所有加入签名的头的列表，需要用逗号分隔形成一个字符串，加入一个新HTTP头@"X-Ca-Signature-Headers"
     *
     */
    private static String buildHeaders(Map<String, String> headers) {

        if (MapUtils.isNotEmpty(headers)) {
            // 筛选出需要签名的key
            Predicate<String> signFilter = new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input.startsWith(SdkConstant.CLOUDAPI_CA_HEADER_TO_SIGN_PREFIX_SYSTEM);
                }
            };

            // 使用TreeMap,默认按照字母排序
            Map<String, String> headersToSign = new TreeMap<String, String>(Maps.filterKeys(headers, signFilter));

            // 所有加入签名的头的列表，需要用逗号分隔形成一个字符串，加入一个新HTTP头@"X-Ca-Signature-Headers"
            String signHeaders = Joiner.on(',').join(headersToSign.keySet());
            headers.put(SdkConstant.CLOUDAPI_X_CA_SIGNATURE_HEADERS, signHeaders);

            // 拼装签名内容
            Joiner.MapJoiner joiner = Joiner.on(SdkConstant.CLOUDAPI_LF).withKeyValueSeparator(':');
            return joiner.join(headersToSign);

        } else {
            return StringUtils.EMPTY;
        }


    }

}
