package com.alibaba.cloudapi.sdk.util;

import com.alibaba.cloudapi.sdk.client.WebSocketApiClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by fred on 2017/8/8.
 */
public class HeartBeatManager implements Runnable{
    private static final Log LOG = LogFactory.getLog(HeartBeatManager.class);
    WebSocketApiClient webSocketApiClient;
    int heartbeatInterval = 25000;
    boolean isStop = false;

    public HeartBeatManager(WebSocketApiClient webSocketApiClientp , int interval){
        this.webSocketApiClient = webSocketApiClientp;
        this.heartbeatInterval = interval;
        isStop = false;
    }

    public void stop(){
        isStop = true;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(heartbeatInterval);
                if(isStop == true){
                    break;
                }
                webSocketApiClient.sendHeatbeart();
            }
            catch (Exception ex){
                LOG.error("SEND HEARTBEAT FAILED" , ex);
            }
        }
    }
}
