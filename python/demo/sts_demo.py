#!/usr/bin/env python
# coding=utf-8
import json
import os

from sts.sts import Sts


if __name__ == '__main__':

    def get_credential_demo():
        config = {
            'url': 'https://sts.tencentcloudapi.com/',
            # 域名，非必须，默认为 sts.tencentcloudapi.com
            'domain': 'sts.tencentcloudapi.com', 
            # 临时密钥有效时长，单位是秒
            'duration_seconds': 1800,
            'secret_id': os.environ['COS_SECRET_ID'],
            # 固定密钥
            'secret_key': os.environ['COS_SECRET_KEY'],
            # 设置网络代理
            # 'proxy': {
            #     'http': 'xx',
            #     'https': 'xx'
            # },
            # 换成你的 bucket
            'bucket': 'example-1253653367',
            # 换成 bucket 所在地区
            'region': 'ap-guangzhou',
            # 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
            # 例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
            'allow_prefix': ['exampleobject', 'exampleobject2'],
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

    def get_role_credential_demo():
        config = {
            # 请求URL，域名部分必须和domain保持一致
            # 使用外网域名时：https://sts.tencentcloudapi.com/
            # 使用内网域名时：https://sts.internal.tencentcloudapi.com/
            'url': 'https://sts.internal.tencentcloudapi.com/',
            # 域名，非必须，默认为外网域名 sts.tencentcloudapi.com
            # 内网域名：sts.internal.tencentcloudapi.com
            'domain': 'sts.internal.tencentcloudapi.com', 
            # 临时密钥有效时长，单位是秒
            'duration_seconds': 1800,
            'secret_id': os.environ['COS_SECRET_ID'],
            # 固定密钥
            'secret_key': os.environ['COS_SECRET_KEY'],
            # 设置网络代理
            # 'proxy': {
            #     'http': 'xx',
            #     'https': 'xx'
            # },
            # 换成你的 bucket
            'bucket': 'example-1253653367',
            # 换成 bucket 所在地区
            'region': 'ap-guangzhou',
            # 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
            # 例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
            'allow_prefix': ['exampleobject', 'exampleobject2'], 
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
            role_arn = 'qcs::cam::uin/12345678:roleName/testRoleName' # 角色的资源描述，可在cam访问管理，点击角色名获取
            response = sts.get_role_credential(role_arn=role_arn)
            print('get data : ' + json.dumps(dict(response), indent=4))
        except Exception as e:
            print(e)
