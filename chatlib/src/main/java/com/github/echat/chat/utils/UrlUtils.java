package com.github.echat.chat.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: xuhaoyang
 * @Email: xuhaoyang3x@gmail.com
 * @program: android-demo
 * @create: 2019-02-14
 * @describe
 */
public class UrlUtils {
    /**
     * 向url链接追加参数
     *
     * @param url
     * @param params Map<String, String>
     * @return
     */
    public static String appendParams(String url, Map<String, String> params) {
        if (isBlank(url)) {
            return "";
        } else if (isEmptyMap(params)) {
            return url.trim();
        } else {
            StringBuffer sb = new StringBuffer();
            Set<String> keys = params.keySet();
            for (String key : keys) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);

            url = url.trim();
            int length = url.length();
            int index = url.indexOf("?");
            if (index > -1) {//url说明有问号
                if ((length - 1) == index) {//url最后一个符号为？，如：http://wwww.baidu.com?
                    url += sb.toString();
                } else {//情况为：http://wwww.baidu.com?aa=11
                    url += "&" + sb.toString();
                }
            } else {//url后面没有问号，如：http://wwww.baidu.com
                url += "?" + sb.toString();
            }
            return url;
        }
    }

    /**
     * 向url链接追加参数(单个)
     *
     * @param url
     * @param name  String
     * @param value String
     * @return
     */
    public static String appendParam(String url, String name, String value) {
        if (isBlank(url)) {
            return "";
        } else if (isBlank(name)) {
            return url.trim();
        } else {
            Map<String, String> params = new HashMap<String, String>();
            params.put(name, value);
            return appendParams(url, params);
        }
    }

    /**
     * 移除url链接的多个参数
     *
     * @param url        String
     * @param paramNames String[]
     * @return
     */
    public static String removeParams(String url, String... paramNames) {
        if (isBlank(url)) {
            return "";
        } else if (isEmptyArray(paramNames)) {
            return url.trim();
        } else {
            url = url.trim();
            int length = url.length();
            int index = url.indexOf("?");
            if (index > -1) {//url说明有问号
                if ((length - 1) == index) {//url最后一个符号为？，如：http://wwww.baidu.com?
                    return url;
                } else {//情况为：http://wwww.baidu.com?aa=11或http://wwww.baidu.com?aa=或http://wwww.baidu.com?aa
                    String baseUrl = url.substring(0, index);
                    String paramsString = url.substring(index + 1);
                    String[] params = paramsString.split("&");
                    if (!isEmptyArray(params)) {
                        Map<String, String> paramsMap = new HashMap<String, String>();
                        for (String param : params) {
                            if (!isBlank(param)) {
                                String[] oneParam = param.split("=");
                                String paramName = oneParam[0];
                                int count = 0;
                                for (int i = 0; i < paramNames.length; i++) {
                                    if (paramNames[i].equals(paramName)) {
                                        break;
                                    }
                                    count++;
                                }
                                if (count == paramNames.length) {
                                    paramsMap.put(paramName, (oneParam.length > 1) ? oneParam[1] : "");
                                }
                            }
                        }
                        if (!isEmptyMap(paramsMap)) {
                            StringBuffer paramBuffer = new StringBuffer(baseUrl);
                            paramBuffer.append("?");
                            Set<String> set = paramsMap.keySet();
                            for (String paramName : set) {
                                paramBuffer.append(paramName).append("=").append(paramsMap.get(paramName)).append("&");
                            }
                            paramBuffer.deleteCharAt(paramBuffer.length() - 1);
                            return paramBuffer.toString();
                        }
                        return baseUrl;
                    }
                }
            }
            return url;
        }
    }

    /**
     * 判断数组是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isEmptyArray(Object[] obj) {
        return (obj == null || obj.length < 1);
    }

    /**
     * 判断Map是否为空
     *
     * @param map
     * @return
     */
    public static <K, V> boolean isEmptyMap(Map<K, V> map) {
        return (map == null || map.size() < 1);
    }


    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }


    public static Map<String, List<String>> getQueryParams(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    public static boolean contains(String url, String s) {
        if (isBlank(url)) {
            return false;
        } else if (s == null || "".equals(s)) {
            return false;
        } else {
            url = url.trim();
            int length = url.length();
            int index = url.indexOf("?");
            if (index > -1) {//url说明有问号
                if ((length - 1) == index) {//url最后一个符号为？，如：http://wwww.baidu.com?
                    return false;
                } else {//情况为：http://wwww.baidu.com?aa=11或http://wwww.baidu.com?aa=或http://wwww.baidu.com?aa
                    String paramsString = url.substring(index + 1);
                    String[] params = paramsString.split("&");
                    if (!isEmptyArray(params)) {
                        for (String param : params) {
                            if (!isBlank(param)) {
                                String[] oneParam = param.split("=");
                                String paramName = oneParam[0];
                                if (s.equals(paramName)) return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

}
