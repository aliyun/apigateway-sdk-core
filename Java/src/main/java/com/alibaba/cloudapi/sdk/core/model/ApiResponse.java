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

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * api同步调用应答类
 *
 * @author VK.Gao
 * @date 2017/03/01
 */
public final class ApiResponse implements Serializable, Cloneable {

    private int statusCode;
    private String contentType;
    private String message;
    private Map<String, String> headers;
    private byte[] body;

    //public void parseFrom(Response response) throws IOException{
    //    this.statusCode = response.code();
    //    this.contentType = "";
    //    this.message = response.message();
    //    Map<String, List<String>> multiMap = response.headers().toMultimap();
    //
    //    // okHttp返回的multiMap中的value仅1个元素
    //    this.headers = Maps.transformValues(multiMap, new Function<List<String>, String>() {
    //        @Override
    //        public String apply(List<String> strings) {
    //
    //            if(CollectionUtils.isNotEmpty(strings)){
    //                return strings.get(0);
    //            }else{
    //                return StringUtils.EMPTY;
    //            }
    //        }
    //    });
    //    this.body = response.body().bytes();
    //}


    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", contentType='" + contentType + '\'' +
                ", message='" + message + '\'' +
                ", body=" + Arrays.toString(body) +
                ", headers=" + headers +
                '}';
    }
}
