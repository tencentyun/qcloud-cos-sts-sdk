# qcloud-cos-sts

## 安装方法

```cmd
npm i qcloud-cos-sts --save
```

## 调用示例

请查看 [demo](https://github.com/tencentyun/qcloud-cos-sts-sdk/blob/master/nodejs/demo/demo.js) 里的示例。

## 接口说明

### `getCredential`

获取临时密钥接口(获取联合身份临时访问凭证)。

#### 参数说明

| 字段 | 类型 | 描述 | 必选 |
| ---- | ---- | ---- | ---- |
| options | Object | 表示当客户端的请求，最少需要什么样的权限，是一个键值对象的数组 | 是 |
| - secretId | String | 云 API 密钥 Id | 是 |
| - secretKey | String | 云 API 密钥 Key | 是 |
| - policy | Object | 要申请的临时密钥，限定的权限范围 | 是 |
| - durationSeconds | Number | 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200  | 否 |
| - proxy | String | 代理地址，如："http://proxy.example.com:8080" | 否 |
| - host | String | 可以通过改参数指定请求的域名 | 否 |
| - endpoint | String | 作用与host参数相同，二者选一即可 | 否 |
| callback | Function | 临时密钥获取完成后的回调方法 | 是 |

#### 返回值说明

| 字段 | 类型 | 描述 |
| ---- | ---- | ---- |
| err | Object | 错误信息 |
| data | Object | 返回的临时密钥内容 |
| - startTime | Number | 密钥的起始时间，是 UNIX 时间戳 |
| - expiredTime | Number | 密钥的失效时间，是 UNIX 时间戳 |
| - credentials | String | 云API |
| - - tmpSecretId | String | 临时密钥 Id，可用于计算签名 |
| - - tmpSecretKey | String | 临时密钥 Key，可用于计算签名 |
| - - sessionToken | String | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |

#### 返回示例

```json
{
    "credentials": {
        "tmpSecretId": "AKIDEPMQB_Q9Jt2fJxXyIekOzKZzx-sdGQgBga4TzsUdTWL9xlvsjInOHhCYFqfoKOY4",
        "tmpSecretKey": "W/3Lbl1YEW02mCoawIesl5kNehSskrSbp1cT1tgW70g=",
        "sessionToken": "c6xnSYAxyFbX8Y50627y9AA79u6Qfucw6924760b61588b79fea4c277b01ba157UVdr_10Y30bdpYtO8CXedYZe3KKZ_DyzaPiSFfNAcbr2MTfAgwJe-dhYhfyLMkeCqWyTNF-rOdOb0rp4Gto7p4yQAKuIPhQhuDd77gcAyGakC2WXHVd6ZuVaYIXBizZxqIHAf4lPiLHa6SZejSQfa_p5Ip2U1cAdkEionKbrX97xTKTcA_5Pu525CFSzHZIQibc2uNMZ-IRdQp12MaXZB6bxM6nB4xXH45mDIlbIGjaAsrtRJJ3csmf82uBKaJrYQoguAjBepMH91WcH87LlW9Ya3emNfVX7NMRRf64riYd_vomGF0TLgan9smEKAOdtaL94IkLvVJdhLqpvjBjp_4JCdqwlFAixaTzGJHdJzpGWOh0mQ6jDegAWgRYTrJvc5caYTz7Vphl8XoX5wHKKESUn_vqyTAid32t0vNYE034FIelxYT6VXuetYD_mvPfbHVDIXaFt7e_O8hRLkFwrdAIVaUml1mRPvccv2qOWSXs"
    },
    "expiration": "2019-08-07T08:54:35Z",
    "startTime": 1565166275,
    "expiredTime": 1565168075,
    "requestId":"3f8f5f69-1929-4f8f-9e47-ee0426a880ae"
}
```

### `getRoleCredential`

获取临时密钥接口(申请扮演角色)。

#### 参数说明

| 字段 | 类型 | 描述 | 必选 |
| ---- | ---- | ---- | ---- |
| options | Object | 表示当客户端的请求，最少需要什么样的权限，是一个键值对象的数组 | 是 |
| - secretId | String | 云 API 密钥 Id | 是 |
| - secretKey | String | 云 API 密钥 Key | 是 |
| - policy | Object | 要申请的临时密钥，限定的权限范围 | 是 |
| - roleArn | String | 角色的资源描述,[参考文档](https://cloud.tencent.com/document/product/1312/48197) | 是 |
| - durationSeconds | Number | 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200  | 否 |
| - proxy | String | 代理地址，如："http://proxy.example.com:8080" | 否 |
| - host | String | 可以通过改参数指定请求的域名 | 否 |
| - endpoint | String | 作用与host参数相同，二者选一即可 | 否 |
| callback | Function | 临时密钥获取完成后的回调方法 | 是 |

#### 返回值说明

| 字段 | 类型 | 描述 |
| ---- | ---- | ---- |
| err | Object | 错误信息 |
| data | Object | 返回的临时密钥内容 |
| - startTime | Number | 密钥的起始时间，是 UNIX 时间戳 |
| - expiredTime | Number | 密钥的失效时间，是 UNIX 时间戳 |
| - credentials | String | 云API |
| - - tmpSecretId | String | 临时密钥 Id，可用于计算签名 |
| - - tmpSecretKey | String | 临时密钥 Key，可用于计算签名 |
| - - sessionToken | String | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |

#### 返回示例

```json
{
    "credentials": {
        "tmpSecretId": "AKIDEPMQB_Q9Jt2fJxXyIekOzKZzx-sdGQgBga4TzsUdTWL9xlvsjInOHhCYFqfoKOY4",
        "tmpSecretKey": "W/3Lbl1YEW02mCoawIesl5kNehSskrSbp1cT1tgW70g=",
        "sessionToken": "c6xnSYAxyFbX8Y50627y9AA79u6Qfucw6924760b61588b79fea4c277b01ba157UVdr_10Y30bdpYtO8CXedYZe3KKZ_DyzaPiSFfNAcbr2MTfAgwJe-dhYhfyLMkeCqWyTNF-rOdOb0rp4Gto7p4yQAKuIPhQhuDd77gcAyGakC2WXHVd6ZuVaYIXBizZxqIHAf4lPiLHa6SZejSQfa_p5Ip2U1cAdkEionKbrX97xTKTcA_5Pu525CFSzHZIQibc2uNMZ-IRdQp12MaXZB6bxM6nB4xXH45mDIlbIGjaAsrtRJJ3csmf82uBKaJrYQoguAjBepMH91WcH87LlW9Ya3emNfVX7NMRRf64riYd_vomGF0TLgan9smEKAOdtaL94IkLvVJdhLqpvjBjp_4JCdqwlFAixaTzGJHdJzpGWOh0mQ6jDegAWgRYTrJvc5caYTz7Vphl8XoX5wHKKESUn_vqyTAid32t0vNYE034FIelxYT6VXuetYD_mvPfbHVDIXaFt7e_O8hRLkFwrdAIVaUml1mRPvccv2qOWSXs"
    },
    "expiration": "2019-08-07T08:54:35Z",
    "startTime": 1565166275,
    "expiredTime": 1565168075,
    "requestId":"3f8f5f69-1929-4f8f-9e47-ee0426a880ae"
}
```

### `getPolicy`

获取 policy 接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。

#### 参数说明

| 字段 | 类型 | 描述 | 必选 |
| ---- | ---- | ---- | ---- |
| scope | ObjectArray | 表示当客户端的请求，最少需要什么样的权限，是一个键值对象的数组 | 是 |
| - action | String | 操作名称，如 "name/cos:PutObject" | 是 |
| - bucket | String | 存储桶名称，格式：test-1250000000 | 是 |
| - region | String | 园区名称，如 ap-guangzhou | 是 |
| - prefix | String | 拼接 resource 字段所需的 key 前缀，客户端 SDK 默认传固定文件名如 "dir/1.txt"，支持 * 结尾如 "dir/*" | 是 |

#### 返回值说明

| 字段 | 类型 | 描述 |
| ---- | ---- | ---- |
| policy | Object | 申请临时密钥所需的权限策略 |

policy 具体格式请看 [文档](https://cloud.tencent.com/document/product/436/31923)

#### 返回示例

```json
{
    "version": "2.0",
    "statement": [{
        "action": [
            "name/cos:PutObject",
        ],
        "effect": "allow",
        "principal": {"qcs": ["*"]},
        "resource": [
            "qcs::cos:ap-guangzhou:uid/1250000000:prefix//1250000000/test/exampleobject"
        ]
    }]
}
```

## 更多示例

* `demo/demo.js` 是调用例子
* `demo/sts-server.js` 是临时密钥服务的例子
* `demo/sts-server-scope.js` 是临时密钥服务的例子，可以细粒度控制权限

您可以直接使用 `demo/sts-server.js` 或者 `demo/sts-server-scope.js`，修改配置参数，来搭建本地密钥服务器。

## 相关 SDK

* [cos-js-sdk-v5](https://github.com/tencentyun/cos-js-sdk-v5)
* [cos-nodejs-sdk-v5](https://github.com/tencentyun/cos-nodejs-sdk-v5)
