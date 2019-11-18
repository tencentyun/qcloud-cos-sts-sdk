# qcloud-cos-sts

## 安装方法

```cmd
dotnet add package Tencent.QCloud.Cos.Sts.Sdk
```

```PackageReference
<PackageReference Include="Tencent.QCloud.Cos.Sts.Sdk" Version="3.0.*" />
```

## 接口说明

### `getCredential`

获取临时密钥接口。

#### 使用示例

```C#
using COSSTS;

Dictionary<string, object> values = new Dictionary<string, object>();

string bucket = "bucketName-1250000000";  // 您的 bucket
string region = "ap-guangzhou";  // bucket 所在区域
string allowPrefix = "exampleobject"; // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
string[] allowActions = new string[] {  // 允许的操作范围，这里以上传操作为例
    "name/cos:PutObject",
    "name/cos:PostObject",
    "name/cos:InitiateMultipartUpload",
    "name/cos:ListMultipartUploads",
    "name/cos:ListParts",
    "name/cos:UploadPart",
    "name/cos:CompleteMultipartUpload"
};
string secretId = "your-secret-id"; // 云 API 密钥 Id
string secretKey = "your-secret-key"; // 云 API 密钥 Key

values.Add("bucket", bucket);
values.Add("region", region);
values.Add("allowPrefix", allowPrefix); 
values.Add("allowActions", allowActions);
values.Add("durationSeconds", 1800); // 要申请的临时密钥最长有效时间，单位秒，最大可设置 7200

values.Add("secretId", secretId);
values.Add("secretKey", secretKey);

string credential = STSClient.genCredential(values);
```

#### 返回示例

```json
{ 
   "Credentials":{ 
      "Token":"Sx5mIX9tS3qSZ1RISU3LhkrwslfUCTLL2de708fdd315fc52bf034e5b8e6c1f1bSoRV6ifryokZr5X7DGZi78Zu5UjkqpuHQszDcuB05Y3USR3_hYwt10tUX_KUktHIUSCxF6IkIZLSkRErV2SUer1Gzph-ouXuPvS66EOZAYCVMg4KZUcL0WvX_JAGFhIbbYG8so-3AES0TBqpCxaHuUgjle0OYvr60ge-PbtYuYp5LZlqCUSmAXpvs-v2Qkp6jYVo3uOzUwgfyBzVNo8HBrjIi8IFbEXcL1_Z9iVrY7YE4WNmkbjcEu5kDd8tgcrhHCHY1tCwrFCXlruQ4zr6mmsUwpo8TkwN2BT60Lw1vd_M9RjK7fd38ANBKPMwlBhixAyg2eVjrdBbsF748nkwB6dCIfUG1wy07ViCfIkqLRSTpUUXuWVh-z0OA0NW97hbYN2osCaZvmJTzB8iI-yoWoAWAk8QopcXtMT-X17IuYLEVXirM-jppGhcSDvYGqpGcIV1SGSVDd4aJYTpb_KgHBhpKRfGqccw6WPklH6GnE4",
      "TmpSecretId":"AKIDPWhQ5TbMphPVVaoVXDYerSadYq_N-nSK6l68wgIKVQqtEtfTjTW934kJWRC-Jll6",
      "TmpSecretKey":"afrk3zl9E89GbL3uCXYdzLyzQiWX+bn/YvF6L5RMnH0="
   },
   "ExpiredTime":1574079131,
   "RequestId":"3f8f5f69-1929-4f8f-9e47-ee0426a880ae"
}
```

## 相关 SDK

* [cos-dotnet-sdk](https://github.com/tencentyun/qcloud-sdk-dotnet)
