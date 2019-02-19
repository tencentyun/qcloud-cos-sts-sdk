#!/usr/bin/env python
# coding=utf-8
from sts import Sts, Scope


def test_policy():
    scope = Scope('name/cos:PutObject', 'test-1250000000', 'ap-guangzhou', 'dir/*')
    print(Sts.get_policy(scope=scope))


def test_policy2():
    scope = Scope()
    scope.set_bucket('test-1250000000')
    scope.set_region('ap-guangzhou')
    scope.add_action('name/cos:PutObject')
    scope.add_action('name/cos:GetObject')
    scope.add_action('name/cos:InitiateMultipartUpload')
    scope.add_action('name/cos:ListMultipartUploads')
    scope.add_action('name/cos:ListParts')
    scope.add_action('name/cos:UploadPart')
    scope.add_action('name/cos:CompleteMultipartUpload')
    scope.add_resource_prefix('1/test.txt')
    scope.add_resource_prefix('test/*')
    print(Sts.get_policy(scope))


def test_sts():
    scope = Scope()
    scope.set_bucket('android-ut-persist-bucket-1253653367')
    scope.set_region('ap-guangzhou')
    scope.add_action('name/cos:PutObject')
    scope.add_action('name/cos:GetObject')
    scope.add_action('name/cos:InitiateMultipartUpload')
    scope.add_action('name/cos:ListMultipartUploads')
    scope.add_action('name/cos:ListParts')
    scope.add_action('name/cos:UploadPart')
    scope.add_action('name/cos:CompleteMultipartUpload')
    scope.add_resource_prefix('1/test.txt')
    scope.add_resource_prefix('test/*')
    config = {
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        # 固定密钥
        'secret_id': 'AKIDPiqmW3qcgXVSKN8jngPzRhvxzYyDL5qP',
        # 固定密钥
        'secret_key': 'EH8oHoLgpmJmBQUM1Uoywjmv7EFzd5OJ',
        'proxy': {
            'http': 'web-proxy.tencent.com:8080',
            'https': 'web-proxy.tencent.com:8080'
        },
        'policy': Sts.get_policy(scope)
    }

    sts = Sts(config)
    response = sts.get_credential()
    print('get data : ' + str(response))


if __name__ == '__main__':
    test_policy()
    test_policy2()
    test_sts()

