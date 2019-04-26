## 获取 SDK
- composer 安装
   ```
   创建composer.json的文件，内容如下：
   {
    	"require":{
    		"qcloud_sts/qcloud-sts-sdk": "1.3.*"
    	}
    }
    ```
	
   执行如下命令 `composer install`,安装sdk。使用该命令后会在当前目录中创建一个 vendor 文件夹，里面包含 SDK 的依赖库和一个 autoload.php 脚本，方便在项目中调用.

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
|allowPrefix|string|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`|
|allowActions|array| 授予 COS API 权限集合, 如简单上传操作：name/cos:PutObject|
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
include 'sts.php'

//方法一
// 配置参数
$config = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
    //'proxy' => null,  //设置网络请求代理,若不需要设置，则为null
    'secretId' => 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 云 API 密钥 secretId
    'secretKey' => 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 云 API 密钥 secretKey
    'bucket' => 'example-1250000000', // 换成你的 bucket
    'region' => 'ap-guangzhou', // 换成 bucket 所在地区
    'durationSeconds' => 1800, // 密钥有效期
    'allowPrefix' => '*', // 设置可操作的资源路径前缀，根据实际情况进行设置,如授予可操作所有的资源：则为 *； 如授予操作某个路径a下的所有资源，则为 a/*；如授予只能操作某个特定路径的文件 a/test.jpg， 则为 a/test.jpg
    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allowActions' => array (
        // 简单上传
        'name/cos:PutObject',
		// 表单上传
        'name/cos:PostObject',
        // 分片上传： 初始化分片
        'name/cos:InitiateMultipartUpload',
		// 分片上传： 查询 bucket 中未完成分片上传的UploadId
        "name/cos:ListMultipartUploads",
		// 分片上传： 查询已上传的分片
        "name/cos:ListParts",
		// 分片上传： 上传分片块
        "name/cos:UploadPart",
		// 分片上传： 完成分片上传
        "name/cos:CompleteMultipartUpload"
    )
);

//创建 sts
$sts = new STS();

// 获取临时密钥，计算签名
$tempKeys = $sts->getTempKeys($config);

echo json_encode($tempKeys);

//方法二
//设置策略 policy，可通过 STS 的 getPolicy($scopes)获取
$actions=array('name/cos:PutObject'); // 简单上传
$resources = array("qcs::cos:ap-guangzhou:uid/12500000:example-1250000000/*"); // 设置可操作的资源路径前缀，根据实际情况进行设置

$statements = array(array(
		'action' => $actions,
		'effect' => 'allow',
		'resource' => $resources
));
$policy = array(
'version'=> '2.0', 
'statement' => $statements
);

// 配置参数
$config = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
    //'proxy' => null,  //设置网络请求代理,若不需要设置，则为null
    'secretId' => 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 云 API 密钥 secretId
    'secretKey' => 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 云 API 密钥 secretKey
    'policy' => $policy //策略
    )
);

//创建 sts
$sts = new STS();

// 获取临时密钥，计算签名
$tempKeys = $sts->getTempKeys($config);

echo json_encode($tempKeys);
```

### 返回结果

成功的话，可以拿到包含密钥的 JSON 文本：

```php
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
|sourcePrefix|string|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`|
|action|string| 授予 COS API 权限，如简单上传操作 name/cos:PutObject |
|scope|Scope| 构造policy的信息：由 action、bucket、region、sourcePrefix组成|

### 返回值说明
|字段|类型|描述|
| ---- | ---- | ---- |
|policy | array | 申请临时密钥所需的权限策略 |

### 使用示例
```php
include 'sts.php'

$scopes = array();
array_push($scopes,new Scope("name/cos:PutObject", "example-1250000000", "ap-guangzhou", "/1.txt"));
array_push($scopes, new Scope("name/cos:GetObject", "example-1250000000", "ap-guangzhou", "/dir/*"));

//创建 sts
$sts = new STS();
//获取policy
$policy= $sts->getPolicy($scopes);
echo str_replace('\\/', '/', json_encode($policy));
```
### 返回结果
```php
{
"version":"2.0",
"statement":[
	{
		"action":["name/cos:PutObject"],
		"effect":"allow",
		"resource":["qcs::cos:ap-guangzhou:uid/12500000:example-1250000000/1.txt"]
	},
	{
		"action":["name/cos:GetObject" ],
		"effect":"allow",
		"resource":["qcs::cos:ap-guangzhou:uid/12500000:example-1250000000/dir/*" ]
	}
]
}
```
