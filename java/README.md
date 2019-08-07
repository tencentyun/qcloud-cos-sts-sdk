## 获取 SDK
- 使用 maven 集成方式

	在 java 工程中的 pom.xml 文件中添加如下：
	
	1)添加 指定的 maven 仓库地址

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
    	    <version>3.0.3</version>
    	</dependency>

## 接口说明

### getCredential

获取临时密钥接口

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|secretId|String| 云 API 密钥 Id|
|secretKey|String| 云 API 密钥 key|
|durationSeconds|int| 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200 |
|bucket|String| 存储桶名称：bucketName-appid, 如 example-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|allowPrefix|String|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`|
|allowActions|String[]| 授予 COS API 权限集合, 如简单上传操作：name/cos:PutObject|
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

    // 云 API 密钥 secretId
    config.put("secretId", properties.getProperty("SecretId"));
    // 云 API 密钥 secretKey
    config.put("secretKey", properties.getProperty("SecretKey"));
	//若需要设置网络代理，则可以如下设置
    if (properties.containsKey("https.proxyHost")) {
        System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
        System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
    }

    // 临时密钥有效时长，单位是秒
    config.put("durationSeconds", 1800);

    // 换成你的 bucket
    config.put("bucket", "example-125000000");
    // 换成 bucket 所在地区
    config.put("region", "ap-guangzhou");

    // 设置可操作的资源路径前缀，根据实际情况进行设置
	// 如授予可操作所有的资源：则为 *； 
	// 如授予操作某个路径a下的所有资源，则为 a/*；  
	// 如授予只能操作某个特定路径的文件 a/test.jpg， 则为 a/test.jpg
    config.put("allowPrefix", "*");

    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    String[] allowActions = new String[] {
            // 简单上传
            "name/cos:PutObject",
			// 表单上传
            "name/cos:PostObject",
            // 分片上传： 初始化分片
            "name/cos:InitiateMultipartUpload",
			// 分片上传： 查询 bucket 中未完成分片上传的UploadId
            "name/cos:ListMultipartUploads",
			// 分片上传： 查询已上传的分片
            "name/cos:ListParts",
			// 分片上传： 上传分片块
            "name/cos:UploadPart",
			// 分片上传： 完成分片上传
            "name/cos:CompleteMultipartUpload"
    };
    config.put("allowActions", allowActions);
	// 请求临时密钥信息
    JSONObject credential = CosStsClient.getCredential(config);
	// 请求成功：打印对应的临时密钥信息
    System.out.println(credential.toString(4));
} catch (Exception e) {
    // 请求失败，抛出异常
	throw new IllegalArgumentException("no valid secret !");
}


//方式二
TreeMap<String, Object> config = new TreeMap<String, Object>();

try {
    Properties properties = new Properties();
    File configFile = new File("local.properties");
    properties.load(new FileInputStream(configFile));

    // 云 API 密钥 secretId
    config.put("secretId", properties.getProperty("SecretId"));
    // 云 API 密钥 secretKey
    config.put("secretKey", properties.getProperty("SecretKey"));
	//若需要设置网络代理，则可以如下设置
    if (properties.containsKey("https.proxyHost")) {
        System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"));
        System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"));
    }

    // 临时密钥有效时长，单位是秒
    config.put("durationSeconds", 1800);
	
	//授权策略，指明了可操作那些资源，以及可操作的权限的列表，可由 getPolicy(List<Scope>)生成
	String policy = "{\"version\":\"2.0\",\"statement\":[{\"action\":[\"name/cos:putobject\"],\"effect\":\"al
	low\",\"resource\":[\"qcs::cos:ap-guangzhou:uid/125000000:example-125000000/*\"]}]}";
	config.put("policy", policy);

	// 请求临时密钥信息
    JSONObject credential = CosStsClient.getCredential(config);
	// 请求成功：打印对应的临时密钥信息
    System.out.println(credential.toString(4));
} catch (Exception e) {
    // 请求失败，抛出异常
	throw new IllegalArgumentException("no valid secret !");
}
```

### 返回结果 credential

成功的话，可以拿到包含密钥的 JSON 文本：

```
{
    "credentials": {
        "tmpSecretId": "AKIDEPMQB_Q9Jt2fJxXyIekOzKZzx-sdGQgBga4TzsUdTWL9xlvsjInOHhCYFqfoKOY4",
        "tmpSecretKey": "W/3Lbl1YEW02mCoawIesl5kNehSskrSbp1cT1tgW70g=",
        "sessionToken": "c6xnSYAxyFbX8Y50627y9AA79u6Qfucw6924760b61588b79fea4c277b01ba157UVdr_10Y30bdpYtO8CXedYZe3KKZ_DyzaPiSFfNAcbr2MTfAgwJe-dhYhfyLMkeCqWyTNF-rOdOb0rp4Gto7p4yQAKuIPhQhuDd77gcAyGakC2WXHVd6ZuVaYIXBizZxqIHAf4lPiLHa6SZejSQfa_p5Ip2U1cAdkEionKbrX97xTKTcA_5Pu525CFSzHZIQibc2uNMZ-IRdQp12MaXZB6bxM6nB4xXH45mDIlbIGjaAsrtRJJ3csmf82uBKaJrYQoguAjBepMH91WcH87LlW9Ya3emNfVX7NMRRf64riYd_vomGF0TLgan9smEKAOdtaL94IkLvVJdhLqpvjBjp_4JCdqwlFAixaTzGJHdJzpGWOh0mQ6jDegAWgRYTrJvc5caYTz7Vphl8XoX5wHKKESUn_vqyTAid32t0vNYE034FIelxYT6VXuetYD_mvPfbHVDIXaFt7e_O8hRLkFwrdAIVaUml1mRPvccv2qOWSXs"
    },
    "expiration": "2019-08-07T08:54:35Z",
    "startTime": 1565166275,
    "expiredTime": 1565168075
}
```

### getPolicy

获取策略(policy)接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|bucket|String| 存储桶名称：bucketName-appid, 如 example-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|sourcePrefix|String|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`|
|action|String| 授予 COS API 权限， 如简单上传操作：name/cos:PutObject name/cos:PutObject |
|scope|Scope| 构造policy的信息：由 action、bucket、region、sourcePrefix组成|

### 返回值说明
|字段|类型|描述|
| ---- | ---- | ---- |
|policy | String | 申请临时密钥所需的权限策略 |

### 使用示例
```java
List<Scope> scopes = new ArrayList<Scope>();
scopes.add(new Scope("name/cos:PutObject", "example-125000000", "ap-guangzhou", "/1.txt"));
scopes.add(new Scope("name/cos:GetObject", "example-125000000", "ap-guangzhou", "/dir/*"));
String policy = CosStsClient.getPolicy(scopes);
```
### 返回结果
```java
{
"version":"2.0",
"statement":[
	{
		"action":["name/cos:PutObject"],
		"effect":"allow",
		"resource":["qcs::cos:ap-guangzhou:uid/1250000000:example-125000000/1.txt"]
	},
	{
		"action":["name/cos:GetObject" ],
		"effect":"allow",
		"resource":["qcs::cos:ap-guangzhou:uid/1250000000:example-125000000/dir/*" ]
	}
]
}
```
