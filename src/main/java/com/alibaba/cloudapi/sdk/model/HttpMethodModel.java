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

import com.alibaba.cloudapi.sdk.enums.HttpMethod;

public class HttpMethodModel {
    public HttpMethodModel(HttpMethod enumMethod){
        this.value = enumMethod.getValue();
        this.requestContentType = enumMethod.getRequestContentType();
        this.acceptContentType = enumMethod.getAcceptContentType();
    }

    public HttpMethodModel(String value , String requestContentType , String acceptContentType){
        this.value = value;
        this.requestContentType = requestContentType;
        this.acceptContentType = acceptContentType;
    }

    private String value;
    private String requestContentType;
    private String acceptContentType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRequestContentType() {
        return requestContentType;
    }

    public void setRequestContentType(String requestContentType) {
        this.requestContentType = requestContentType;
    }

    public String getAcceptContentType() {
        return acceptContentType;
    }

    public void setAcceptContentType(String acceptContentType) {
        this.acceptContentType = acceptContentType;
    }
}
