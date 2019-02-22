#!/usr/bin/env python
# coding=utf-8
from sts import Sts, Scope


def test_policy():
    scope = Scope('name/cos:PutObject', 'test-1250000000', 'ap-guangzhou', 'dir/*')
    scopes = list()
    scopes.append(scope)
    print(Sts.get_policy(scopes=scopes))


def test_policy2():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:ListParts', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:UploadPart', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'test-1250000000', 'ap-guangzhou', '1/test.txt'))
    print(Sts.get_policy(scopes))


def test_sts():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:ListParts', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:UploadPart', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'test-1250000000', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'test-1250000000', 'ap-guangzhou', '1/test.txt'))
    config = {
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        'secret_id': 'AKIDXXX',
        # 固定密钥
        'secret_key': 'EH8oXXX',
        'proxy': {
            'http': 'XXX',
            'https': 'XXX'
        },
        'policy': Sts.get_policy(scopes)
    }

    sts = Sts(config)
    response = sts.get_credential()
    print('get data : ' + str(response))


if __name__ == '__main__':
    test_policy()
    test_policy2()
    test_sts()

