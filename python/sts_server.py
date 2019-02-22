# encoding: utf-8
import json

from flask import Flask, request
from sts import Sts, Scope

app = Flask(__name__)


@app.route('/sts')
def get_credential_response():
    config = {
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        'secret_id': 'AKIDPiqmW3qcgXVSKN8jngPzRhvxzYyDL5qP',
        # 固定密钥
        'secret_key': 'EH8oHoLgpmJmBQUM1Uoywjmv7EFzd5OJ',
        'proxy': {
            'http': 'web-proxy.tencent.com:8080',
            'https': 'web-proxy.tencent.com:8080'
        },
        # 换成你的 bucket
        'bucket': 'android-ut-persist-bucket-1253653367',
        # 换成 bucket 所在地区
        'region': 'ap-guangzhou',
        # 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 a/* 或者 a.jpg
        'allow_prefix': '*',
        # 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
        'allow_actions': [
            # 简单上传
            'name/cos:PutObject',
            # 分片上传
            'name/cos:InitiateMultipartUpload',
            'name/cos:ListMultipartUploads',
            'name/cos:ListParts',
            'name/cos:UploadPart',
            'name/cos:CompleteMultipartUpload'
        ],

    }

    sts = Sts(config)
    response = sts.get_credential()
    return str(response)


@app.route('/scopests', methods=['POST'])
def get_credential_response_with_scope():
    print(request.headers)
    content = request.form.get('scopes')
    _list = json.loads(content)
    print(_list)
    scopes = list()
    for l in _list:
        d = dict(l)
        scopes.append(Scope(action=d['action'], bucket=d['bucket'], region=d['region'], resource_prefix=d['prefix']))
    policy = Sts.get_policy(scopes)
    print(policy)
    config = {
        # 临时密钥有效时长，单位是秒
        'duration_seconds': 1800,
        'secret_id': 'AKIDPiqmW3qcgXVSKN8jngPzRhvxzYyDL5qP',
        # 固定密钥
        'secret_key': 'EH8oHoLgpmJmBQUM1Uoywjmv7EFzd5OJ',
        'proxy': {
            'http': 'web-proxy.tencent.com:8080',
            'https': 'web-proxy.tencent.com:8080'
        },
        'policy': policy
    }

    sts = Sts(config)
    response = sts.get_credential()
    return str(response)


if __name__ == '__main__':
    app.run()


