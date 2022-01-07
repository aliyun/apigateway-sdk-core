package com.alibaba.cloudapi.sdk.signature;

import com.alibaba.cloudapi.sdk.exception.SdkException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by guikong on 17/10/28.
 */
public interface ISinger {
    String sign(String strToSign, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException , SdkException;
}
