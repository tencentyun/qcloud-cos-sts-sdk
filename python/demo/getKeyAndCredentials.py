#!/usr/bin/env python
# coding=utf-8
import json
import os
import datetime
import random

from sts.sts import Sts


if __name__ == '__main__':

    # 配置参数
    config = {
        "ext":"jpg",
        "appId": "125000000",
        "secretId": os.getenv("SecretId"),
        "secretKey": os.getenv("SecretKey"),
        "proxy": os.getenv("Proxy"),
        "durationSeconds": 1800,
        "bucket": "bucket-125000000",
        "region": "ap-guangzhou",
        # 密钥的上传操作权限列表
        "allowActions": [
            # 简单上传
            "name/cos:PutObject",
            # 分块上传
            "name/cos:InitiateMultipartUpload",
            "name/cos:ListMultipartUploads",
            "name/cos:ListParts",
            "name/cos:UploadPart",
            "name/cos:CompleteMultipartUpload",
        ],
    }

    permission = {
        "limitExt": False,  # 限制上传文件后缀
        "extWhiteList": ["jpg", "jpeg", "png", "gif", "bmp"],  # 限制的上传后缀
        "limitContentType": False,  # 限制上传 contentType
        "limitContentLength": False,  # 限制上传文件大小
    }

    # 生成要上传的 COS 文件路径文件名
    def generate_cos_key(ext=None):
        date = datetime.datetime.now()
        ymd = date.strftime('%Y%m%d')
        r = str(int(random.random() * 1000000)).zfill(6)
        cos_key = f"file/{ymd}/{ymd}_{r}.{ext if ext else ''}"
        return cos_key

    resource = f"qcs::cos:{config['region']}:uid/{config['appId']}:{config['bucket']}/{generate_cos_key(config['ext'])}"

    condition = {}

    # 1. 限制上传文件后缀
    if permission["limitExt"]:
        ext_invalid = not config['ext'] or config['ext'] not in permission["extWhiteList"]
        if ext_invalid:
            print('非法文件，禁止上传')

    # 2. 限制上传文件 content-type
    if permission["limitContentType"]:
        condition.update({
            "string_like": {
                # 只允许上传 content-type 为图片类型
                "cos:content-type": "image/*"
            }
        })

    # 3. 限制上传文件大小
    if permission["limitContentLength"]:
        condition.update({
            "numeric_less_than_equal": {
                # 上传大小限制不能超过 5MB
                "cos:content-length": 5 * 1024 * 1024
            }
        })

    def get_credential_demo():
        credentialOption = {
            # 临时密钥有效时长，单位是秒
            'duration_seconds': config.get('durationSeconds'),
            'secret_id': config.get("secretId"),
            # 固定密钥
            'secret_key': config.get("secretKey"),
            # 换成你的 bucket
            'bucket': config.get("bucket"),
            'proxy': config.get("proxy"),
            # 换成 bucket 所在地区
            'region': config.get("region"),
            "policy":{
                "version": '2.0',
                "statement": [
                    {
                        "action": config.get("allowActions"),
                        "effect": "allow",
                        "resource": [
                            resource
                        ],
                        "condition": condition
                    }
                ],
            },
        }

        try:
            sts = Sts(credentialOption)
            response = sts.get_credential()
            print('get data : ' + json.dumps(dict(response), indent=4))
        except Exception as e:
            print(e)

    get_credential_demo()
