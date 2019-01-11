# 通过 STS 细粒度控制权限

## scope 定义

scope 是客户端和前端发请求前，整理出需要的最小权限描述。

scope 的格式是一个对象数组，格式说明如下：

| 字段 | 类型 | 描述 | 必选 |
| ---- | ---- | ---- | ---- |
| scope | ObjectArray | 表示当客户端的请求，最少需要什么样的权限，是一个键值对象的数组 | 是 |
| - action | String | 操作名称，如 "name/cos:PutObject" | 是 |
| - bucket | String | 存储桶名称，格式：test-1250000000 | 是 |
| - region | String | 园区名称，如 ap-guangzhou | 是 |
| - prefix | String | 拼接 resource 字段所需的 key 前缀，客户端 SDK 默认传固定文件名如 "dir/1.txt"，支持 * 结尾如 "dir/*" | 是 |

## 按 scope 获取临时密钥

1. 服务端先按照业务逻辑需要，按照 scope 的格式，判断什么操作、什么资源，控制是否允许放行。如果不允许返回错误，如果允许继续下一步。
2. 通过当前 SDK 每种语言提供的 STS.getPolicy 方法把 scope 转换成 policy 对象或字符串
3. 把得到的 policy 传给 STS.getCredential 方法获取临时密钥。得到
4. 将 STS.getCredential 返回的 startTime 、expiredTime、credentials 回传给客户端和前端

getCredential 和 getPolicy 其他字段详细说明可以看 [STS nodejs 接口文档](nodejs/README.md)

以下是 server 例子：
[sts nodejs scope demo](nodejs/sts-server-scope.js)
其他语言有待完善，可以参考 ndoejs 例子逻辑自行实现