const axios = require('axios');
const qs = require('qs');
const crypto = require('crypto');

const StsUrl = 'https://{host}/';

const util = {
    // 获取随机数
    getRandom: (min, max) => {
        return Math.round(Math.random() * (max - min) + min);
    },
    // obj 转 query string
    json2str: (obj, $notEncode) => {
        return Object.keys(obj)
            .sort()
            .map((item) => {
                const val = obj[item] || '';
                return `${item}=${$notEncode ? encodeURIComponent(val) : val}`;
            })
            .join('&');
    },
    // 计算签名
    getSignature: (opt, key, method, stsDomain) => {
        const formatString = `${method}${stsDomain}/?${util.json2str(opt)}`;
        const hmac = crypto.createHmac('sha1', key);
        const sign = hmac.update(Buffer.from(formatString, 'utf8')).digest('base64');
        return sign;
    },
    // v2接口的key首字母小写，v3改成大写，此处做了向下兼容
    backwardCompat: (data) => {
        let compat = {};
        for (const key in data) {
            if (typeof data[key] == 'object') {
                compat[util.lowerFirstLetter(key)] = util.backwardCompat(data[key]);
            } else if (key === 'Token') {
                compat['sessionToken'] = data[key];
            } else {
                compat[util.lowerFirstLetter(key)] = data[key];
            }
        }

        return compat;
    },
    lowerFirstLetter: (source) => {
        return source.charAt(0).toLowerCase() + source.slice(1);
    },
};

// 拼接获取临时密钥的参数
const _getCredential = (options, callback) => {
    if (options.durationInSeconds !== undefined) {
        console.warn('warning: durationInSeconds has been deprecated, Please use durationSeconds ).');
    }

    const { secretId, secretKey, policy } = options;
    const proxy = options.proxy || '';
    const region = options.region || 'ap-beijing';
    const host = options.host || '';

    const durationSeconds = options.durationSeconds || options.durationInSeconds || 1800;

    const endpoint = options.host || options.endpoint || 'sts.tencentcloudapi.com';

    const policyStr = JSON.stringify(policy);
    const action = options.action || 'GetFederationToken'; // 默认GetFederationToken
    const nonce = util.getRandom(10000, 20000);
    const timestamp = Math.floor(Date.now() / 1000);
    const method = 'POST';
    const name = 'cos-sts-nodejs'; // 临时会话名称

    const params = {
        SecretId: secretId,
        Timestamp: timestamp,
        Nonce: nonce,
        Action: action,
        DurationSeconds: durationSeconds,
        Version: '2018-08-13',
        Region: region,
        Policy: encodeURIComponent(policyStr),
    };
    if (action === 'AssumeRole') {
        params.RoleSessionName = name;
        params.RoleArn = options.roleArn;
    } else {
        params.Name = name;
    }
    params.Signature = util.getSignature(params, secretKey, method, endpoint);

    const opt = {
        method,
        url: StsUrl.replace('{host}', endpoint),
        headers: {
            'accept': 'application/json',
            'content-type': 'application/x-www-form-urlencoded',
            'Host': endpoint,
        },
        data: qs.stringify(params),
        proxy,
    };
    axios(opt)
        .then((res) => {
            let data = res.data.Response;
            if (data.Error) {
                const RequestId = data?.RequestId || '';
                const res = Object.assign(data.Error, { RequestId });
                return callback(res);
            }
            data.startTime = data.ExpiredTime - durationSeconds;
            data = util.backwardCompat(data);
            callback(null, data);
        })
        .catch((err) => {
            callback(err);
        });
};

// 获取联合身份临时访问凭证 GetFederationToken
const getCredential = (opt, callback) => {
    Object.assign(opt, { action: 'GetFederationToken' });
    if (callback) return _getCredential(opt, callback);
    return new Promise((resolve, reject) => {
        _getCredential(opt, (err, data) => {
            err ? reject(err) : resolve(data);
        });
    });
};

// 申请扮演角色 AssumeRole
const getRoleCredential = (opt, callback) => {
    Object.assign(opt, { action: 'AssumeRole' });
    if (callback) return _getCredential(opt, callback);
    return new Promise((resolve, reject) => {
        _getCredential(opt, (err, data) => {
            err ? reject(err) : resolve(data);
        });
    });
};

const getPolicy = (scope) => {
    // 定义绑定临时密钥的权限策略
    const statement = scope.map((item) => {
        const { prefix } = item;
        const action = item.action || '';
        const bucket = item.bucket || '';
        const region = item.region || '';

        const shortBucketName = bucket.substring(0, bucket.lastIndexOf('-'));
        const appId = bucket.substring(1 + bucket.lastIndexOf('-'));

        let resource = `qcs::cos:${region}:uid/${appId}:prefix//${appId}/${shortBucketName}/${prefix}`;
        if (action === 'name/cos:GetService') {
            resource = '*';
        }
        return {
            'action': action,
            'effect': 'allow',
            'principal': { 'qcs': '*' },
            'resource': resource,
        };
    });
    return { 'version': '2.0', 'statement': statement };
};

const cosStsSdk = {
    getCredential,
    getRoleCredential,
    getPolicy,
};

module.exports = cosStsSdk;
