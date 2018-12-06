var request= require('request');
var crypto= require('crypto');

var StsDomain = 'sts.api.qcloud.com';
var StsUrl = 'https://sts.api.qcloud.com/v2/index.php';

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
    getSignature: function (opt, key, method) {
        var formatString = method + StsDomain + '/v2/index.php?' + util.json2str(opt);
        var hmac = crypto.createHmac('sha1', key);
        var sign = hmac.update(Buffer.from(formatString, 'utf8')).digest('base64');
        return sign;
    },
};

// 拼接获取临时密钥的参数
var getCredential = function (options, callback) {

    if (options.durationInSeconds !== undefined) {
        console.warn('warning: durationInSeconds has been deprecated, Please use durationSeconds ).');
    }

    var secretId = options.secretId;
    var secretKey = options.secretKey;
    var proxy = options.proxy || '';
    var durationSeconds = options.durationSeconds || options.durationInSeconds || 1800;
    var policy = options.policy;

    var policyStr = JSON.stringify(policy);
    var action = 'GetFederationToken';
    var nonce = util.getRandom(10000, 20000);
    var timestamp = parseInt(+new Date() / 1000);
    var method = 'POST';

    var params = {
        Region: '',
        SecretId: secretId,
        Timestamp: timestamp,
        Nonce: nonce,
        Action: action,
        durationSeconds: durationSeconds,
        name: 'cos',
        policy: encodeURIComponent(policyStr),
    };
    params.Signature = util.getSignature(params, secretKey, method);

    var opt = {
        method: method,
        url: StsUrl,
        strictSSL: false,
        json: true,
        form: params,
        headers: {
            Host: StsDomain
        },
        proxy: proxy,
    };
    request(opt, function (err, response, body) {
        var data = body && body.data;
        if (data) {
            callback(null, data);
        } else {
            callback(body);
        }
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
            'action': [action],
            'effect': 'allow',
            'principal': {'qcs': ['*']},
            'resource': resource,
        };
    });
    return {'version': '2.0', 'statement': statement};
};

var  cosStsSdk = {
    getCredential: getCredential,
    getPolicy: getPolicy,
};

module.exports = cosStsSdk;