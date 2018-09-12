var crypto = require('crypto');
var request = require('request');

var stsUrl = 'https://sts.api.qcloud.com/v2/index.php';
var stsDomain = 'sts.api.qcloud.com';

var defaultDurationInSeconds = 1800;

var util = {
    // 获取随机数
    getRandom: function (min, max) {
        return Math.round(Math.random() * (max - min) + min);
    },
    // json 转 query string
    json2str: function (obj, notEncode) {
        var arr = [];
        Object.keys(obj).sort().forEach(function (item) {
            var val = obj[item] || '';
            !notEncode && (val = encodeURIComponent(val));
            arr.push(item + '=' + val);
        });
        return arr.join('&');
    },
    // 计算签名
    getSignature: function (opt, key, method) {
        var formatString = method + stsDomain + '/v2/index.php?' + util.json2str(opt, 1);
        formatString = formatString = decodeURIComponent(formatString);
        var hmac = crypto.createHmac('sha1', key);
        var sign = hmac.update(new Buffer(formatString, 'utf8')).digest('base64');
        return sign;
    },
};

// 拼接获取临时密钥的参数
var getSTS = function (options, callback) {
    var policy = (options.policy === undefined) ? {
        'version': '2.0',
        'statement': [{
            'action': [
                'name/cos:*'
            ],
            'effect': 'allow',
            'principal': {'qcs': ['*']},
            'resource': '*'
        }]
    } : options.policy;

    var policyStr = JSON.stringify(policy);
    var Action = 'GetFederationToken';
    var Nonce = util.getRandom(10000, 20000);
    var Timestamp = parseInt(+new Date() / 1000);
    var Method = 'GET';

    var params = {
        Action: Action,
        Nonce: Nonce,
        Region: '',
        name: '',
        SecretId: options.secretId,
        Timestamp: Timestamp,
        durationSeconds: (options.expired === undefined) ? defaultDurationInSeconds : options.durationInSeconds,
        policy: encodeURIComponent(policyStr),
    };
    params.Signature = encodeURIComponent(util.getSignature(params, options.secretKey, Method));

    var opt = {
        method: Method,
        url: stsUrl + '?' + util.json2str(params, 1),
        rejectUnauthorized: false,
        headers: {
            Host: stsDomain
        }
    };
    if (options.proxy) {
        opt.proxy = options.proxy;
    }
    request(opt, function (err, response, body) {
        body = body && JSON.parse(body);
        var message = body.message;
        var error = err || message;
        if (error) {
            console.error(error);
            callback(null);
        } else {
            callback(body);
        }
    });
};

module.exports = {
    getCredential: getSTS
};
