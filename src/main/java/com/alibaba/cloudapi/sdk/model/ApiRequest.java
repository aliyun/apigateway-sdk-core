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

package com.alibaba.cloudapi.sdk.model;

import com.alibaba.cloudapi.sdk.enums.*;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.signature.HMacSHA256SignerFactory;


public final class ApiRequest  extends ApiHttpMessage {
    public ApiRequest(HttpMethod method, String path) {
        this.scheme = scheme;
        this.host = host;
        this.method = method;
        this.path = path;
        this.body = new byte[]{};
    }

    public ApiRequest(HttpMethod method, String path, byte[] body) {
        this.scheme = scheme;
        this.host = host;
        this.method = method;
        this.path = path;
        this.body = body;
    }

    private Scheme scheme;

    private HttpMethod method;

    private String host;

    private String path;

    private String url;

    private String signatureMethod = HMacSHA256SignerFactory.METHOD;

    private Date currentDate;

    private boolean isBase64BodyViaWebsocket = false;

    private boolean isGenerateContentMd5 = true;
    private boolean isGenerateNonce = true;

    private HttpConnectionModel httpConnectionMode = HttpConnectionModel.SINGER_CONNECTION;

    private WebSocketApiType webSocketApiType = com.alibaba.cloudapi.sdk.enums.WebSocketApiType.COMMON;

    private Map<String, String> pathParams = new HashMap<String, String>();

    private Map<String, List<String>> querys = new HashMap<String, List<String>>();

    private Map<String, List<String>> formParams = new HashMap<String, List<String>>();

    private boolean isNeedSignature = true;


    public Scheme getScheme() {
        return scheme;
    }

    public HttpMethod getMethod() {
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

    public Map<String, List<String>> getQuerys() {
        return querys;
    }

    public Map<String, List<String>> getFormParams() {
        return formParams;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addParam(String name, String value, ParamPosition position, boolean isRequired) {
        if (value == null) {
            if (isRequired) {
                throw new SdkException(String.format("param %s is not nullable, please check your codes", name));
            } else {
                return;
            }
        }
        switch (position) {
            case HEAD: {
                addHeader(name , value);
                return;
            }
            case PATH: {
                this.pathParams.put(name , value);
                break;
            }
            case QUERY: {
                addParam(name , value , querys);
                break;
            }
            case BODY: {
                addParam(name , value , formParams);
                break;
            }
            default: {
                throw new SdkException("unknown param position: " + position);
            }
        }
    }


    public void addParam(String name, List<String> value, ParamPosition position, boolean isRequired) {
        if (value == null) {
            if (isRequired) {
                throw new SdkException(String.format("param %s is not nullable, please check your codes", name));
            } else {
                return;
            }
        }

        if(position == ParamPosition.PATH){
            throw new SdkException(String.format("path parameter does not support array : name is %s", name));
        }

        for(String str : value){
            addParam(name, str, position, isRequired);
        }

    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }


    public void setQuerys(Map<String, List<String>> querys) {
        this.querys = querys;
    }

    public void setFormParams(Map<String, List<String>> formParams) {
        this.formParams = formParams;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public boolean isNeedSignature() {
        return isNeedSignature;
    }

    public boolean isGenerateContentMd5() {
        return isGenerateContentMd5;
    }

    public void setGenerateContentMd5(boolean generateContentMd5) {
        isGenerateContentMd5 = generateContentMd5;
    }

    public boolean isGenerateNonce() {
        return isGenerateNonce;
    }

    public void setGenerateNonce(boolean generateNonce) {
        isGenerateNonce = generateNonce;
    }

    public void setNeedSignature(boolean needSignature) {
        isNeedSignature = needSignature;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public HttpConnectionModel getHttpConnectionMode() {
        return httpConnectionMode;
    }

    public void setHttpConnectionMode(HttpConnectionModel httpConnectionMode) {
        this.httpConnectionMode = httpConnectionMode;
    }

    public WebSocketApiType getWebSocketApiType() {
        return webSocketApiType;
    }

    public void setWebSocketApiType(WebSocketApiType webSocketApiType) {
        this.webSocketApiType = webSocketApiType;
    }

    public boolean isBase64BodyViaWebsocket() {
        return isBase64BodyViaWebsocket;
    }

    public void setBase64BodyViaWebsocket(boolean base64BodyViaWebsocket) {
        isBase64BodyViaWebsocket = base64BodyViaWebsocket;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public ApiRequest duplicate(){
        ApiRequest apiRequest = new ApiRequest(method , path , body);
        if(null != host) {
            apiRequest.host = new String(host);
        }
        if(null != url) {
            apiRequest.url = new String(url);
        }
        apiRequest.pathParams = new HashMap<String , String>();
        apiRequest.pathParams.putAll(pathParams);

        apiRequest.headers = new HashMap<String, List<String>>();
        apiRequest.headers.putAll(headers);

        apiRequest.querys =  new HashMap<String , List<String>>();
        apiRequest.querys.putAll(querys);

        apiRequest.formParams =  new HashMap<String , List<String>>();
        apiRequest.formParams.putAll(formParams);

        if(null != signatureMethod) {
            apiRequest.signatureMethod = new String(signatureMethod);
        }
        apiRequest.webSocketApiType = webSocketApiType;
        apiRequest.httpConnectionMode = httpConnectionMode;
        apiRequest.isBase64BodyViaWebsocket = isBase64BodyViaWebsocket;
        return apiRequest;

    }

}
