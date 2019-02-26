## 获取 SDK
- 使用 maven 集成方式
- 
	在 java 工程中的 pom.xml 文件中添加如下：
	1）添加 maven 仓库地址
	<repositories>
		<repository>
			<id>bintray-qcloud-maven-repo</id>
			<name>qcloud-maven-repo</name>
			<url>https://dl.bintray.com/qcloud/maven-repo/</url>
			<layout>default</layout>
			<releases>
				<enabled>true</enabled>
			</releases>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
	</repositories>
	2）添加 sdk 依赖
    <dependency>
      <groupId>com.tencent.cloud</groupId>
      <artifactId>cos-sts-java</artifactId>
      <version>3.0.0</version>
    </dependency>

- 源码安装
- 
	拷贝 `src\main\java` 中的代码到您的工程中，并根据 pom.xml文件添加依赖项.

## 查看示例

请查看 `src/test` 下的 java 文件，里面描述了如何调用SDK。

## 接口说明

### getCredential

获取临时密钥接口

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|SecretId|String| 云 API 密钥 Id|
|SecretKey|String| 云 API 密钥 key|
|durationSeconds|long| 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200 |
|bucket|String| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|allowPrefix|String|资源的前缀，如* 或者 a/* 或者 a.jpg|
|allowActions|String[]| 授予 COS API 权限集合|
|policy|String| 策略：由 allowActions、bucket、region、allowPrefix字段组成的描述授权的具体信息|

### 返回值说明

|字段|类型|描述|
| ---- | ---- | ---- |
|credentials | String | 临时密钥信息 |
|tmpSecretId | String | 临时密钥 Id，可用于计算签名 |
|tmpSecretKey | String | 临时密钥 Key，可用于计算签名 |
|sessionToken | String | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |
|startTime | String | 密钥的起止时间，是 UNIX 时间戳 |
|expiredTime | String | 密钥的失效时间，是 UNIX 时间戳 |

### 使用示例
```
//方式一
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
    throw new IllegalArgumentException("no valid secret !");
}


//方式二
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
	
	//授权策略，可由 getPolicy(List<Scope>)生成
	String policy = "{\"version\":\"2.0\",\"statement\":[{\"actions\":[\"name/cos:putobject\"],\"effect\":\"al
	low\",\"principal\":{\"qcs\":[\"*\"]},\"resource\":[\"qcs::cos:ap-guangzhou:uid/12500000:p
	refix//12500000/test/test/*\"]}]}";
	config.put("policy", policy);

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

### getPolicy

获取策略(policy)接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|bucket|String| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|sourcePrefix|String|资源的前缀，如* 或者 a/* 或者 a.jpg|
|action|String| 授予 COS API 权限，如 name/cos:PutObject |
|scope|Scope| 构造policy的信息：由 action、bucket、region、sourcePrefix组成|

### 返回值说明
|字段|类型|描述|
| ---- | ---- | ---- |
|policy | String | 申请临时密钥所需的权限策略 |

### 使用示例
```java
List<Scope> scopes = new ArrayList<Scope>();
scopes.add(new Scope("name/cos:PutObject", "test-12500000", "ap-guangzhou", "/1.txt"));
scopes.add(new Scope("name/cos:GetObject", "test-12500000", "ap-guangzhou", "/dir/*"));
String policy = CosStsClient.getPolicy(scopes);
```
### 返回结果
```java
{
"version":"2.0",
"statement":[
	{
		"actions":["name/cos:PutObject"],
		"effect":"allow",
		"principal":{"qcs":["*"]},
		"resource":["qcs::cos:ap-guangzhou:uid/12500000:prefix//12500000/test/test/1.txt"]
	},
	{
		"actions":["name/cos:GetObject" ],
		"effect":"allow",
		"principal":{"qcs":["*"]},
		"resource":["qcs::cos:ap-guangzhou:uid/12500000:prefix//12500000/test/dir/*" ]
	}
]
}
```