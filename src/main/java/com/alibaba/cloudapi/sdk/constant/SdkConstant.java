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

package com.alibaba.cloudapi.sdk.constant;

import java.nio.charset.Charset;

/**
 * sdk相关常量
 *
 * @author fred
 * @date 2016/12/19
 */
public class SdkConstant {
    //签名Header
    public static final String CLOUDAPI_X_CA_SIGNATURE = "x-ca-signature";
    //所有参与签名的Header
    public static final String CLOUDAPI_X_CA_SIGNATURE_HEADERS = "x-ca-signature-headers";
    //请求时间戳
    public static final String CLOUDAPI_X_CA_TIMESTAMP = "x-ca-timestamp";
    //请求放重放Nonce,15分钟内保持唯一,建议使用UUID
    public static final String CLOUDAPI_X_CA_NONCE = "x-ca-nonce";
    //APP KEY
    public static final String CLOUDAPI_X_CA_KEY = "x-ca-key";
    //APP KEY
    public static final String CLOUDAPI_X_CA_ERROR_MESSAGE = "x-ca-error-message";
    //APP KEY
    public static final String CLOUDAPI_X_CA_SEQ = "x-ca-seq";
    //签名版本号
    public static final String CLOUDAPI_X_CA_VERSION = "CA_VERSION";

    //编码UTF-8
    public static final Charset CLOUDAPI_ENCODING = Charset.forName("UTF-8");

    //Header头的编码
    public static final Charset CLOUDAPI_HEADER_ENCODING = Charset.forName("ISO-8859-1");

    //签名算法
    public static final String CLOUDAPI_X_CA_SIGNATURE_METHOD = "X-Ca-Signature-Method";

    public static final String CLOUDAPI_X_CA_WEBSOCKET_API_TYPE = "x-ca-websocket_api_type";


    //UserAgent
    public static final String CLOUDAPI_USER_AGENT = "ALIYUN-ANDROID-DEMO";
    //换行符
    public static final String CLOUDAPI_LF = "\n";

    //参与签名的系统Header前缀,只有指定前缀的Header才会参与到签名中
    public static final String CLOUDAPI_CA_HEADER_TO_SIGN_PREFIX_SYSTEM = "x-ca-";

    //心跳信令命令字
    public static final String CLOUDAPI_COMMAND_HEART_BEAT_REQUEST = "H1";

    public static final String CLOUDAPI_COMMAND_HEART_BEAT_RESPONSE = "HO";

    public static final String CLOUDAPI_COMMAND_REGISTER_REQUEST = "RG";

    public static final String CLOUDAPI_COMMAND_REGISTER_SUCCESS_RESPONSE = "RO";

    public static final String CLOUDAPI_COMMAND_REGISTER_FAIL_REQUEST = "RF";

    public static final String CLOUDAPI_COMMAND_NOTIFY_REQUEST = "NF";

    public static final String CLOUDAPI_COMMAND_NOTIFY_RESPONSE = "NO";

    public static final String CLOUDAPI_COMMAND_OVER_FLOW_BY_SECOND = "OS";

    public static final String CLOUDAPI_COMMAND_CONNECTION_RUNS_OUT = "CR";

    //签名版本号
    public static final String CLOUDAPI_CA_VERSION_VALUE = "1.1.2";
}
