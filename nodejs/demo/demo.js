var STS = require('../sdk/sts');

// 配置参数
var config = {
    secretId: 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',   // 固定密钥
    secretKey: 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',  // 固定密钥
    proxy: '',
    durationSeconds: 1800,  // 密钥有效期
    // 放行判断相关参数
    bucket: 'test-1250000000', // 换成你的 bucket
    region: 'ap-guangzhou', // 换成 bucket 所在地区
    allowPrefix: '*' // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 a/* 或者 a.jpg
};


// getCredential
// 简单上传和分片，需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
(function () {
    var shortBucketName = config.bucket.substr(0 , config.bucket.lastIndexOf('-'));
    var appId = config.bucket.substr(1 + config.bucket.lastIndexOf('-'));
    var policy = {
        'version': '2.0',
        'statement': [{
            'action': [
                // 简单上传
                'name/cos:PutObject',
                // 分片上传
                'name/cos:InitiateMultipartUpload',
                'name/cos:ListMultipartUploads',
                'name/cos:ListParts',
                'name/cos:UploadPart',
                'name/cos:CompleteMultipartUpload',
            ],
            'effect': 'allow',
            'principal': {'qcs': ['*']},
            'resource': [
                'qcs::cos:' + config.region + ':uid/' + appId + ':prefix//' + appId + '/' + shortBucketName + '/' + config.allowPrefix,
            ],
        }],
    };
    STS.getCredential({
        secretId: config.secretId,
        secretKey: config.secretKey,
        proxy: config.proxy,
        durationSeconds: config.durationSeconds,
        policy: policy,
    }, function (err, credential) {
        console.log('getCredential:');
        console.log(JSON.stringify(policy, null, '    '));
        console.log(err || credential);
    });
})();


// getPolicy
// 获取临时密钥
(function () {
    var scope = [{
        action: 'name/cos:PutObject',
        bucket: config.bucket,
        region: config.region,
        prefix: '1.txt',
    }];
    var policy = STS.getPolicy(scope);
    STS.getCredential({
        secretId: config.secretId,
        secretKey: config.secretKey,
        proxy: config.proxy,
        policy: policy,
        durationSeconds: config.durationSeconds,
    }, function (err, credential) {
        console.log('getPolicy,getCredential:');
        console.log(JSON.stringify(policy, null, '    '));
        console.log(err || credential);
    });
})();