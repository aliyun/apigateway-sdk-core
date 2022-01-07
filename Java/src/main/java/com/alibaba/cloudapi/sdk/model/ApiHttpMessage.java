package com.alibaba.cloudapi.sdk.model;

import com.alibaba.cloudapi.sdk.constant.HttpConstant;
import com.alibaba.cloudapi.sdk.constant.SdkConstant;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.Charset;
import java.util.*;

public abstract class ApiHttpMessage {
    protected byte[] body;
    /*
    只有在websocket应答中可以读取bodyStr
     */
    protected String bodyStr;
    protected Map<String, List<String>> headers = new HashMap<String, List<String>>();

    public String getBodyStr() {
        return bodyStr;
    }

    public byte[] getBody() {
        return body;
    }
    public void setBody(byte[] body) {
        this.body = body;
    }
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void addHeader(String name , String value){
        name = name.trim().toLowerCase();
        addParam(name , value , headers);
    }

    protected void addParam(String name , String value , Map<String, List<String>> map){
        if(map.containsKey(name)){
            map.get(name).add(value);
        }
        else{
            List<String> values = new ArrayList<String>();
            values.add(value == null ? "" : value.trim());
            map.put(name , values);
        }
    }

    public String getFirstHeaderValue(String name){
        if(headers.containsKey(name) && headers.get(name).size() > 0){
            return headers.get(name).get(0);
        }

        return null;
    }

    public void parse(JsonNode message){
        JsonNode headers = message.get("header");
        if(headers != null && headers.size() > 0) {
            Iterator<String> names = headers.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (headers.get(name) != null) {
                    if (headers.get(name).size() > 0) {
                        for (JsonNode value : headers.get(name)) {
                            addHeader(name, value.asText());
                        }
                    } else {
                        addHeader(name, headers.get(name).asText());
                    }
                }

            }
        }


        String contentType = this.getFirstHeaderValue(HttpConstant.CLOUDAPI_HTTP_HEADER_CONTENT_TYPE);
        Charset charset = SdkConstant.CLOUDAPI_ENCODING;
        if(null  != contentType){
            try{
                contentType = contentType.toLowerCase();
                String[] charsetStr = contentType.split(";");
                for(int i = 0 ; i < charsetStr.length ; i++){
                    if(charsetStr[i].contains("charset")){
                        charset = Charset.forName(charsetStr[i].substring(charsetStr[i].indexOf("=") + 1));
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        JsonNode bodyNode = message.get("body");
        if(bodyNode != null){
            bodyStr = bodyNode.asText();
            body = bodyStr.getBytes(charset);
        }
    }
}
