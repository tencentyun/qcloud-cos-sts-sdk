# encoding: utf-8
import json

import requests

from sts.sts import Scope


def request_sts():
    scopes = list()
    scopes.append(Scope('name/cos:PutObject', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:InitiateMultipartUpload', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:ListParts', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:UploadPart', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:CompleteMultipartUpload', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', 'dir/*'))
    scopes.append(Scope('name/cos:GetObject', 'android-ut-persist-bucket-1253653367', 'ap-guangzhou', '1/test.txt'))
    content = list()
    for element in scopes:
        content.append(element.get_dict())
    user_info = {'scopes': json.dumps(content)}

    r = requests.post("http://127.0.0.1:5000/scopests", data=user_info)
    print(r.text)


def request_sts2():
    r = requests.get("http://127.0.0.1:5000/sts")
    print(r.text)


if __name__ == '__main__':
    request_sts()
    # request_sts2()
