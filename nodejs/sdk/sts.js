var request= require('request');
var crypto= require('crypto');

var StsUrl = 'https://{host}/';

var util = {
    // 获取随机数
    getRandom: function (min, max) {
        return Math.round(Math.random() * (max - min) + min);
    },
    // obj 转 query string
    json2str: function (obj, $notEncode) {
        var arr = [];
        Object.keys(obj).sort().forEach(function (item) {
            var val = obj[item] || '';
            arr.push(item + '=' + ($notEncode ? encodeURIComponent(val) : val));
        });
        return arr.join('&');
    },
    // 计算签名
    getSignature: function (opt, key, method, stsDomain) {
        var formatString = method + stsDomain + '/?' + util.json2str(opt);
        var hmac = crypto.createHmac('sha1', key);
        var sign = hmac.update(Buffer.from(formatString, 'utf8')).digest('base64');
        return sign;
    },
    // v2接口的key首字母小写，v3改成大写，此处做了向下兼容
    backwardCompat: function (data) {
        var compat = {};
        for (var key in data) {
            if (typeof(data[key]) == 'object') {
                compat[this.lowerFirstLetter(key)] = this.backwardCompat(data[key])
            } else if (key === 'Token') {
                compat['sessionToken'] = data[key];
            } else {
                compat[this.lowerFirstLetter(key)] = data[key];
            }
        }

        return compat;
    },
    lowerFirstLetter: function (source) {
        return source.charAt(0).toLowerCase() + source.slice(1);
    }
};

// 拼接获取临时密钥的参数
var _getCredential = function (options, callback) {

    if (options.durationInSeconds !== undefined) {
        console.warn('warning: durationInSeconds has been deprecated, Please use durationSeconds ).');
    }

    var secretId = options.secretId;
    var secretKey = options.secretKey;
    var proxy = options.proxy || '';
    var host = options.host || '';
    var region = options.region || 'ap-beijing';
    var durationSeconds = options.durationSeconds || options.durationInSeconds || 1800;
    var policy = options.policy;
    var endpoint = options.host || options.endpoint || 'sts.tencentcloudapi.com';

    var policyStr = JSON.stringify(policy);
    var action = options.action || 'GetFederationToken'; // 默认GetFederationToken
    var nonce = util.getRandom(10000, 20000);
    var timestamp = parseInt(+new Date() / 1000);
    var method = 'POST';
    var name = 'cos-sts-nodejs'; // 临时会话名称

    var params = {
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

    var opt = {
        method: method,
        url: StsUrl.replace('{host}', endpoint),
        strictSSL: false,
        json: true,
        form: params,
        headers: {
            Host: endpoint
        },
        proxy: proxy,
    };
    request(opt, function (err, response, body) {
        var data = body && body.Response;
        if (data) {
            if (data.Error) {
                callback(data.Error);
            } else {
                try {
                    data.startTime = data.ExpiredTime - durationSeconds;
                    data = util.backwardCompat(data);
                    callback(null, data)
                } catch (e) {
                    callback(new Error(`Parse Response Error: ${JSON.stringify(data)}`))
                }
            }
        } else {
            callback(err || body);
        }
    });
};

// 获取联合身份临时访问凭证 GetFederationToken
var getCredential = (opt, callback) => {
  Object.assign(opt, { action: 'GetFederationToken' });
    if (callback) return _getCredential(opt, callback);
    return new Promise((resolve, reject) => {
        _getCredential(opt, (err, data) => {
            err ? reject(err) : resolve(data);
        });
    });
};

// 申请扮演角色 AssumeRole
var getRoleCredential = (opt, callback) => {
  Object.assign(opt, { action: 'AssumeRole' });
  if (callback) return _getCredential(opt, callback);
  return new Promise((resolve, reject) => {
      _getCredential(opt, (err, data) => {
          err ? reject(err) : resolve(data);
      });
  });
};

var getPolicy = function (scope) {
    // 定义绑定临时密钥的权限策略
    var statement = scope.map(function (item) {
        var action = item.action || '';
        var bucket = item.bucket || '';
        var region = item.region || '';
        var shortBucketName = bucket.substr(0 , bucket.lastIndexOf('-'));
        var appId = bucket.substr(1 + bucket.lastIndexOf('-'));
        var prefix = item.prefix;
        var resource = 'qcs::cos:' + region + ':uid/' + appId + ':prefix//' + appId + '/' + shortBucketName + '/' + prefix;
        if (action === 'name/cos:GetService') {
            resource = '*';
        }
        return {
            'action': action,
            'effect': 'allow',
            'principal': {'qcs': '*'},
            'resource': resource,
        };
    });
    return {'version': '2.0', 'statement': statement};
};

var  cosStsSdk = {
    getCredential: getCredential,
    getRoleCredential: getRoleCredential,
    getPolicy: getPolicy,
};

module.exports = cosStsSdk;
