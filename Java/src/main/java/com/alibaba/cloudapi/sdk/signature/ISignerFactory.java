package com.alibaba.cloudapi.sdk.signature;

import com.alibaba.cloudapi.sdk.exception.SdkException;

/**
 * Created by guikong on 17/10/28.
 */
public interface ISignerFactory {
    ISinger getSigner() throws SdkException;
}
