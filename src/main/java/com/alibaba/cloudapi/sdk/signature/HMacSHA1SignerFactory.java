package com.alibaba.cloudapi.sdk.signature;


import com.alibaba.cloudapi.sdk.constant.SdkConstant;
import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.util.HttpCommonUtil;
import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by guikong on 17/10/28.
 */
public class HMacSHA1SignerFactory implements ISignerFactory {

    public static final String METHOD = "HmacSHA1";

    private static ISinger singer = null;

    @Override
    public ISinger getSigner() throws SdkException{

        if (null == singer) {
            singer = new HMacSHA1Signer();
        }

        return singer;
    }

    private static class HMacSHA1Signer implements ISinger {
        @Override
        public String sign(String strToSign, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException  , SdkException {

            if (HttpCommonUtil.isEmpty(strToSign)) {
                throw new IllegalArgumentException("strToSign can not be empty");
            }

            if (HttpCommonUtil.isEmpty(secretKey)) {
                throw new IllegalArgumentException("secretKey can not be empty");
            }

            Mac hmacSha1 = Mac.getInstance(METHOD);
            byte[] keyBytes = secretKey.getBytes(SdkConstant.CLOUDAPI_ENCODING);
            hmacSha1.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, METHOD));

            byte[] md5Result = hmacSha1.doFinal(strToSign.getBytes(SdkConstant.CLOUDAPI_ENCODING));
            return Base64.encodeBase64String(md5Result);
        }
    }

    static String byte2String(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();


        if (null != data) {
            for (byte b : data) {
                stringBuilder.append(String.format("%02x", b));
            }
        }

        return stringBuilder.toString();
    }
}
