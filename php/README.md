## 获取 SDK
- 源码安装

	拷贝 `sts/sts.php` 文件到您的 php 工程中。

## 查看示例

请查看 `demo/sts_test.php` 文件，里面描述了如何调用SDK。

## 接口说明

### getTempKeys

获取临时密钥接口

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|secretId|string| 云 API 密钥 Id|
|secretKey|string| 云 API 密钥 key|
|durationSeconds|long| 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200 |
|bucket|string| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|string| 存储桶所属地域，如 ap-guangzhou|
|allowPrefix|string|资源的前缀，如* 或者 a/* 或者 a.jpg|
|allowActions|array| 授予 COS API 权限集合|
|policy|array| 策略：由 allowActions、bucket、region、allowPrefix字段组成的描述授权的具体信息|

### 返回值说明

|字段|类型|描述|
| ---- | ---- | ---- |
|credentials | string | 临时密钥信息 |
|tmpSecretId | string | 临时密钥 Id，可用于计算签名 |
|tmpSecretKey | string | 临时密钥 Key，可用于计算签名 |
|sessionToken | string | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |
|startTime | string | 密钥的起止时间，是 UNIX 时间戳 |
|expiredTime | string | 密钥的失效时间，是 UNIX 时间戳 |

### 使用示例
```php
//方法一
// 配置参数
$config = array(
    'Url' => 'https://sts.api.qcloud.com/v2/index.php',
    'Domain' => 'sts.api.qcloud.com',
    'Proxy' => '', 
    'SecretId' => 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    'SecretKey' => 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    'Bucket' => 'test-1250000000', // 换成你的 bucket
    'Region' => 'ap-guangzhou', // 换成 bucket 所在地区
    'DurationSeconds' => 1800, // 密钥有效期
    'AllowPrefix' => '*', // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 a/* 或者 a.jpg
    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allowActions' => array (
        // 简单上传
        'name/cos:PutObject',
        'name/cos:PostObject',
        // 分片上传
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    )
);

// 获取临时密钥，计算签名
$tempKeys = getTempKeys();

// 返回数据给前端
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: http://127.0.0.1'); // 这里修改允许跨域访问的网站
header('Access-Control-Allow-Headers: origin,accept,content-type');
echo json_encode($tempKeys);

//方法二
//设置策略 policy，可通过 getPolicy($scopes)获取
$actions=array('name/cos:PutObject');
$resources = array("qcs::cos:ap-guangzhou:uid/12500000:prefix//12500000/test/*");
$principal = array(
	'qcs' => array('*')
);
$statements = array(array(
		'actions' => $actions,
		'effect' => 'allow',
		'principal' => $principal,
		'resource' => $resources
));
$policy = array(
'version'=> '2.0', 
'statement' => $statements
);

// 配置参数
$config = array(
    'Url' => 'https://sts.api.qcloud.com/v2/index.php',
    'Domain' => 'sts.api.qcloud.com',
    'Proxy' => '', 
    'SecretId' => 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    'SecretKey' => 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    'policy' => $policy
    )
);

// 获取临时密钥，计算签名
$tempKeys = getTempKeys();

// 返回数据给前端
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: http://127.0.0.1'); // 这里修改允许跨域访问的网站
header('Access-Control-Allow-Headers: origin,accept,content-type');
echo json_encode($tempKeys);
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
|bucket|string| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|string| 存储桶所属地域，如 ap-guangzhou|
|sourcePrefix|string|资源的前缀，如* 或者 a/* 或者 a.jpg|
|action|string| 授予 COS API 权限，如 name/cos:PutObject |
|scope|Scope| 构造policy的信息：由 action、bucket、region、sourcePrefix组成|

### 返回值说明
|字段|类型|描述|
| ---- | ---- | ---- |
|policy | array | 申请临时密钥所需的权限策略 |

### 使用示例
```java
$scopes = array();
array_push($scopes,new Scope("name/cos:PutObject", "test-12500000", "ap-guangzhou", "/1.txt"));
array_push($scopes, new Scope("name/cos:GetObject", "test-12500000", "ap-guangzhou", "/dir/*"));
$policy = getPolicy($scopes);
echo str_replace('\\/', '/', json_encode($policy));
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