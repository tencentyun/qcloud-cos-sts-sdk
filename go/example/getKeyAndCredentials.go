package main

import (
	"fmt"
	"github.com/tencentyun/qcloud-cos-sts-sdk/go"
	"math/rand"
	"os"
	"time"
)

type Config struct {
	ext             string
	appId           string
	SecretId        string
	SecretKey       string
	Proxy           string
	DurationSeconds int
	Bucket          string
	Region          string
	AllowActions    []string
}

type Permission struct {
	LimitExt           bool     `json:"limitExt"`
	ExtWhiteList       []string `json:"extWhiteList"`
	LimitContentType   bool     `json:"limitContentType"`
	LimitContentLength bool     `json:"limitContentLength"`
}

func generateCosKey(ext string) string {
	date := time.Now()
	m := int(date.Month()) + 1
	ymd := fmt.Sprintf("%d%02d%d", date.Year(), m, date.Day())
	r := fmt.Sprintf("%06d", rand.Intn(1000000))
	cosKey := fmt.Sprintf("file/%s/%s_%s.%s", ymd, ymd, r, ext)
	return cosKey
}

func getPermission() Permission {
	permission := Permission{
		LimitExt:           false,
		ExtWhiteList:       []string{"jpg", "jpeg", "png", "gif", "bmp"},
		LimitContentType:   false,
		LimitContentLength: false,
	}
	return permission
}

func getConfig() Config {
	config := Config{
		ext:             "jpg",
		appId:           "12500000000",
		SecretId:        os.Getenv("SECRETID"),  // 用户的 SecretId，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参考https://cloud.tencent.com/document/product/598/37140
		SecretKey:       os.Getenv("SECRETKEY"), // 用户的 SecretKey，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参考https://cloud.tencent.com/document/product/598/37140
		Proxy:           os.Getenv("Proxy"),
		DurationSeconds: 1800,
		Bucket:          "bucket-12500000000",
		Region:          "ap-guangzhou",
		AllowActions: []string{
			"name/cos:PutObject",
			"name/cos:InitiateMultipartUpload",
			"name/cos:ListMultipartUploads",
			"name/cos:ListParts",
			"name/cos:UploadPart",
			"name/cos:CompleteMultipartUpload",
		},
	}
	return config
}

func stringInSlice(str string, list []string) bool {
	for _, v := range list {
		if v == str {
			return true
		}
	}
	return false
}

func main() {

	config := getConfig()

	permission := getPermission()

	c := sts.NewClient(
		// 通过环境变量获取密钥, os.Getenv 方法表示获取环境变量
		config.SecretId,  //os.Getenv("SECRETID"),  // 用户的 SecretId，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参考https://cloud.tencent.com/document/product/598/37140
		config.SecretKey, //os.Getenv("SECRETKEY"),                 // 用户的 SecretKey，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参考https://cloud.tencent.com/document/product/598/37140
		nil,
		// sts.Host("sts.internal.tencentcloudapi.com"), // 设置域名, 默认域名sts.tencentcloudapi.com
		// sts.Scheme("http"),      // 设置协议, 默认为https，公有云sts获取临时密钥不允许走http，特殊场景才需要设置http
	)

	condition := make(map[string]map[string]interface{})

	if permission.LimitExt {
		extInvalid := config.ext == "" || !stringInSlice(config.ext, permission.ExtWhiteList)
		if extInvalid {
			fmt.Printf("%+v\n", "非法文件，禁止上传")
			return
		}
	}

	if permission.LimitContentType {
		condition["string_like"] = map[string]interface{}{
			// 只允许上传 content-type 为图片类型
			"cos:content-type": "image/*",
		}
	}

	// 3. 限制上传文件大小
	if permission.LimitContentLength {
		condition["numeric_less_than_equal"] = map[string]interface{}{
			// 上传大小限制不能超过 5MB
			"cos:content-length": 5 * 1024 * 1024,
		}
	}

	// 策略概述 https://cloud.tencent.com/document/product/436/18023
	opt := &sts.CredentialOptions{
		DurationSeconds: int64(config.DurationSeconds),
		Region:          config.Region,
		Policy: &sts.CredentialPolicy{
			Version: "2.0",
			Statement: []sts.CredentialPolicyStatement{
				{
					// 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
					Action: config.AllowActions,
					Effect: "allow",
					Resource: []string{
						// 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
						// 存储桶的命名格式为 BucketName-APPID，此处填写的 bucket 必须为此格式
						"qcs::cos:ap-guangzhou:uid/" + config.appId + ":" + config.Bucket + "/" + generateCosKey(config.ext),
					},
					// 开始构建生效条件 condition
					// 关于 condition 的详细设置规则和COS支持的condition类型可以参考https://cloud.tencent.com/document/product/436/71306
					Condition: condition,
				},
			},
		},
	}

	// case 1 请求临时密钥
	res, err := c.GetCredential(opt)
	if err != nil {
		panic(err)
	}
	fmt.Printf("%+v\n", res)
	fmt.Printf("%+v\n", res.Credentials)
}
