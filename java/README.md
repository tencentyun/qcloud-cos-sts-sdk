## 获取 SDK

在 java 工程的 pom.xml 文件中集成依赖：

```
<dependency>
   <groupId>org.json</groupId>
   <artifactId>json</artifactId>
   <version>20180130</version>
   <scope>compile</scope>
</dependency>
```


### 查看示例

请查看 `src/test` 下的 java 文件，里面描述了如何调用SDK。

### 使用方法

调用代码如下：

```
TreeMap<String, Object> config = new TreeMap<String, Object>();

try {
    Properties properties = new Properties();
    File configFile = new File("local.properties");
    properties.load(new FileInputStream(configFile));

    // 固定密钥
    config.put("SecretId", properties.getProperty("SecretId"));
    // 固定密钥
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
    throw new IllegalArgumentException("no valid secret !");
}
```

### 返回结果

成功的话，可以拿到包含密钥的 JSON 文本：

```
{ credentials:
   { sessionToken: 'd88109ab2794fc4e8c9491353face398c240441030001',
     tmpSecretId: 'AKIDq9bhO815EteWwntqvvzOeSTONZ4knQgr',
     tmpSecretKey: 'GJz0iUp5eCeidvqnXoFGfm6Leq28t1NX' },
  expiredTime: 1545306616,
  startTime: 1545304817 }
```


