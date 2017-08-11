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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Future;

import com.alibaba.cloudapi.sdk.core.model.ApiCallBack;
import com.alibaba.cloudapi.sdk.core.model.ApiRequest;
import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.cloudapi.sdk.core.model.BuilderParams;

/**
 * httpclient interface
 *
 * @author VK.Gao
 * @date 2017/03/01
 */
public abstract class HttpClient implements Closeable{

    public HttpClient(BuilderParams builderParams){
        init(builderParams);
    }

    protected abstract void init(BuilderParams builderParams);

    public abstract ApiResponse syncInvoke(ApiRequest request) throws IOException;

    public abstract Future<ApiResponse> asyncInvoke(ApiRequest request, ApiCallBack callback);

    public abstract void shutdown();

    @Override
    public void close() throws IOException {
        shutdown();
    }
}
