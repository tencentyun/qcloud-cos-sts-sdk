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
		os.Getenv("COS_SECRETID"),
		os.Getenv("COS_SECRETKEY"),
		nil,
	)
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
						"qcs::cos:ap-guangzhou:uid/" + appid + ":" + bucket + "/exampleobject",
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
