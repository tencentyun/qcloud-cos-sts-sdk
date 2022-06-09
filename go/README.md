## 获取 SDK
go get -u github.com/tencentyun/qcloud-cos-sts-sdk

## 查看示例

请查看 `example/sts_demo.go` 文件，里面描述了如何调用SDK。

## 接口说明

### NewClient

创建客户端。

```go
func NewClient(secretId, secretKey string, hc *http.Client, opt ...func(*Client)) *Client
```

#### 参数说明

|字段|类型|描述|
| ---- | ---- | ---- |
|secretId|String| 云 API 密钥 Id|
|secretKey|String| 云 API 密钥 key|
|hc|*http.Client| go http模块Client |
|opt|func(*Client)| Client配置项，可选, 可用于设置 Host 和 Scheme |

### NewClientWithCredential

通过 CredentialIface 创建客户端。
```go
func NewClientWithCredential(cred CredentialIface, hc *http.Client, opt ...func(*Client)) *Client
```

#### 参数说明
```go
type CredentialIface interface {
	GetSecretId() string
	GetSecretKey() string
	GetToken() string
}

```
|字段|类型|描述|
| ---- | ---- | ---- |
|cred |CredentialIface | 通过用户实现的CredentialIface接口获取密钥,用户需要自行保证接口线程安全 |
|hc|*http.Client| go http模块Client |
|opt|func(*Client)| Client配置项，可选, 可用于设置 Host 和 Scheme |

### GetCredential

[获取临时密钥接口](https://cloud.tencent.com/document/product/1312/48195)

```go
func (c *Client) GetCredential(opt *CredentialOptions) (*CredentialResult, error)
```
#### 参数说明

```go
type CredentialPolicyStatement struct {
    Action    []string                       
    Effect    string                          
    Resource  []string                         
    Condition map[string]map[string]interface{} 
}
type CredentialPolicy struct {
    Version   string                      
    Statement []CredentialPolicyStatement 
}
type CredentialOptions struct {
    Policy          *CredentialPolicy
    Region          string
    DurationSeconds int64
}
```

|字段|类型|描述||
| ---- | ---- | ---- | ---- |
|opt|*CredentialPolicy| 授权策略 | 是 |
| Version         |string| 策略语法版本，默认为2.0 | 否 |
|Action|[]string| 此处是指 COS API，根据需求指定一个或者一序列操作的组合或所有操作(*) | 是 |
|Effect|string| 有 allow （允许）和 deny （显式拒绝）两种情况 | 是 |
|Resource|[]string| 授权操作的具体数据，可以是任意资源、指定路径前缀的资源、指定绝对路径的资源或它们的组合 | 是 |
|Condition|map|约束条件，可以不填，具体说明请参见 [condition](https://cloud.tencent.com/document/product/598/10603#6.-.E7.94.9F.E6.95.88.E6.9D.A1.E4.BB.B6.EF.BC.88condition.EF.BC.89) 说明|否|
|Region|string| STS云API的地域，建议与存储桶地域保持一致 | 否 |
|DurationSeconds|int64| 指定临时证书的有效期，单位：秒，默认1800秒，最长可设定有效期为7200秒。 | 否 |

### 返回值说明

``` go
type Credentials struct {
    TmpSecretID  string 
    TmpSecretKey string 
    SessionToken string 
}
type CredentialResult struct {
    Credentials *Credentials 
    StartTime   int
    ExpiredTime int          
    Expiration  string                 
    RequestId   string  
}
```

|字段|类型|描述|
| ---- | ---- | ---- |
|Credentials | *Credentials | 临时密钥信息 |
|TmpSecretID | string | 临时密钥 Id，可用于计算签名 |
|TmpSecretKey | string | 临时密钥 Key，可用于计算签名 |
|SessionToken | string | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |
|StartTime | int | 密钥的起始时间，是 UNIX 时间戳 |
|ExpiredTime | int | 密钥的失效时间，是 UNIX 时间戳 |

### 使用方法

调用代码如下：

```go
package main

import (
	"fmt"
	"github.com/tencentyun/qcloud-cos-sts-sdk/go"
	"os"
	"time"
)

func main() {
	appid := "1259654469"
	bucket := "test-1259654469"
	region := "ap-guangzhou"
	c := sts.NewClient(
		// 通过环境变量获取密钥
		os.Getenv("SECRETID"),
		os.Getenv("SECRETKEY"),
		nil,
		// sts.Host("sts.internal.tencentcloudapi.com"), // 设置域名, 默认域名sts.tencentcloudapi.com
		// sts.Scheme("http"),      // 设置协议, 默认为https，公有云sts获取临时密钥不允许走http，特殊场景才需要设置http
	)
	// 策略概述 https://cloud.tencent.com/document/product/436/18023
	opt := &sts.CredentialOptions{
		DurationSeconds: int64(time.Hour.Seconds()),
		Region:          "ap-guangzhou",
		Policy: &sts.CredentialPolicy{
			Statement: []sts.CredentialPolicyStatement{
				{
					Action: []string{
						"name/cos:PostObject",
						"name/cos:PutObject",
					},
					Effect: "allow",
					Resource: []string{
						//这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
						"qcs::cos:" + region + ":uid/" + appid + ":" + bucket + "/exampleobject",
					},
				},
			},
		},
	}
	res, err := c.GetCredential(opt)
	if err != nil {
		panic(err)
	}
	fmt.Printf("%+v\n", res)
	fmt.Printf("%+v\n", res.Credentials)
}
```
### GetRoleCredential

[申请扮演角色](https://cloud.tencent.com/document/product/1312/48197)临时密钥接口。

```go
func (c *Client) GetRoleCredential(opt *CredentialOptions) (*CredentialResult, error)
```
#### 参数说明

```go
type CredentialPolicyStatement struct {
    Action    []string                       
    Effect    string                          
    Resource  []string                         
    Condition map[string]map[string]interface{} 
}
type CredentialPolicy struct {
    Version   string                      
    Statement []CredentialPolicyStatement 
}
type CredentialOptions struct {
    Policy          *CredentialPolicy
    Region          string
    DurationSeconds int64
    RoleArn         string
    RoleSessionName string
    ExternalId      string
}
```

|字段|类型|描述|必选|
| ---- | ---- | ---- | ---- |
|opt|*CredentialPolicy| 授权策略 | 是 |
| Version         |string| 策略语法版本，默认为2.0 | 否 |
|Action|[]string| 此处是指 COS API，根据需求指定一个或者一序列操作的组合或所有操作(*) | 是 |
|Effect|string| 有 allow （允许）和 deny （显式拒绝）两种情况 | 是 |
|Resource|[]string| 授权操作的具体数据，可以是任意资源、指定路径前缀的资源、指定绝对路径的资源或它们的组合 | 是 |
|Condition|map|约束条件，可以不填，具体说明请参见 [condition](https://cloud.tencent.com/document/product/598/10603#6.-.E7.94.9F.E6.95.88.E6.9D.A1.E4.BB.B6.EF.BC.88condition.EF.BC.89) 说明|否|
|Region|string| STS云API的地域，建议与存储桶地域保持一致 | 否 |
|DurationSeconds|int64| 指定临时证书的有效期，单位：秒，默认1800秒，最长可设定有效期为7200秒。 | 否 |
|RoleArn|String| 角色的资源描述，可在[访问管理](https://console.cloud.tencent.com/cam/role)，点击角色名获取。 普通角色： qcs::cam::uin/12345678:role/4611686018427397919、qcs::cam::uin/12345678:roleName/testRoleName 服务角色： qcs::cam::uin/12345678:role/tencentcloudServiceRole/4611686018427397920、qcs::cam::uin/12345678:role/tencentcloudServiceRoleName/testServiceRoleName | 是 |
|RoleSessionName|String| 临时会话名称，由用户自定义名称。 长度在2到128之间，可包含大小写字符，数字以及特殊字符：=,.@*-。 正则为：[\w+=,.@*-]* | 是 |
|ExternalId|String| 角色外部ID，可在[访问管理](https://console.cloud.tencent.com/cam/role)，点击角色名获取。 长度在2到128之间，可包含大小写字符，数字以及特殊字符：=,.@:/-。 正则为：[\w+=,.@:/-]* | 否 |

### 返回值说明

``` go
type Credentials struct {
    TmpSecretID  string 
    TmpSecretKey string 
    SessionToken string 
}
type CredentialResult struct {
    Credentials *Credentials 
    StartTime   int
    ExpiredTime int          
    Expiration  string                 
    RequestId   string  
}
```

|字段|类型|描述|
| ---- | ---- | ---- |
|Credentials | *Credentials | 临时密钥信息 |
|TmpSecretID | string | 临时密钥 Id，可用于计算签名 |
|TmpSecretKey | string | 临时密钥 Key，可用于计算签名 |
|SessionToken | string | 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 |
|StartTime | int | 密钥的起始时间，是 UNIX 时间戳 |
|ExpiredTime | int | 密钥的失效时间，是 UNIX 时间戳 |

### 使用方法

调用代码如下：

```go
package main

import (
        "fmt"
        "github.com/tencentyun/qcloud-cos-sts-sdk/go"
        "os"
        "time"
)

func main() {
        appid := "1259654469"
        bucket := "test-1259654469"
        c := sts.NewClient(
                // 通过环境变量获取密钥
                os.Getenv("SECRETID"),
                os.Getenv("SECRETKEY"),
                nil,
                // sts.Host("sts.internal.tencentcloudapi.com"), // 设置域名, 默认域名sts.tencentcloudapi.com
                // sts.Scheme("http"),      // 设置协议, 默认为https，公有云sts获取临时密钥不允许走http，特殊场景才需要设置http
        )
        // 发起角色授权临时密钥请求, policy选填
        // 策略概述 https://cloud.tencent.com/document/product/436/18023
        opt := &sts.CredentialOptions{
                DurationSeconds: int64(time.Hour.Seconds()),
                Region:          "ap-guangzhou",
                Policy: &sts.CredentialPolicy{
                        Statement: []sts.CredentialPolicyStatement{
                                {
                                        // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
                                        Action: []string{
                                                // 简单上传
                                                "name/cos:PostObject",
                                                "name/cos:PutObject",
                                                // 分片上传
                                                "name/cos:InitiateMultipartUpload",
                                                "name/cos:ListMultipartUploads",
                                                "name/cos:ListParts",
                                                "name/cos:UploadPart",
                                                "name/cos:CompleteMultipartUpload",
                                        },
                                        Effect: "allow",
                                        Resource: []string{
                                                //这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
                                                "qcs::cos:ap-guangzhou:uid/" + appid + ":" + bucket + "/exampleobject",
                                        },
                                },
                        },
                },
                RoleArn:         "qcs::cam::uin/100010805041:roleName/COSBatch_QCSRole",
                RoleSessionName: "test",
        }

        res, err := c.GetRoleCredential(opt)
        if err != nil {
                panic(err)
        }
        fmt.Printf("%+v\n", res)
        fmt.Printf("%+v\n", res.Credentials)
}
```
