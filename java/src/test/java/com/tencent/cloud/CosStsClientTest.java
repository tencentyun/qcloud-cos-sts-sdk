package com.tencent.cloud;

import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

public class CosStsClientTest {

    @Test
    public void testGetCredential() {
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            Properties properties = new Properties();
            File configFile = new File("local.properties");
            properties.load(new FileInputStream(configFile));

			 // 云 api 密钥 SecretId
             config.put("SecretId", properties.getProperty("SecretId"));
             // 云 api 密钥 SecretKey
             config.put("SecretKey", properties.getProperty("SecretKey"));

            if (properties.containsKey("https.proxyHost")) {
                System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
            }

            // 临时密钥有效时长，单位是秒
            config.put("durationSeconds", 1800);

            // 换成你的 bucket
            config.put("bucket", "android-ut-persist-bucket-1253653367");
            // 换成 bucket 所在地区
            config.put("region", "ap-guangzhou");

            // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 a/* 或者 a.jpg
            config.put("allowPrefix", "*");

             // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
            String[] allowActions = new String[] {
                     // 简单上传
                    "name/cos:PutObject",
                    "name/cos:PostObject",
                    // 分片上传
                    "name/cos:InitiateMultipartUpload",
                    "name/cos:ListMultipartUploads",
                    "name/cos:ListParts",
                    "name/cos:UploadPart",
                    "name/cos:CompleteMultipartUpload"
            };
            config.put("allowActions", allowActions);

            JSONObject credential = CosStsClient.getCredential(config);
            System.out.println(credential);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new IllegalArgumentException("no valid secret !");
        }
    }
    
    @Test
    public void testGetPolicy() {
    	List<Scope> scopes = new ArrayList<Scope>();
    	Scope scope = new Scope("name/cos:PutObject", "android-ut-persist-bucket-1253653367", "ap-guangzhou", "/test.txt");
    	scopes.add(scope);
    	System.out.println(CosStsClient.getPolicy(scopes));
    	
    }
    
    @Test 
    public void testGetCredential2() {
    	 TreeMap<String, Object> config = new TreeMap<String, Object>();

         try {
             Properties properties = new Properties();
             File configFile = new File("local.properties");
             properties.load(new FileInputStream(configFile));

             // 固定密钥 SecretId
             config.put("SecretId", properties.getProperty("SecretId"));
             // 固定密钥 SecretKey
             config.put("SecretKey", properties.getProperty("SecretKey"));

             if (properties.containsKey("https.proxyHost")) {
                 System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                 System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
             }

             // 临时密钥有效时长，单位是秒
             config.put("durationSeconds", 1800);
             
             //设置 policy
             List<Scope> scopes = new ArrayList<Scope>();
             Scope scope = new Scope("name/cos:PutObject", "android-ut-persist-bucket-1253653367", "ap-guangzhou", "/test.txt");
         	 scopes.add(scope);
         	 scopes.add(new Scope("name/cos:GetObject", "android-ut-persist-bucket-1253653367", "ap-guangzhou", "/test.txt"));
             config.put("policy", CosStsClient.getPolicy(scopes));

             JSONObject credential = CosStsClient.getCredential(config);
             System.out.println(credential);
         } catch (Exception e) {
        	 e.printStackTrace();
             throw new IllegalArgumentException("no valid secret !");
         }
    	
    }
}
