# -*- coding:utf-8 -*-

import hashlib
import hmac
import time
try:
    from urllib import quote  # Python 2.X
except ImportError:
    from urllib.parse import quote  # Python 3+
from functools import reduce
import json
import base64
import requests
import random


class Sts:
    secret_id = None
    secret_key = None
    duration_seconds = 1800
    bucket = None
    region = None
    resource = None
    allow_actions = None
    policy = None
    network_proxy = None
    sts_url = 'sts.tencentcloudapi.com/'
    sts_scheme = 'https'

    def __init__(self, config={}):
        self.parse_parameters(config)

    def parse_parameters(self, config):
        if not isinstance(config, dict):
            raise ValueError("config is not dict")
        keys = config.keys()
        resource_prefix = None
        for key in keys:
            key_lower = str(key).lower()
            if "secret_id" == key_lower:
                self.secret_id = config.get(key)
            elif "secret_key" == key_lower:
                self.secret_key = config.get(key)
            elif "bucket" == key_lower:
                self.bucket = config.get(key)
            elif "duration_seconds" == key_lower:
                self.duration_seconds = config.get(key)
            elif "region" == key_lower:
                self.region = config.get(key)
            elif "allow_prefix" == key_lower:
                resource_prefix = config.get(key)
            elif "policy" == key_lower:
                self.policy = config.get(key)
            elif "allow_actions" == key_lower:
                self.allow_actions = config.get(key)
            elif "proxy" == key_lower:
                self.network_proxy = config.get(key)
        if not isinstance(self.duration_seconds, int):
            raise ValueError('duration_seconds must be int type')
        # 若是policy 为空，则 bucket he resource_prefix 不为空
        if self.policy is None:
            if self.bucket is None:
                raise ValueError('bucket == None')
            if resource_prefix is None:
                raise ValueError("resource_prefix == None")
            split_index = self.bucket.rfind('-')
            if split_index < 0:
                raise ValueError('bucket is invalid: ' + self.bucket)
            appid = str(self.bucket[(split_index + 1):]).strip()
            if not str(resource_prefix).startswith('/'):
                resource_prefix = '/' + resource_prefix
            self.resource = "qcs::cos:{region}:uid/{appid}:{bucket}{prefix}".format(region=self.region,
                                                                                    appid=appid, bucket=self.bucket,
                                                                                    prefix=resource_prefix)

    @staticmethod
    def get_policy(scopes=[]):
        if not isinstance(scopes, list):
            return None
        policy = dict()
        policy['version'] = '2.0'
        statement = list()

        for scope in scopes:
            statement_element = dict()
            actions = list()
            resources = list()

            actions.append(scope.get_action())
            statement_element['action'] = actions

            statement_element['effect'] = scope.get_effect()

            resources.append(scope.get_resource())
            statement_element['resource'] = resources

            statement.append(statement_element)

        policy['statement'] = statement
        return policy

    def get_credential(self):
        try:
            import ssl
        except ImportError as e:
            raise e
        if self.policy is None:
            policy = {
                'version': '2.0',
                'statement': [
                    {
                        'action': self.allow_actions,
                        'effect': 'allow',
                        'resource': self.resource
                    }
                ]
            }
        else:
            policy = self.policy
        policy_encode = quote(json.dumps(policy))

        data = {
            'SecretId': self.secret_id,
            'Timestamp': int(time.time()),
            'Nonce': random.randint(100000, 200000),
            'Action': 'GetFederationToken',
            'Version': '2018-08-13',
            'DurationSeconds': self.duration_seconds,
            'Name': 'cos-sts-python',
            'Policy': policy_encode,
            'Region': self.region
        }
        data['Signature'] = self.__encrypt('POST', self.sts_url, data)
        result_json = None
        try:
            url = self.sts_scheme + '://' + self.sts_url
            response = requests.post(url, proxies=self.network_proxy, data=data)
            result_json = response.json()

            if isinstance(result_json['Response'], dict):
                result_json = result_json['Response']
       
            result_json['startTime'] = result_json['ExpiredTime'] - self.duration_seconds
            return self._backwardCompat(result_json)
        except Exception as e:
            result = "error"
            if result_json is not None:
                result = str(result_json)
                raise Exception("result: " + result, e)
            raise Exception("result: " + result, e)

    def __encrypt(self, method, url, key_values):
        source = Tools.flat_params(key_values)
        source = method + url + '?' + source
        try:
            key = bytes(self.secret_key) # Python 2.X
            source = bytes(source)
        except TypeError:
            key = bytes(self.secret_key, encoding='utf-8') # Python 3.X
            source = bytes(source, encoding='utf-8')
        sign = hmac.new(key, source, hashlib.sha1).digest()
        sign = base64.b64encode(sign).rstrip()
        return sign

    # v2接口的key首字母小写，v3改成大写，此处做了向下兼容
    def _backwardCompat(self, result_json):
        bc_json = dict()
        for k,v in result_json.items():
            if isinstance(v, dict):
                bc_json[k[0].lower() + k[1:]] = self._backwardCompat(v)
            elif k == 'Token':
                bc_json['sessionToken'] = v
            else:
                bc_json[k[0].lower() + k[1:]] = v
        
        return bc_json


class Tools(object):

    @staticmethod
    def _flat_key_values(a):
        return a[0] + '=' + str(a[1])

    @staticmethod
    def _link_key_values(a, b):
        return a + '&' + b

    @staticmethod
    def flat_params(key_values):
        key_values = sorted(key_values.items(), key=lambda d: d[0])
        return reduce(Tools._link_key_values, map(Tools._flat_key_values, key_values))


class Scope(object):
    action = None
    bucket = None
    region = None
    resource_prefix = None
    condition = None
    effect = 'allow'

    def __init__(self, action=None, bucket=None, region=None, resource_prefix=None):
        self.action = action
        self.bucket = bucket
        self.region = region
        self.resource_prefix = resource_prefix

    def set_bucket(self, bucket):
        self.bucket = bucket

    def set_region(self, region):
        self.region = region

    def set_action(self, action):
        self.action = action

    def set_resource_prefix(self, resource_prefix):
        self.resource_prefix = resource_prefix

    def is_allow(self, is_allow):
        if is_allow:
            self.effect = 'allow'
        else:
            self.effect = 'deny'

    def set_condition(self, condition):
        self.condition = condition

    def get_action(self):
        if self.action is None:
            raise ValueError('action == None')
        return self.action

    def get_resource(self):
        if self.bucket is None:
            raise ValueError('bucket == None')
        if self.resource_prefix is None:
            raise ValueError("resource_prefix == None")
        split_index = self.bucket.rfind('-')
        if split_index < 0:
            raise ValueError('bucket is invalid: ' + self.bucket)
        appid = str(self.bucket[(split_index + 1):]).strip()
        if not str(self.resource_prefix).startswith('/'):
            self.resource_prefix = '/' + self.resource_prefix
        resource = "qcs::cos:{region}:uid/{appid}:" \
                   "{bucket}{prefix}".format(region=self.region, appid=appid,
                                             bucket=self.bucket, prefix=self.resource_prefix)
        return resource

    def get_effect(self):
        return self.effect

    def get_dict(self):
        result = dict()
        result['action'] = self.action;
        result['bucket'] = self.bucket
        result['region'] = self.region
        result['prefix'] = self.resource_prefix
        result['effect'] = self.effect
        result['condition'] = self.condition
        return result

