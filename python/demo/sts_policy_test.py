#!/usr/bin/env python
# coding=utf-8
import json

from sts.sts import Sts, Scope


def test_policy():
    scope = Scope('name/cos:PutObject', 'test-1250000000', 'ap-guangzhou', 'dir/*')
    scopes = list()
    scopes.append(scope)
    print(json.dumps(Sts.get_policy(scopes), indent=4))


def test_policy2():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:ListParts', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:UploadPart', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'example-1250000000', 'ap-guangzhou', '1/test.txt'))
    print(json.dumps(Sts.get_policy(scopes), indent=4))


def test_sts():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:ListParts', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:UploadPart', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'example-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'example-1250000000', 'ap-guangzhou', '1/test.txt'))
    config = {
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        # 固定密钥 id
        'secret_id': 'AKIDPXXX',
        # 固定密钥 key
        'secret_key': 'EH8xxx',
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

