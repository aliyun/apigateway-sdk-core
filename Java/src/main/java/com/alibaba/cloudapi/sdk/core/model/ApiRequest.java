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

package com.alibaba.cloudapi.sdk.core.model;

import com.alibaba.cloudapi.sdk.core.enums.Method;
import com.alibaba.cloudapi.sdk.core.enums.ParamPosition;
import com.alibaba.cloudapi.sdk.core.enums.Scheme;
import com.alibaba.cloudapi.sdk.core.exception.SdkClientException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;

/**
 * api请求类
 *
 * @author VK.Gao
 * @date 2017/03/02
 */
public final class ApiRequest implements Serializable, Cloneable {

    public ApiRequest(Scheme scheme, Method method, String host, String path) {
        this.scheme = scheme;
        this.method = method;
        this.host = host;
        this.path = path;
    }

    public ApiRequest(Scheme scheme, Method method, String host, String path, byte[] body) {
        this.scheme = scheme;
        this.method = method;
        this.host = host;
        this.path = path;
        this.body = body;
    }

    private Scheme scheme;

    private Method method;

    private String host;

    private String path;

    private Map<String, String> pathParams = new HashMap<String, String>();

    private Map<String, String> headers = new HashMap<String, String>();

    private Map<String, String> querys = new HashMap<String, String>();

    private Map<String, String> formParams = new HashMap<String, String>();

    private byte[] body;

    public Scheme getScheme() {
        return scheme;
    }

    public Method getMethod() {
        return method;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQuerys() {
        return querys;
    }

    public Map<String, String> getFormParams() {
        return formParams;
    }

    public byte[] getBody() {
        return body;
    }

    public void addParam(String name, Object value, ParamPosition position, boolean isRequired) {
        if (value == null) {
            if (isRequired) {
                throw new SdkClientException(String.format("param %s is not nullable, please check your codes", name));
            } else {
                return;
            }
        }
        Map<String, String> targetParamMap = null;
        switch (position) {
            case HEADER: {
                targetParamMap = this.headers;
                break;
            }
            case PATH: {
                targetParamMap = this.pathParams;
                break;
            }
            case QUERY: {
                targetParamMap = this.querys;
                break;
            }
            case FORM: {
                targetParamMap = this.formParams;
                break;
            }
            default: {
                throw new SdkClientException("unknown param position: " + position);
            }
        }
        if(value instanceof String){
            targetParamMap.put(name, (String)value);
        }else{
            targetParamMap.put(name, value.toString());
        }
    }

    public void addMappedParams(Map<String, String> params, ParamPosition position){
        if(MapUtils.isNotEmpty(params)){
            for(Entry<String, String> entry : params.entrySet()){
                addParam(entry.getKey(), entry.getValue(), position, false);
            }
        }
    }

    @Deprecated
    public void addPathParam(String name, String value) {
        this.pathParams.put(name, value);
    }

    @Deprecated
    public void addHeaderParam(String name, String value) {
        this.headers.put(name, value);
    }

    @Deprecated
    public void addQueryParam(String name, String value) {
        this.querys.put(name, value);
    }

    @Deprecated
    public void addFormParam(String name, String value) {
        this.formParams.put(name, value);
    }

    @Override
    public String toString() {
        return "ApiRequest{" +
            "scheme=" + scheme +
            ", method=" + method +
            ", host='" + host + '\'' +
            ", path='" + path + '\'' +
            ", pathParams=" + pathParams +
            ", headers=" + headers +
            ", querys=" + querys +
            ", formParams=" + formParams +
            ", body=" + Arrays.toString(body) +
            '}';
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQuerys(Map<String, String> querys) {
        this.querys = querys;
    }

    public void setFormParams(Map<String, String> formParams) {
        this.formParams = formParams;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
