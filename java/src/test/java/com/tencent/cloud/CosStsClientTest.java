package com.tencent.cloud;

import com.tencent.cloud.assumerole.AssumeRoleParam;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import com.tencent.cloud.cos.util.Jackson;

import org.junit.Test;

public class CosStsClientTest {

    /**
     * 基本的临时密钥申请示例，适合对一个桶内的一批对象路径，统一授予一批操作权限
     */
    @Test
    public void testGetCredential() {
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            Properties properties = new Properties();
            File configFile = new File("local.properties");
            properties.load(new FileInputStream(configFile));

			 // 云 api 密钥 SecretId
             config.put("secretId", properties.getProperty("SecretId"));
             // 云 api 密钥 SecretKey
             config.put("secretKey", properties.getProperty("SecretKey"));

            if (properties.containsKey("https.proxyHost")) {
                System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
            }

            // 设置域名,可通过此方式设置内网域名
            //config.put("host", "sts.internal.tencentcloudapi.com");

            // 临时密钥有效时长，单位是秒
            config.put("durationSeconds", 1800);

            // 换成你的 bucket
            config.put("bucket", "sts-sdk-test-1251668577");
            // 换成 bucket 所在地区
            config.put("region", "ap-chengdu");

            // 可以通过 allowPrefixes 指定前缀数组, 例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
            config.put("allowPrefixes", new String[] {
                    "exampleobject",
                    "exampleobject2"
            });

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

            Response response = CosStsClient.getCredential(config);
            System.out.println(response.credentials.tmpSecretId);
            System.out.println(response.credentials.tmpSecretKey);
            System.out.println(response.credentials.sessionToken);
        } catch (Exception e) {
        	    e.printStackTrace();
            throw new IllegalArgumentException("no valid secret !");
        }
    }
    	
    /**
     * 基本的临时密钥申请示例，适用于对多个桶的对应文件路径设置分别多条权限
     */
    @Test 
    public void testGetCredential2() {
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            Properties properties = new Properties();
            File configFile = new File("local.properties");
            properties.load(new FileInputStream(configFile));

            // 固定密钥 SecretId
            config.put("secretId", properties.getProperty("SecretId"));
            // 固定密钥 SecretKey
            config.put("secretKey", properties.getProperty("SecretKey"));

            if (properties.containsKey("https.proxyHost")) {
                System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
            }

            // 临时密钥有效时长，单位是秒
            config.put("durationSeconds", 1800);

            //设置 policy
            List<Scope> scopes = new ArrayList<Scope>();
            Scope scope = new Scope("name/cos:PutObject", "sts-sdk-test-1251668577", "ap-chengdu", "/test.txt");
            scopes.add(scope);
            scopes.add(new Scope("name/cos:GetObject", "sts-sdk-test-1251668577", "ap-chengdu", "/test.txt"));
            config.put("policy", CosStsClient.getPolicy(scopes));

            /**
             * condition复杂且没有统一的格式，这里也可以直接设置
             * 设置condition（如有需要）
             //# 临时密钥生效条件，关于condition的详细设置规则和COS支持的condition类型可以参考 https://cloud.tencent.com/document/product/436/71307
             final String raw_policy = "{\n" +
             "  \"version\":\"2.0\",\n" +
             "  \"statement\":[\n" +
             "    {\n" +
             "      \"effect\":\"allow\",\n" +
             "      \"action\":[\n" +
             "          \"name/cos:PutObject\",\n" +
             "          \"name/cos:PostObject\",\n" +
             "          \"name/cos:InitiateMultipartUpload\",\n" +
             "          \"name/cos:ListMultipartUploads\",\n" +
             "          \"name/cos:ListParts\",\n" +
             "          \"name/cos:UploadPart\",\n" +
             "          \"name/cos:CompleteMultipartUpload\"\n" +
             "        ],\n" +
             "      \"resource\":[\n" +
             "          \"qcs::cos:ap-shanghai:uid/1250000000:examplebucket-1250000000/*\"\n" +
             "      ],\n" +
             "      \"condition\": {\n" +
             "        \"ip_equal\": {\n" +
             "            \"qcs:ip\": [\n" +
             "                \"192.168.1.0/24\",\n" +
             "                \"101.226.100.185\",\n" +
             "                \"101.226.100.186\"\n" +
             "            ]\n" +
             "        }\n" +
             "      }\n" +
             "    }\n" +
             "  ]\n" +
             "}";

             config.put("policy", raw_policy);
             */

            Response credential = CosStsClient.getCredential(config);
            System.out.println(Jackson.toJsonPrettyString(credential));
        } catch (Exception e) {
         e.printStackTrace();
            throw new IllegalArgumentException("no valid secret !");
        }
    }

    /**
     * 更细致粒度设置的临时密钥申请示例，根据 https://cloud.tencent.com/document/product/598/10603 中列出的元素设置临时密钥权限
     */
    @Test
    public void testGetCredential3() {
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            Properties properties = new Properties();
            File configFile = new File("local.properties");
            properties.load(new FileInputStream(configFile));

            // 云 api 密钥 SecretId
            config.put("secretId", properties.getProperty("SecretId"));
            // 云 api 密钥 SecretKey
            config.put("secretKey", properties.getProperty("SecretKey"));

            if (properties.containsKey("https.proxyHost")) {
                System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
                System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
            }

            // 初始化 policy
            Policy policy = new Policy();
            policy.setVersion("2.0");

            // 开始构建一条 statement
            Statement statement = new Statement();
            // 声明设置的结果是允许操作
            statement.setEffect("allow");
            // 添加一批操作权限
            statement.addActions(new String[] {
                    "name/cos:PutObject",
                    "name/cos:PostObject",
                    // 分片上传
                    "name/cos:InitiateMultipartUpload",
                    "name/cos:ListMultipartUploads",
                    "name/cos:ListParts",
                    "name/cos:UploadPart",
                    "name/cos:CompleteMultipartUpload"
            });
            // 添加一批资源路径
            // 一条资源的规则是：qcs::cos:<REGION>:uid/<APPID>:<BUCKET-APPID>/<OBJECT>
            statement.addResources(new String[] {
                    String.format("qcs::cos:%s:uid/%s:%s/%s",
                            "ap-chengdu", "1251668577", "sts-sdk-test-1251668577", "exampleObject")
            });

            // 开始构建生效条件 condition
            // 关于 condition 的详细设置规则可以参考 https://cloud.tencent.com/document/product/598/10604#.E5.AD.97.E7.AC.A6.E4.B8.B2.E8.AF.B4.E6.98.8E
            ConditionTypeValue conditionTypeValue = new ConditionTypeValue();
            conditionTypeValue.setKey("qcs:ip");
            conditionTypeValue.addValue("10.10.10.10");
            conditionTypeValue.addValue("10.10.10.11");

            // 增加规则
            statement.addCondition("ip_equal", conditionTypeValue);

            // 把一条 statement 添加到 policy
            // 可以添加多条
            policy.addStatement(statement);

            // 临时密钥有效时长，单位是秒
            config.put("durationSeconds", 1800);
            // 换成 bucket 所在地区
            config.put("region", "ap-chengdu");

            // 将 Policy 示例转化成 String，可以使用任何 json 转化方式，这里是本 SDK 自带的推荐方式
            config.put("policy", Jackson.toJsonPrettyString(policy));

            Response response = CosStsClient.getCredential(config);
            System.out.println(Jackson.toJsonPrettyString(response));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("no valid secret !");
        }
    }

    @Test
    public void testGetAssumeRoleCredential() {
        final String configFileName = "local.properties";
        File configFile = null;
        try {
            configFile = new File(configFileName);
        } catch (Exception e) {
            System.out.println("fail to load config file: " + configFile + "from root directory of the project.");
            e.printStackTrace();
            throw new IllegalArgumentException("fail to load config file.");
        }

        AssumeRoleParam param = null;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(configFile));

            param = new AssumeRoleParam();
            param.Parse(properties);
//            param.setDurationSec(3600); // the param con also be modified by setter.
        } catch (Exception e) {
            System.out.println("fail to build param from properties");
            e.printStackTrace();
            throw new IllegalArgumentException("fail to build param from properties.");
        }

        try {
            Response response = CosStsClient.getRoleCredential(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("fail to assume role");
        }
    }
}
