package com.alibaba.cloudapi.sdk.util;

/**
 * Created by fred on 2017/8/3.
 */
public class ObjectReference<T> {

    private T obj;

    public ObjectReference(T obj){
        this.obj = obj;
    }

    public ObjectReference(){
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
