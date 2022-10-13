## 获取 SDK
- 使用 maven 集成方式，在 Java 工程中的 pom.xml 文件中添加如下的 maven 依赖：
	
```xml
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>cos-sts_api</artifactId>
    <version>3.1.0</version>
</dependency>
```

## 调用示例

请查看 [test](https://github.com/tencentyun/qcloud-cos-sts-sdk/tree/master/java/src/test) 里的示例。

## 接口说明

### getCredential

获取临时密钥接口
具体接口说明请见 [官网文档](https://cloud.tencent.com/document/product/1312/48195) 。

### getPolicy

获取策略(policy)接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|bucket|String| 存储桶名称：bucketName-appid, 如 example-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|sourcePrefix|String|资源的前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用) |
|action|String| 授予 COS API 权限， 如简单上传操作：name/cos:PutObject name/cos:PutObject |
|scope|Scope| 构造policy的信息：由 action、bucket、region、sourcePrefix组成|

### 返回值说明
|字段|类型|描述|
| ---- | ---- | ---- |
|policy | String | 申请临时密钥所需的权限策略 |


### 返回结果
```json
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
		"resource":["qcs::cos:ap-guangzhou:uid/1250000000:example-125000000/dir/exampleobject" ]
	}
]
}
```
