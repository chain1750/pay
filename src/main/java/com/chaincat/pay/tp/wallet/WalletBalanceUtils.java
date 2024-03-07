package com.chaincat.pay.tp.wallet;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 钱包余额工具
 *
 * @author chenhaizhuang
 */
public class WalletBalanceUtils {

    /**
     * 获取签名
     *
     * @param requestMap 请求参数Map
     * @param salt       盐值
     * @return String
     */
    public static String getSign(Map<String, Object> requestMap, String salt) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        List<String> paramsArr = new ArrayList<>();
        requestMap.forEach((key, valueObj) -> {
            String value = valueObj.toString().trim();
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                value = value.substring(1, value.length() - 1).trim();
            }
            if (StrUtil.isNotEmpty(value)) {
                paramsArr.add(value);
            }
        });
        paramsArr.add(salt);
        paramsArr.sort(String::compareTo);

        StringBuilder builder = new StringBuilder();
        String sep = "";
        for (String param : paramsArr) {
            builder.append(sep).append(param);
            sep = "&";
        }

        byte[] byteArray = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 发送请求
     *
     * @param url               地址
     * @param requestJsonString 请求体
     * @return 返回数据
     */
    public static Object sendRequest(String url, String requestJsonString) {
        String response = HttpUtil.post(url, requestJsonString);
        JSONObject responseJsonObject = JSON.parseObject(response);
        Assert.isTrue(responseJsonObject.getIntValue("code") == 0, responseJsonObject.getString("msg"));
        return responseJsonObject.get("data");
    }
}
