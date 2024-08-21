package com.alibaba.cloudapi.sdk.util;
import com.alibaba.cloudapi.sdk.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.constant.SdkConstant;
import com.alibaba.cloudapi.sdk.model.ApiRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by fred on 2017/7/14.
 */
public class ApiRequestMaker {
    public static void make(ApiRequest request , String appKey , String appSecret){


        /**
         * 将pathParams中的value替换掉path中的动态参数
         * 比如 path=/v2/getUserInfo/[userId]，pathParams 字典中包含 key:userId , value:10000003
         * 替换后path会变成/v2/getUserInfo/10000003
         */
        request.setPath(combinePathParam(request.getPath() , request.getPathParams()));

        /**
         *  拼接URL
         *  HTTP + HOST + PATH(With pathparameter) + Query Parameter
         */
        StringBuilder url = new StringBuilder().append(request.getScheme().getValue()).append(request.getHost()).append(request.getPath());

        if(null != request.getQuerys() && request.getQuerys().size() > 0){
            url.append("?").append(HttpCommonUtil.buildParamString(request.getQuerys()));
        }

        request.setUrl(url.toString());



        Date current = request.getCurrentDate() == null ? new Date() : request.getCurrentDate();
        //设置请求头中的时间戳
        if(null == request.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE)) {
            request.addHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_DATE, getHttpDateHeaderValue(current));
        }

        //设置请求头中的时间戳，以timeIntervalSince1970的形式
        request.addHeader(SdkConstant.CLOUDAPI_X_CA_TIMESTAMP, String.valueOf(current.getTime()));

        //请求放重放Nonce,15分钟内保持唯一,建议使用UUID
        if(request.isGenerateNonce()){
            if(null == request.getFirstHeaderValue(SdkConstant.CLOUDAPI_X_CA_NONCE)) {
                request.addHeader(SdkConstant.CLOUDAPI_X_CA_NONCE, UUID.randomUUID().toString());
            }
        }


        //设置请求头中的UserAgent
        request.addHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_USER_AGENT, SdkConstant.CLOUDAPI_USER_AGENT);

        //设置请求头中的主机地址
        request.addHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_HOST , request.getHost());

        //设置请求头中的Api绑定的的AppKey
        if(request.isNeedSignature()) {
            request.addHeader(SdkConstant.CLOUDAPI_X_CA_KEY, appKey);
        }

        //设置签名版本号
        request.addHeader(SdkConstant.CLOUDAPI_X_CA_VERSION , SdkConstant.CLOUDAPI_CA_VERSION_VALUE);

        //设置请求数据类型
        if(null == request.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE)) {
            request.addHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON);
        }
        //设置应答数据类型
        if(null == request.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT)){
            request.addHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_ACCEPT , HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON);
        }


        if (request.isNeedSignature() && !HttpCommonUtil.isEmpty(request.getSignatureMethod())) {
            request.addHeader(SdkConstant.CLOUDAPI_X_CA_SIGNATURE_METHOD, request.getSignatureMethod());
        }

        /**
         *  如果formParams不为空
         *  将Form中的内容拼接成字符串后使用UTF8编码序列化成Byte数组后加入到Request中去
         */
        if(null != request.getBody() && request.isGenerateContentMd5() && request.getBody().length >0 && null == request.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5)){
            request.addHeader(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_MD5 , SignUtil.base64AndMD5(request.getBody()));
        }

        /**
         *  将Request中的httpMethod、headers、path、queryParam、formParam合成一个字符串用hmacSha256算法双向加密进行签名
         *  签名内容放到Http头中，用作服务器校验
         */
        if(request.isNeedSignature()) {
            String signature = SignUtil.sign(request, appSecret);
            request.addHeader(SdkConstant.CLOUDAPI_X_CA_SIGNATURE, signature);
        }

        /**
         *  凑齐所有HTTP头之后，将头中的数据全部放入Request对象中
         *  Http头编码方式：先将字符串进行UTF-8编码，然后使用Iso-8859-1解码生成字符串
         */
        for(String key : request.getHeaders().keySet()){
            List<String> values = request.getHeaders().get(key);
            if(null != values && values.size() > 0){
                for(int i = 0 ; i < values.size() ; i++){
                    byte[] temp = values.get(i).getBytes(SdkConstant.CLOUDAPI_ENCODING);
                    values.set(i , new String(temp , SdkConstant.CLOUDAPI_HEADER_ENCODING));
                }
            }
            request.getHeaders().put(key , values);
        }

    }



    private static String combinePathParam(String path , Map<String , String> pathParams){
        if(pathParams == null){
            return path;
        }

        for(String key : pathParams.keySet()){
            path = path.replace("["+key+"]" , pathParams.get(key));
        }
        return path;
    }



    private static String getHttpDateHeaderValue(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }


}
