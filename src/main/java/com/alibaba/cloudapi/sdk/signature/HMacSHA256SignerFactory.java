package com.alibaba.cloudapi.sdk.signature;


import com.alibaba.cloudapi.sdk.constant.SdkConstant;
import com.alibaba.cloudapi.sdk.exception.SdkException;
import com.alibaba.cloudapi.sdk.util.HttpCommonUtil;
import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMacSHA256SignerFactory implements ISignerFactory {

    public static final String METHOD = "HmacSHA256";

    private static ISinger singer = null;

    @Override
    public ISinger getSigner() throws SdkException{

        if (null == singer) {
            singer = new HMacSHA256Signer();
        }

        return singer;
    }

    private static class HMacSHA256Signer implements ISinger {
        @Override
        public String sign(String strToSign, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException  , SdkException {

            if (HttpCommonUtil.isEmpty(strToSign)) {
                throw new IllegalArgumentException("strToSign can not be empty");
            }

            if (HttpCommonUtil.isEmpty(secretKey)) {
                throw new IllegalArgumentException("secretKey can not be empty");
            }

            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = secretKey.getBytes(SdkConstant.CLOUDAPI_ENCODING);
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));

            byte[] md5Result = hmacSha256.doFinal(strToSign.getBytes(SdkConstant.CLOUDAPI_ENCODING));
            return Base64.encodeBase64String(md5Result);
        }
    }
}
