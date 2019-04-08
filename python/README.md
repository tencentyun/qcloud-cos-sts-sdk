## 获取 SDK
- 使用 pip 安装
    
    pip install -U qcloud-python-sts
   
- 源码安装

    拷贝 `sts/sts.py` 文件到您的 python 工程中。

## 查看示例

请查看 `demo/sts_demo.py` 文件，里面描述了如何调用SDK。

## 接口说明

### get_credential

获取临时密钥接口

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|secret_id|String| 云 API 密钥 Id|
|secret_key|String| 云 API 密钥 key|
|duration_seconds|long| 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200 |
|bucket|String| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|allow_prefix|String|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`|
|allow_actions|list| 授予 COS API 权限集合, 如简单上传操作：name/cos:PutObject|
|policy|dict| 策略：由 allow_actions、bucket、allow_prefix字段组成的描述授权的具体信息|

### 返回值说明

|字段|类型|描述|
| ---- | ---- | ---- |
|credentials | String | 临时密钥信息 |
|tmpSecretId | String | 临时密钥 Id，可用于计算签名 |
|tmpSecretKey | String | 临时密钥 Key，可用于计算签名 |
|sessionToken | String | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |
|startTime | String | 密钥的起止时间，是 UNIX 时间戳 |
|expiredTime | String | 密钥的失效时间，是 UNIX 时间戳 |

### 使用方法

调用代码如下：

```python
# 方式 一
from sts.sts import Sts

config = {
    # 临时密钥有效时长，单位是秒
    'duration_seconds': 1800,
    # 固定密钥
    'secret_id': 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 
    # 固定密钥
    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    # 是否需要设置代理
	'proxy': {
		'http': 'XXX',
		'https': 'XXX'
	},
    # 换成你的 bucket
    'bucket': 'test-1250000000', 
    # 换成 bucket 所在地区
    'region': 'ap-guangzhou',
    # 设置可操作的资源路径前缀，根据实际情况进行设置,如授予可操作所有的资源：则为 *； 如授予操作某个路径a下的所有资源，则为 a/*；如授予只能操作某个特定路径的文件 a/test.jpg， 则为 a/test.jpg
    'allow_prefix': '*', 
    # 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allow_actions': [
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
    ]

}

sts = Sts(config)
response = sts.get_credential()
print ('get data : ' + response.content.decode("unicode-escape"))

# 方式 二
policy = {'version': '2.0', 'statement': [{'action': ['name/cos:PutObject'], 'effect': 'allow', 
'principal': {'qcs': ['*']}, 'resource': ['qcs::cos:ap-guangzhou:uid/1250000000:prefix//1250000000/test/*']}]}
config = {
    # 临时密钥有效时长，单位是秒
    'duration_seconds': 1800,
    # 固定密钥
    'secret_id': 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 
    # 固定密钥
    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    # 是否需要设置代理
	'proxy': {
		'http': 'XXX',
		'https': 'XXX'
	},
	# 设置 策略 policy, 可通过 get_policy(list)获取
    'policy': policy

}

sts = Sts(config)
response = sts.get_credential()
print ('get data : ' + response.content.decode("unicode-escape"))
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

### get_policy

获取策略(policy)接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|bucket|String| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|resource_prefix|String|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`|
|action|String| 授予 COS API 权限，, 如简单上传操作：name/cos:PutObject|
|scope|Scope| 构造policy的信息：由 action、bucket、region、sourcePrefix组成|

### 返回值说明
|字段|类型|描述|
| ---- | ---- | ---- |
|policy | dict | 申请临时密钥所需的权限策略 |

### 使用示例
```python
from sts.sts import Sts, Scope
scopes = list()
scopes.append(Scope("name/cos:PutObject", "test-12500000", "ap-guangzhou", "/1.txt"));
scopes.append(Scope("name/cos:GetObject", "test-12500000", "ap-guangzhou", "/dir/*"));
policy = Sts.get_policy(scopes)
```
### 返回结果
```python
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
