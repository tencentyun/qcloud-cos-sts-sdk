# qcloud-cos-sts

## 安装方法

```
dotnet add package Tencent.QCloud.Cos.Sts.Sdk
```

```PackageReference
<PackageReference Include="Tencent.QCloud.Cos.Sts.Sdk" Version="3.0.*" />
```

## 查看使用示例

请查看 [demo 示例](https://github.com/tencentyun/qcloud-cos-sts-sdk/tree/master/dotnet/demo)，里面描述了如何调用SDK。

#### 返回示例

```json
{ 
   "Credentials":{ 
      "Token":"Sx5mIX9tS3qSZ1RISU3LhkrwslfUCTLL2de708fdd315fc52bf034e5b8e6c1f1bSoRV6ifryokZr5X7DGZi78Zu5UjkqpuHQszDcuB05Y3USR3_hYwt10tUX_KUktHIUSCxF6IkIZLSkRErV2SUer1Gzph-ouXuPvS66EOZAYCVMg4KZUcL0WvX_JAGFhIbbYG8so-3AES0TBqpCxaHuUgjle0OYvr60ge-PbtYuYp5LZlqCUSmAXpvs-v2Qkp6jYVo3uOzUwgfyBzVNo8HBrjIi8IFbEXcL1_Z9iVrY7YE4WNmkbjcEu5kDd8tgcrhHCHY1tCwrFCXlruQ4zr6mmsUwpo8TkwN2BT60Lw1vd_M9RjK7fd38ANBKPMwlBhixAyg2eVjrdBbsF748nkwB6dCIfUG1wy07ViCfIkqLRSTpUUXuWVh-z0OA0NW97hbYN2osCaZvmJTzB8iI-yoWoAWAk8QopcXtMT-X17IuYLEVXirM-jppGhcSDvYGqpGcIV1SGSVDd4aJYTpb_KgHBhpKRfGqccw6WPklH6GnE4",
      "TmpSecretId":"AKIDPWhQ5TbMphPVVaoVXDYerSadYq_N-nSK6l68wgIKVQqtEtfTjTW934kJWRC-Jll6",
      "TmpSecretKey":"afrk3zl9E89GbL3uCXYdzLyzQiWX+bn/YvF6L5RMnH0="
   },
   "Expiration": "2019-08-07T08:54:35Z",
   "ExpiredTime":1574079131,
   "StartTime":1574077331,
   "RequestId":"3f8f5f69-1929-4f8f-9e47-ee0426a880ae"
}
```

## 相关 SDK

* [cos-dotnet-sdk](https://github.com/tencentyun/qcloud-sdk-dotnet)
