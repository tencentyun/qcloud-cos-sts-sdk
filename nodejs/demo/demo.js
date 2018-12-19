var STS = require('../sdk/sts');

// 配置参数
var config = {
    secretId: 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    secretKey: 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    proxy: '',
    durationSeconds: 1800,
    // 放行判断相关参数
    bucket: 'test-1250000000',
    region: 'ap-guangzhou',
};


// getCredential
// 简单上传和分片，需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/14048
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
                'qcs::cos:' + config.region + 'uid/' + appId + ':prefix//' + appId + '/' + shortBucketName + '/dir/*',
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