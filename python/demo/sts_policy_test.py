#!/usr/bin/env python
# coding=utf-8
import json
import os

from sts.sts import Sts, Scope


def test_policy():
    scope = Scope('name/cos:PutObject', 'test-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2'])
    scopes = list()
    scopes.append(scope)
    print(json.dumps(Sts.get_policy(scopes), indent=4))


def test_policy2():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'example-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:GetObject', 'example-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'example-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:ListParts', 'example-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:UploadPart', 'example-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'example-1250000000', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:GetObject', 'example-1250000000', 'ap-guangzhou', ['1/test.txt', '2/test.txt']))
    print(json.dumps(Sts.get_policy(scopes), indent=4))


def test_sts():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'example-1253653367', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:GetObject', 'example-1253653367', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'example-1253653367', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:ListParts', 'example-1253653367', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:UploadPart', 'example-1253653367', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'example-1253653367', 'ap-guangzhou', ['exampleobject', 'exampleobject2']))
    scopes.append(Scope('name/cos:GetObject', 'example-1253653367', 'ap-guangzhou', ['1/test.txt', '2/test.txt']))
    config = {
        'sts_scheme': 'https',
        'sts_url': 'sts.tencentcloudapi.com/',
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        'secret_id': os.environ['COS_SECRET_ID'],
        # 固定密钥
        'secret_key': os.environ['COS_SECRET_KEY'],
        # 换成 bucket 所在地区
        'region': 'ap-guangzhou',
        #  设置网络代理
        # 'proxy': {
        #     'http': 'xxx',
        #     'https': 'xxx'
        # },
        'policy': Sts.get_policy(scopes)
    }

    sts = Sts(config)
    response = sts.get_credential()
    print('get data : ' + json.dumps(dict(response), indent=4))


if __name__ == '__main__':
    test_policy()
    test_policy2()
    test_sts()

