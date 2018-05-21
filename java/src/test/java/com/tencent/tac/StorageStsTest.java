package com.tencent.tac;

import com.qcloud.Utilities.Json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;

public class StorageStsTest {

    @Test
    public void testGetCredential() {

        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            Properties properties = new Properties();
            File configFile = new File("local.properties");
            properties.load(new FileInputStream(configFile));

            // 您的 SecretID
            config.put("SecretId", properties.getProperty("SecretId"));
            // 您的 SecretKey
            config.put("SecretKey", properties.getProperty("SecretKey"));
            // 临时密钥有效时长，单位是秒
            config.put("durationInSeconds", 1800);

            if (properties.containsKey("https.proxyHost")) {
                System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
            }
        } catch (IOException e) {
            // 您的 SecretID
            config.put("SecretId", "xxx");
            // 您的 SecretKey
            config.put("SecretKey", "xxx");
            // 临时密钥有效时长，单位是秒
            config.put("durationInSeconds", 1800);
        }


        JSONObject credential = StorageSts.getCredential(config);

        System.out.println(credential);
    }
}
