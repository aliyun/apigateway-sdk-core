/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.alibaba.cloudapi.sdk.client;



import com.alibaba.cloudapi.sdk.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.constant.SdkConstant;
import com.alibaba.cloudapi.sdk.enums.Scheme;
import com.alibaba.cloudapi.sdk.enums.WebSocketApiType;
import com.alibaba.cloudapi.sdk.enums.WebSocketConnectStatus;
import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.model.ApiCallback;
import com.alibaba.cloudapi.sdk.model.ApiContext;
import com.alibaba.cloudapi.sdk.model.ApiRequest;
import com.alibaba.cloudapi.sdk.model.ApiResponse;
import com.alibaba.cloudapi.sdk.model.ApiWebSocketListner;
import com.alibaba.cloudapi.sdk.model.WebSocketApiRequest;
import com.alibaba.cloudapi.sdk.model.WebSocketClientBuilderParams;
import com.alibaba.cloudapi.sdk.util.*;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by fred on 2017/7/14.
 */
public class WebSocketApiClient extends BaseApiClient {
    final ObjectReference<WebSocket> webSocketRef = new ObjectReference<WebSocket>();
    final ObjectReference<CountDownLatch> connectLatch = new ObjectReference<CountDownLatch>();
    final ObjectReference<CountDownLatch> registerLatch = new ObjectReference<CountDownLatch>();
    final ObjectReference<Boolean> registerCommandSuccess = new ObjectReference<Boolean>();
    final ObjectReference<String> errorMessage = new ObjectReference<String>();
    private WebSocketApiClient instance;
    String websocketUrl;
    OkHttpClient client;
    Request connectRequest;
    ApiWebSocketListner apiWebSocketListner;
    AtomicInteger seq = new AtomicInteger(0);
    CallbackManager callbackManager;
    Thread callbackThread;
    WebSocketConnectStatus status = WebSocketConnectStatus.LOST_CONNECTION;
    WebSocketListener webSocketListener;
    HeartBeatManager heartBeatManager;
    Thread heartbeatThread;
    int port = 8080;
    String deviceId;
    String connectionCredential = "";
    ApiRequest lastRegisterReqeust;
    ApiCallback lastRegisterCallback;
    boolean isRegister = false;
    int heartBeatInterval = 25000;
    Object connectionLock = new Object();
    private static ObjectMapper objectMapper= new ObjectMapper();;



    protected String getDeviceId(){
        return deviceId;
    }

    public boolean isRegister(){
        return isRegister;
    }

    public boolean isOnline(){
        return isRegister && (status == WebSocketConnectStatus.CONNECTED);
    }

    protected WebSocketApiClient(){}

    protected void init(WebSocketClientBuilderParams params){
        if(null == params){
            throw new SdkException("WebSocketClientBuilderParams must not be null");
        }

        params.check();

        appKey = params.getAppKey();
        appSecret = params.getAppSecret();
        deviceId = generateDeviceSum();

        if(params.getScheme() != null){
            if(params.getScheme() == Scheme.WEBSOCKET || params.getScheme() == Scheme.WEBSOCKET_SECURITY){
                scheme = params.getScheme();
            }
            else{
                throw new SdkException("Scheme should be WEBSOCKET or WEBSOCKET_SECURITY");
            }
        }
        else{
            scheme = Scheme.WEBSOCKET;
        }

        websocketUrl = scheme.getValue() + params.getHost();
        if(scheme == Scheme.WEBSOCKET){
            port = 8080;
        }else {
            port = 8443;
        }


        if(port != 80){
            websocketUrl = websocketUrl + ":" + port;
        }
        host = params.getHost();
        scheme = Scheme.WEBSOCKET;

        OkHttpClient.Builder builder = new OkHttpClient.Builder().readTimeout(params.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(params.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(params.getConnectionTimeout(), TimeUnit.MILLISECONDS);
        if(params.isSkipSsCheck()){
            builder.sslSocketFactory(SSLSkipCheck.getSSLSocketFactory(), SSLSkipCheck.getX509TrustManager())
                    .hostnameVerifier(SSLSkipCheck.getHostnameVerifier());
        }

        client = builder.build();
        connectRequest = new Request.Builder().url(websocketUrl).build();
        apiWebSocketListner = params.getApiWebSocketListner();
        callbackManager = new CallbackManager(params.getCallbackThreadPoolCount() , params.getRequestExpiredTime());
        callbackThread=new Thread(callbackManager);
        callbackThread.start();
        this.connect();
        registerCommandSuccess.setObj(false);
        errorMessage.setObj("");
        instance = this;
        isInit = true;
    }

    public WebSocketConnectStatus getStatus() {
        return status;
    }

    /**
     * 在外部知道网络不可用的情况下,请调用本接口设置网络状态,避免无所谓的重试
     * @param status
     */
    public void setStatus(WebSocketConnectStatus status) {
        this.status = status;
    }

    public void connect(){
        if (null == connectLatch.getObj()) {
            connectLatch.setObj(new CountDownLatch(1));
        }

        if(null == webSocketListener){
            webSocketListener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    webSocketRef.setObj(webSocket);
                    status = WebSocketConnectStatus.CONNECTED;

                    registerLatch.setObj(new CountDownLatch(1));
                    String registerCommand = SdkConstant.CLOUDAPI_COMMAND_REGISTER_REQUEST + "#" + deviceId;
                    webSocketRef.getObj().send(registerCommand);

                    if (null != connectLatch.getObj()) {
                        connectLatch.getObj().countDown();
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    if(null  == text || "".equalsIgnoreCase(text)) {
                        return;
                    }else if(text.length() > 2 && text.startsWith(SdkConstant.CLOUDAPI_COMMAND_HEART_BEAT_RESPONSE)){
                        if(!connectionCredential.equalsIgnoreCase(text.substring(3))){
                            reSendRegister();
                        }

                        return;

                    }else  if (SdkConstant.CLOUDAPI_COMMAND_OVER_FLOW_BY_SECOND.equalsIgnoreCase(text)) {
                        //overflow by server
                        close();
                        return;
                    }else if (SdkConstant.CLOUDAPI_COMMAND_CONNECTION_RUNS_OUT.equalsIgnoreCase(text)) {
                        //bye by server
                        close();
                        return;
                    }else if(text.length() > 2 && text.startsWith(SdkConstant.CLOUDAPI_COMMAND_REGISTER_FAIL_REQUEST)){
                        registerCommandSuccess.setObj(false);
                        String responseObject[] = text.split("#");
                        errorMessage.setObj(responseObject[1]);
                        if(null != registerLatch.getObj()) {
                            registerLatch.getObj().countDown();
                        }

                        if (null != heartBeatManager) {
                            heartBeatManager.stop();
                        }

                        return;
                    }else if(text.length() > 2 && text.startsWith(SdkConstant.CLOUDAPI_COMMAND_REGISTER_SUCCESS_RESPONSE)){
                        registerCommandSuccess.setObj(true);
                        String responseObject[] = text.split("#");
                        connectionCredential = responseObject[1];
                        heartBeatInterval = Integer.parseInt(responseObject[2]);

                        if(null != registerLatch.getObj()) {
                            registerLatch.getObj().countDown();
                        }

                        if (null != heartBeatManager) {
                            heartBeatManager.stop();
                        }
                        heartBeatManager = new HeartBeatManager(instance, heartBeatInterval);
                        heartbeatThread = new Thread(heartBeatManager);
                        heartbeatThread.start();

                        if(isRegister) {
                            reSendRegister();
                        }


                        return;
                    }else if(text.length() > 2 && text.startsWith(SdkConstant.CLOUDAPI_COMMAND_NOTIFY_REQUEST)){
                        String message  = text.substring(3);
                        apiWebSocketListner.onNotify(message);
                        if(status == WebSocketConnectStatus.CONNECTED && webSocketRef.getObj() != null){
                            webSocketRef.getObj().send(SdkConstant.CLOUDAPI_COMMAND_NOTIFY_RESPONSE);
                        }
                        return ;

                    }else if(text.length() > 2 && !text.startsWith("{") && "#".equalsIgnoreCase(text.substring(3 ,4))){
                        //兼容以后新版本信令
                        return;
                    }
                    else{
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readValue(text, JsonNode.class);
                            ApiResponse response = new ApiResponse(jsonNode);
                            String seqStr = response.getFirstHeaderValue(SdkConstant.CLOUDAPI_X_CA_SEQ);
                            Integer seq = Integer.parseInt(seqStr);

                            ApiContext context = callbackManager.getContext(seq);
                            WebSocketApiType type = context.getRequest().getWebSocketApiType();
                            if (null != context && type != WebSocketApiType.COMMON) {
                                postSendWebsocketCommandApi(type , response);
                            }

                            callbackManager.callback(seq, response);
                        }
                        catch (Exception ex){
                            apiWebSocketListner.onFailure(ex , new ApiResponse(508 , "Call back occur error , text is " + text , ex));
                        }

                    }


                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    webSocketRef.setObj(null);
                    reconnect();
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    try {
                        ApiResponse apiResponse;
                        if (null != response) {
                            apiResponse = new ApiResponse(response.code());
                            apiResponse.setMessage(response.message());
                        } else {
                            apiResponse = new ApiResponse(505);
                            apiResponse.setMessage("WebSocket inner failed");
                        }

                        apiResponse.setEx(new SdkException(t));
                        apiWebSocketListner.onFailure(t, apiResponse);

                        if (null != t) {
                            /**
                             * 连接不上
                             */
                            if (t instanceof ConnectException || t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                                if (null != connectLatch.getObj()) {
                                    connectLatch.getObj().countDown();
                                }
                                try {
                                    Thread.sleep(500);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                reconnect();
                            }

                            /**
                             * 被动断开连接
                             */
                            else if (t instanceof SocketException || t instanceof EOFException) {
                                if (null == connectLatch.getObj()) {
                                    connectLatch.setObj(new CountDownLatch(1));
                                }

                                try {
                                    Thread.sleep(500);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                               reconnect();
                            }
                        }
                    }
                    catch (Exception ex){
                        apiWebSocketListner.onFailure(ex , new ApiResponse(507 , "Failure block" , ex));
                    }
                 }
            };
        }


        //建立连接
        client.newWebSocket(connectRequest, webSocketListener);


    }

    private void reSendRegister(){
        sendAsyncRequest(lastRegisterReqeust, lastRegisterCallback);
    }

    private void reconnect(){
        status = WebSocketConnectStatus.LOST_CONNECTION;

        if (null != heartBeatManager) {
            heartBeatManager.stop();
        }
        connect();


    }

    private void close() {

        synchronized (connectionLock) {
            try {
                connectLatch.setObj(new CountDownLatch(1));

                if (null != heartBeatManager) {
                    heartBeatManager.stop();
                }

                if (null != webSocketRef.getObj()) {
                    Thread.sleep(1000);
                    webSocketRef.getObj().close(1000, "Reconnect");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }


    @Override
    protected ApiResponse sendSyncRequest(ApiRequest apiRequest)
    {
        throw new SdkException("Not support sending sync request via websocket channel");
    }

    @Override
    protected void sendAsyncRequest(final ApiRequest apiRequest , final ApiCallback apiCallback){
        checkIsInit();

        synchronized (connectionLock) {
            if (null != connectLatch.getObj() && connectLatch.getObj().getCount() == 1) {
                try {
                    connectLatch.getObj().await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    throw new SdkException("WebSocket connect server failed ", ex);
                } finally {
                    connectLatch.setObj(null);
                }
            }

            if (status == WebSocketConnectStatus.LOST_CONNECTION) {
                apiCallback.onFailure(apiRequest, new SdkException("WebSocket conection lost , connecting"));
                return;
            }

            if (WebSocketApiType.COMMON != apiRequest.getWebSocketApiType()) {
                if(!preSendWebsocketCommandApi(apiRequest , apiCallback)) {
                    return;
                }
            }

            Integer seqNumber = seq.getAndIncrement();
            apiRequest.addHeader(SdkConstant.CLOUDAPI_X_CA_SEQ, seqNumber.toString());
            callbackManager.add(seqNumber, new ApiContext(apiCallback, apiRequest));
            String request = buildRequest(apiRequest);
             webSocketRef.getObj().send(request);
        }

    }

    private boolean preSendWebsocketCommandApi(final ApiRequest apiRequest , final ApiCallback apiCallback){
        if(WebSocketApiType.REGISTER == apiRequest.getWebSocketApiType()) {
            try {
                if (null != registerLatch.getObj() && !registerLatch.getObj().await(10, TimeUnit.SECONDS)) {
                    Thread.sleep(5000);
                    close();
                    apiCallback.onFailure(apiRequest, new SdkException("WebSocket conection lost , connecting"));
                    return false;
                }
            } catch (InterruptedException ex) {
                throw new SdkException("WebSocket register failed ", ex);
            } finally {
                registerLatch.setObj(null);
            }

            if (!registerCommandSuccess.getObj()) {
                apiCallback.onFailure(null, new SdkException("Register Comand return error :" + errorMessage.getObj()));
                return false;
            }

            lastRegisterReqeust = apiRequest.duplicate();
            lastRegisterCallback = apiCallback;

        }

        apiRequest.addHeader(SdkConstant.CLOUDAPI_X_CA_WEBSOCKET_API_TYPE, apiRequest.getWebSocketApiType().toString());


        return true;
    }

    private void postSendWebsocketCommandApi(WebSocketApiType type , ApiResponse response){
        if(WebSocketApiType.REGISTER == type){
            if(200 == response.getCode()){
                isRegister = true;
            }
        }
        if(WebSocketApiType.UNREGISTER == type){
            if (null != heartBeatManager) {
                heartBeatManager.stop();
            }
            lastRegisterReqeust = null;
            lastRegisterCallback = null;
            isRegister = false;
        }
    }



    public void sendHeatbeart(){
        if(isInit == true && status == WebSocketConnectStatus.CONNECTED && webSocketRef.getObj() != null){
            webSocketRef.getObj().send(SdkConstant.CLOUDAPI_COMMAND_HEART_BEAT_REQUEST);
        }
    }


    private String generateDeviceSum(){
        return generateDeviceId() + "@" + appKey;
    }

    private String generateDeviceId(){
        return UUID.randomUUID().toString().replace("-" , "").substring(0 , 8);
    }


    private String buildRequest(ApiRequest apiRequest){
        try {
            apiRequest.setHost(host);
            apiRequest.setScheme(scheme);
            ApiRequestMaker.make(apiRequest, appKey, appSecret);


            WebSocketApiRequest webSocketApiRequest = new WebSocketApiRequest();
            webSocketApiRequest.setHost(host);
            webSocketApiRequest.setPath(apiRequest.getPath());
            webSocketApiRequest.setMethod(apiRequest.getMethod().getValue());
            webSocketApiRequest.setQuerys(apiRequest.getQuerys());
            webSocketApiRequest.setHeaders(apiRequest.getHeaders());
            webSocketApiRequest.setIsBase64(apiRequest.isBase64BodyViaWebsocket() == true ? 1 : 0);
            MediaType bodyType = MediaType.parse(apiRequest.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE));

            if (null != apiRequest.getFormParams() && apiRequest.getFormParams().size() > 0) {
                webSocketApiRequest.setBody(HttpCommonUtil.buildParamString(apiRequest.getFormParams()));
            } else if (null != apiRequest.getBody()) {
                webSocketApiRequest.setBody(new String(apiRequest.getBody(), bodyType.charset(SdkConstant.CLOUDAPI_ENCODING)));
            }

            if (apiRequest.isBase64BodyViaWebsocket()) {
                webSocketApiRequest.setBody(Base64.encodeBase64String(apiRequest.getBody()));
            }

            return objectMapper.writeValueAsString(webSocketApiRequest);
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }



}
