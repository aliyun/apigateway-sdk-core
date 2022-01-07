package com.alibaba.cloudapi.sdk.enums;

import com.alibaba.cloudapi.sdk.constant.HttpConstant;

/**
 * Created by fred on 2017/7/14.
 */
public enum HttpMethod {
    /**
     * httpGet
     */
    GET("GET", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpPost with form
     */
    POST_FORM("POST", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpPost with binary body
     */
    POST_BODY("POST", HttpConstant.CLOUDAPI_CONTENT_TYPE_STREAM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpPut with form
     */
    PUT_FORM("PUT", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpPut with binary body
     */
    PUT_BODY("PUT", HttpConstant.CLOUDAPI_CONTENT_TYPE_STREAM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpPatch with form
     */
    PATCH_FORM("PATCH", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpPatch with binary body
     */
    PATCH_BODY("PATCH", HttpConstant.CLOUDAPI_CONTENT_TYPE_STREAM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpDelete
     */
    DELETE("DELETE", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpHead
     */
    HEAD("HEAD", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON),

    /**
     * httpOptions
     */
    OPTIONS("OPTIONS", HttpConstant.CLOUDAPI_CONTENT_TYPE_FORM, HttpConstant.CLOUDAPI_CONTENT_TYPE_JSON);




    private String value;
    private String requestContentType;
    private String acceptContentType;

    HttpMethod(String value, String requestContentType, String acceptContentType) {
        this.value = value;
        this.requestContentType = requestContentType;
        this.acceptContentType = acceptContentType;
    }

    public String getValue() {
        return value;
    }

    public String getRequestContentType() {
        return requestContentType;
    }

    public String getAcceptContentType() {
        return acceptContentType;
    }
}
