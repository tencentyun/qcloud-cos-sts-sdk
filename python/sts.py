# -*- coding:utf-8 -*-

import hashlib
import hmac
import time
import urllib2
import urllib
import json
import requests
import random

class Sts:

    def __init__(self, config = {}):
        if config.has_key('allow_actions'):
            self.allow_actions = config.get('allow_actions')
        else:
            raise ValueError('missing allow_actions')

        if config.has_key('duration_seconds'):
            self.duration = config.get('duration_seconds')
        else:
            self.duration = 1800

        self.sts_domain = 'sts.api.qcloud.com'
        self.sts_url = 'sts.api.qcloud.com/v2/index.php'
        self.sts_scheme = 'https://'

        self.secret_id = config.get('secret_id')
        self.secret_key = config.get('secret_key')
        self.proxy = config.get('proxy')
        self.region = config.get('region')

        bucket = config['bucket']
        split_index = bucket.rfind('-')
        short_bucket_name = bucket[:split_index]
        appid = bucket[(split_index+1):]

        self.resource = "qcs::cos:{region}:uid/{appid}:prefix//{appid}/{short_bucket_name}/{allow_prefix}".format(
            region=config['region'],appid=appid,short_bucket_name=short_bucket_name,
            allow_prefix=config['allow_prefix']
        )

    def get_credential(self):
        try:
            import ssl
        except ImportError, e:
            raise e

        policy = {
            'version': '2.0',
            'statement': {
                'action': self.allow_actions,
                'effect': 'allow',
                'principal': {'qcs': '*'},
                'resource': self.resource
            }
        }
        policy_encode = urllib.quote(json.dumps(policy))

        data = {
            'Region':'',
            'SecretId':self.secret_id,
            'Timestamp':int(time.time()),
            'Nonce':random.randint(100000, 200000),
            'Action':'GetFederationToken',
            'durationSeconds':self.duration,
            'name':'cos-sts-python',
            'policy':policy_encode
        }
        data['Signature'] = self.__encrypt('POST', self.sts_url, data)

        try:
            response = requests.post(self.sts_scheme + self.sts_url, proxies=self.proxy, data=data)
            result_json = response.json()
            
            if isinstance(result_json['data'], dict):
                result_json = result_json['data']          
            result_json['startTime'] = result_json['expiredTime'] - self.duration
            
            return result_json
        except urllib2.HTTPError, e:
            raise e

    def __encrypt(self, method, url, key_values):
        source = Tools.flat_params(key_values)
        source = method + url + '?' + source
        sign = hmac.new(self.secret_key, source, hashlib.sha1).digest().encode('base64').rstrip()
        return sign


class Tools(object):

    @staticmethod
    def _flat_key_values(a):
        return a[0] + '=' + str(a[1])

    @staticmethod
    def _link_key_values(a, b):
        return a + '&' + b

    @staticmethod
    def flat_params(key_values):
        key_values = sorted(key_values.iteritems(), key=lambda d: d[0])
        return reduce(Tools._link_key_values, map(Tools._flat_key_values, key_values))