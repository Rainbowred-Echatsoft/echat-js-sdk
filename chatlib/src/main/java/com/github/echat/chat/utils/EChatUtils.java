package com.github.echat.chat.utils;

import com.github.echat.chat.utils.aes.AesUtils;

import org.dom4j.Document;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author: xhy
 * @Email: xuhaoyang3x@gmail.com
 * @program: EChatMultiSample
 * @create: 2019-06-20
 * @describe
 */
public class EChatUtils {

    public static String getSHA1(String token, String appid, String companyId) {
        try {
            String[] array = new String[]{token, appid, companyId};
            StringBuffer sb = new StringBuffer();
            Arrays.sort(array);
            for (String s : array) {
                sb.append(s);
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(sb.toString().getBytes());
            byte[] digest = md.digest();

            StringBuffer hexstr = new StringBuffer();
            String shaHex;
            for (int i = 0; i < digest.length; i++) {
                shaHex = Integer.toHexString(digest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexstr.append(0);
                }
                hexstr.append(shaHex);
            }
            return hexstr.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 加密生成metaData
     *
     * @param metaData
     * @param encodingKey
     * @param appId
     * @return
     */
    public static String create2MetaData(Map<String, Object> metaData,
                                         String encodingKey, String appId) {
        try {
            Document document = XMLUtils.map2xml(metaData, "xml");
            String xml = XMLUtils.formatXml(document);
            AesUtils aes = new AesUtils(encodingKey, appId);
            return aes.encrypt(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
