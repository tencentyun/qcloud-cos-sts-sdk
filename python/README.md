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
|allow_prefix|String|资源的前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)|
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


# coding=utf-8
from sts.sts import Sts
import json

# 方式 一
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
    'bucket': 'example-1250000000', 
    # 换成 bucket 所在地区
    'region': 'ap-guangzhou',
    # 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
    # 例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
    'allow_prefix': 'exampleobject', 
    # 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allow_actions': [
        # 简单上传
        'name/cos:PutObject',
		# 表单上传
        'name/cos:PostObject',
        # 分片上传： 初始化分片
        'name/cos:InitiateMultipartUpload',
		# 分片上传： 查询 bucket 中未完成分片上传的UploadId
        "name/cos:ListMultipartUploads",
		# 分片上传： 查询已上传的分片
        "name/cos:ListParts",
		# 分片上传： 上传分片块
        "name/cos:UploadPart",
		# 分片上传： 完成分片上传
        "name/cos:CompleteMultipartUpload"
    ]

}

sts = Sts(config)
response = sts.get_credential()
print ('get data : ' + json.dumps(dict(response), indent=4))

# 方式 二
policy = {'version': '2.0', 'statement': [{'action': ['name/cos:PutObject'], 'effect': 'allow', 
 'resource': ['qcs::cos:ap-guangzhou:uid/1250000000:example-1250000000/exampleobject']}]}
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
print ('get data : ' + json.dumps(dict(response), indent=4))
```

### 返回结果

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

### get_policy

获取策略(policy)接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。

### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|bucket|String| 存储桶名称：bucketName-appid, 如 test-125000000|
|region|String| 存储桶所属地域，如 ap-guangzhou|
|resource_prefix|String|资源的前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用) |
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
scopes.append(Scope("name/cos:PutObject", "example-1250000000", "ap-guangzhou", "/1.txt"));
scopes.append(Scope("name/cos:GetObject", "example-1250000000", "ap-guangzhou", "/dir/exampleobject"));
policy = Sts.get_policy(scopes)
```
### 返回结果
```python
{
"version":"2.0",
"statement":[
	{
		"action":["name/cos:PutObject"],
		"effect":"allow",
		"resource":["qcs::cos:ap-guangzhou:uid/1250000000:example-1250000000/1.txt"]
	},
	{
		"action":["name/cos:GetObject" ],
		"effect":"allow",
		"resource":["qcs::cos:ap-guangzhou:uid/1250000000:example-1250000000/dir/exampleobject" ]
	}
]
}
```
