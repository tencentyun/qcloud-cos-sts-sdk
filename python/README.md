## 获取 SDK

拷贝 `sts.py` 文件到您的 python 工程中。

### 查看示例

请查看 `sts_demo.py` 文件，里面描述了如何调用SDK。

### 使用方法

调用代码如下：

```
from sts import Sts

config = {
    # 临时密钥有效时长，单位是秒
    'duration_seconds': 1800,
    # 固定密钥
    'secret_id': 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 
    # 固定密钥
    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    'proxy': '',
    # 换成你的 bucket
    'bucket': 'test-1250000000', 
    # 换成 bucket 所在地区
    'region': 'ap-guangzhou',
    # 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 a/* 或者 a.jpg
    'allow_prefix': '*', 
    # 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allow_actions': [
        # 简单上传
        'name/cos:PutObject',
        'name/cos:PostObject',
        # 分片上传
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    ]

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


