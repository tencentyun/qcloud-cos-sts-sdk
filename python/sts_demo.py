#!/usr/bin/env python
# coding=utf-8

from sts import Sts
import json

if __name__ == '__main__':
    try:
        raw_config = json.load(open('key.json'))
        config = {}
        for key, value in raw_config.iteritems():
            s_key = key.encode('utf-8')
            if not isinstance(value, dict):
                config[s_key] = value.encode('utf-8')
            else:
                config[s_key] = {}
                for k, v in value.iteritems():
                    config[s_key][k.encode('utf-8')] = v.encode('utf-8')
        # 临时密钥有效时长，单位是秒
        config['duration_in_seconds'] = 1800
    except IOError:
        config = {
            # 临时密钥有效时长，单位是秒
            'duration_in_seconds': 1800,
            # 您的secret id
            'secret_id': 'xxx',
            # 您的secret key
            'secret_key': 'xxx',
        }

    sts = Sts(config)
    response = sts.get_credential()
    print ('get data : ' + response.content.decode("unicode-escape"))
