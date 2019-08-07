#!/usr/bin/env python
# coding=utf-8
import json
import os

from sts.sts import Sts


if __name__ == '__main__':

    config = {
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        'secret_id': os.environ['GROUP_SECRET_ID'],
        # 固定密钥
        'secret_key': os.environ['GROUP_SECRET_KEY'],
        # 设置网络代理
        # 'proxy': {
        #     'http': 'xx',
        #     'https': 'xx'
        # },
        # 换成你的 bucket
        'bucket': 'example-1253653367',
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
        ],

    }

    try:
        sts = Sts(config)
        response = sts.get_credential()
        print('get data : ' + json.dumps(dict(response), indent=4))
    except Exception as e:
        print(e)


