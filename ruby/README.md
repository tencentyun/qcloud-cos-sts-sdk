## 获取 SDK

拷贝 `sts.rb` 文件到您的 ruby或者rails  工程中。

### 查看示例

请查看 `sts_demo.rb` 文件，里面描述了如何调用SDK。

### 使用方法

* 注意: appid和bucket是分离的

调用代码如下：

```
require './sts'

config = {
    duration_seconds: 1800,
    secret_id: '',
    secret_key: '',
    appid: '',
    bucket: '',
    region: '',
    allow_prefix: '*',
    allow_actions: [
        'name/cos:PutObject',
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    ]
}


sts = Sts.new(config)
puts sts.get_credential

```

TODO: 有时间将其做成Gem


### 返回结果

成功的话，可以拿到包含密钥的 Hash：

```
{
    "credentials"=>{"token"=>"x-x-x-x-x-x-dZ0Z_0d90-x", "tmpsecretid"=>"x-x", "tmpsecretkey"=>"x="}, "expiredtime"=>1548331708, "requestid"=>"x-x-x-x-x", "starttime"=>1548329908}
```


