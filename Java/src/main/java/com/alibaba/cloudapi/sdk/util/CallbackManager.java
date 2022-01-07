package com.alibaba.cloudapi.sdk.util;

import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.model.ApiContext;
import com.alibaba.cloudapi.sdk.model.ApiResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fred on 2017/8/7.
 */
public class CallbackManager implements Runnable{
    private static final Log LOG = LogFactory.getLog(CallbackManager.class);
    final int CHECK_EXPIRE_INTERVAL = 500;
    int requestExpiredTime = 10000;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    ConcurrentHashMap<Integer , ApiContext> callbacks = new ConcurrentHashMap<Integer, ApiContext>();
    //处理应答的线程池数量，一般作为客户端，一个线程就够了，而且一个线程能保证执行的顺序和收到应答的顺序一致
    static ExecutorService fixThreadPool;

    public CallbackManager(int callbackThreadPoolCount , int requestExpiredTimep){
        requestExpiredTime = requestExpiredTimep;
        fixThreadPool = Executors.newFixedThreadPool(callbackThreadPoolCount);
    }

    public void add(Integer seq , ApiContext context){
        callbacks.put(seq , context);
        if(countDownLatch!= null && countDownLatch.getCount() == 1){
            countDownLatch.countDown();
        }
    }

    public ApiContext getContext(Integer seq){
        return callbacks.get(seq);
    }

    public void callback(int seq , final ApiResponse response){
        final ApiContext apiContext = callbacks.remove(seq);
        if(null != apiContext){
            fixThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        apiContext.getCallback().onResponse(apiContext.getRequest(), response);
                    }
                    catch (Exception ex){
                        apiContext.getCallback().onFailure(apiContext.getRequest(), new SdkException("Callback failure" , ex));
                    }
                }
            });

        }
    }

    @Override
    public void run() {
        Long current;
        while(true){
            current = (new Date()).getTime();
            ArrayList<Integer> toBeRemove = new ArrayList<Integer>();
            for (Map.Entry<Integer , ApiContext> callback : callbacks.entrySet()){
                ApiContext context = callback.getValue();
                if((current -  context.getStartTime()) > requestExpiredTime){
                    context.getCallback().onFailure(context.getRequest() , new SdkException("Get Response Timeout"));
                    toBeRemove.add(callback.getKey());
                }
            }

            for(Integer key : toBeRemove){
                callbacks.remove(key);
            }

            try {
                if(callbacks.size() == 0) {
                    countDownLatch = new CountDownLatch(1);
                    countDownLatch.await();
                }

                Thread.sleep(CHECK_EXPIRE_INTERVAL);
            }
            catch (Exception ex){
                LOG.error("Check callback expired" , ex);
            }
        }
    }
}
