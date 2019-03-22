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

            // 鍥哄畾瀵嗛挜
            config.put("SecretId", properties.getProperty("SecretId"));
            // 鍥哄畾瀵嗛挜
            config.put("SecretKey", properties.getProperty("SecretKey"));

            if (properties.containsKey("https.proxyHost")) {
                System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
            }

            // 涓存椂瀵嗛挜鏈夋晥鏃堕暱锛屽崟浣嶆槸绉�
            config.put("durationSeconds", 1800);

            // 鎹㈡垚浣犵殑 bucket
            config.put("bucket", "android-ut-persist-bucket-1253653367");
            // 鎹㈡垚 bucket 鎵�鍦ㄥ湴鍖�
            config.put("region", "ap-guangzhou");

            // 杩欓噷鏀规垚鍏佽鐨勮矾寰勫墠缂�锛屽彲浠ユ牴鎹嚜宸辩綉绔欑殑鐢ㄦ埛鐧诲綍鎬佸垽鏂厑璁镐笂浼犵殑鐩綍锛屼緥瀛愶細* 鎴栬�� a/* 鎴栬�� a.jpg
            config.put("allowPrefix", "*");

            // 瀵嗛挜鐨勬潈闄愬垪琛ㄣ�傜畝鍗曚笂浼犲拰鍒嗙墖闇�瑕佷互涓嬬殑鏉冮檺锛屽叾浠栨潈闄愬垪琛ㄨ鐪� https://cloud.tencent.com/document/product/436/31923
            String[] allowActions = new String[] {
                    // 绠�鍗曚笂浼�
                    "name/cos:PutObject",
                    "name/cos:PostObject",
                    // 鍒嗙墖涓婁紶
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

             // 鍥哄畾瀵嗛挜
             config.put("SecretId", properties.getProperty("SecretId"));
             // 鍥哄畾瀵嗛挜
             config.put("SecretKey", properties.getProperty("SecretKey"));

             if (properties.containsKey("https.proxyHost")) {
                 System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                 System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
             }

             // 涓存椂瀵嗛挜鏈夋晥鏃堕暱锛屽崟浣嶆槸绉�
             config.put("durationSeconds", 1800);
             
             //璁剧疆 policy
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
