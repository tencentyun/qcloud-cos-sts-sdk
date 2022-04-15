## 获取 SDK

- composer 安装

```
创建composer.json的文件，内容如下：
{
    "require":{
        "qcloud_sts/qcloud-sts-sdk": "3.0.*"
    }
}
```

## 查看SDK源码

PHP SDK 源码已迁移到 [COS STS PHP SDK](https://github.com/tencentyun/qcloud-cos-sts-php-sdk)。

## 查看使用示例

请查看 [demo 示例](https://github.com/tencentyun/qcloud-cos-sts-sdk/tree/master/php/demo)，里面描述了如何调用SDK。

- 外网用户获取临时密钥请参考[获取临时密钥](https://github.com/tencentyun/qcloud-cos-sts-sdk/blob/master/php/demo/sts_test.php)
- 外网用户申请扮演角色请参考[申请扮演角色](https://github.com/tencentyun/qcloud-cos-sts-sdk/blob/master/php/demo/sts_role_test.php)
- 内网用户相关操作请参考[获取临时密钥/申请角色](https://github.com/tencentyun/qcloud-cos-sts-sdk/blob/master/php/demo/sts_internal_test.php)


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
|startTime | string | 密钥的起始时间，是 UNIX 时间戳 |
|expiredTime | string | 密钥的失效时间，是 UNIX 时间戳 |

### 返回数据示例

```
{
	"expiredTime": 1589258683,
	"expiration": "2020-05-12T04:44:43Z",
	"credentials": {
		"sessionToken": "Biypn6exa48PpMe7wFerEnNMpBKKPQo180c57e0a5275ebae506d7851a85f36a4P0TV5UFR3FYJjsoZA1tk6uRKoDRzc6-60BmwLqdS75OhjHEa7GlVYpL_ofKQJTpPKziKX7FnI10D_6qtLdjzf2NdsyUtQEd5kPpDCOQJZn9-BpleqWQe8oyH_2u7xi2f0FtjCYaoGIZ_lUqlILXQwr0B0t3hLfL4xNE-EmVjUlUXa16HxVCn4_hJetqo9LmI0AgLOjCbYx9aVrsV10eDsRta-TQSIXmJNP3aJ6oz8d8GBTgTE1I2qSFDnv9pjtQKW8HZWI_glPIfmHXCCwAESxEFL_owGz839Va0qYhF6LkfVmsuoU1zNcvJR1w3cIE6izV3SKHaOtWaew3IOervuOPoN3S2oYGNwv2EavtDAWyUBIeI7X6nMVzlpnyJ-3bkIhOq9QVIQAs8wh5A0u9mvKWugT5t6qgyEgvEZSj9k6p-JjwxMgLC6s5uK1i_nnf4fN7ZQ6I-JAfHnH4jEDiVtJgXqfuWPX_vnzskyR2Co6E",
		"tmpSecretId": "AKIDTRPc-oe6c_avPSRwFVsPDyy3IoAr3szMajlOGuoEXY1232YLy6j4f-xZ5zL-NBMG",
		"tmpSecretKey": "2v29SZztGYk6SGwHYm\/chJXdD3zPRFasmPoJiCmlR\/I="
	},
	"requestId": "69ef6295-b981-464d-9816-9c2ef92189d1",
	"startTime": 1589256883
}
```

### getRoleCredential

申请扮演角色

### 参数说明

|字段|类型|描述|必选|
| ---- | ---- | ---- | ----|
|roleArn|string|角色的资源描述，可在 [访问管理](https://console.cloud.tencent.com/cam/role) 点击角色名获取。| 是 |
|secretId|string| 云 API 密钥 Id| 是 |
|secretKey|string| 云 API 密钥 key| 是 |
|endpoint|string| 接入点，内网填写"internal.tencentcloudapi.com"，外网填写"tencentcloudapi.com"| 是 |
|durationSeconds|long| 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200 | 否 |
|bucket|string| 存储桶名称：bucketName-appid, 如 test-125000000| 是 |
|region|string| 存储桶所属地域，如 ap-guangzhou| 是 |
|allowPrefix|string|资源的前缀，如授予操作所有资源，则为`*`；如授予操作某个路径a下的所有资源,则为 `a/*`，如授予只能操作特定的文件a/test.jpg, 则为`a/test.jpg`| 是 |
|allowActions|array| 授予 COS API 权限集合, 如简单上传操作：name/cos:PutObject| 是 |
|policy|array| 策略：由 allowActions、bucket、region、allowPrefix字段组成的描述授权的具体信息| 否 |
|externalId|string| 角色外部ID| 否 |

### 返回值说明

|字段|类型|描述|
| ---- | ---- | ---- |
|credentials | string | 临时密钥信息 |
|tmpSecretId | string | 临时密钥 Id，可用于计算签名 |
|tmpSecretKey | string | 临时密钥 Key，可用于计算签名 |
|sessionToken | string | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |
|startTime | string | 密钥的起始时间，是 UNIX 时间戳 |
|expiredTime | string | 密钥的失效时间，是 UNIX 时间戳 |

### 返回数据示例

```
{
  "Response": {
    "Credentials": {
      "Token": "da1e9d2ee9dda83506832d5ecb903b790132dfe340001",
      "TmpSecretId": "AKID65zyIP0mpXtaI******WIQVMn1umNH58",
      "TmpSecretKey": "q95K84wrzuEGoc*******52boxvp71yoh"
    },
    "ExpiredTime": 1543914376,
    "Expiration": "2018-12-04T09:06:16Z",
    "RequestId": "4daec797-9cd2-4f09-9e7a-7d4c43b2a74c"
  }
}
```
