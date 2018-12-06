var bodyParser = require('body-parser');
var STS = require('../index');
var express = require('express');

// 配置参数
var config = {
    secretId: 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    secretKey: 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
    proxy: '',
    durationSeconds: 1800,
    bucket: 'test-1250000000',
    region: 'ap-guangzhou',
    allowPrefix: '',
    // 简单上传和分片，需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/14048
    allowActions: [
        'PutObject',
        'InitiateMultipartUpload',
        'ListMultipartUploads',
        'ListParts',
        'UploadPart',
        'CompleteMultipartUpload'
    ],
};

// 判断是否允许获取密钥
var allowScope = function (scope) {
    var allow = (scope || []).every(function (item) {
        return config.allowActions.includes(item.action) &&
            item.bucket === config.bucket &&
            item.region === config.region &&
            (item.prefix || '').startsWith(config.allowPrefix);
    });
    return allow;
};


// 创建临时密钥服务
var app = express();
app.use(bodyParser.json());

// 支持跨域访问
app.all('*', function (req, res, next) {
    res.header('Content-Type', 'application/json');
    res.header('Access-Control-Allow-Origin', 'http://127.0.0.1:88');
    res.header('Access-Control-Allow-Headers', 'origin,accept,content-type');
    if (req.method.toUpperCase() === 'OPTIONS') {
        res.end();
    } else {
        next();
    }
});

// 临时密钥接口
app.all('/sts', function (req, res, next) {

    // TODO 这里根据自己业务需要做好放行判断
    var scope = req.body;
    if (!scope || !scope.length || !allowScope(scope)) return res.send({code: -1, message: 'deny'});

    // 获取临时密钥
    var policy = STS.getPolicy(scope);
    STS.getCredential({
        secretId: config.secretId,
        secretKey: config.secretKey,
        proxy: config.proxy,
        durationSeconds: config.durationSeconds,
        policy: policy,
    }, function (err, tempKeys) {
        var result = JSON.stringify(err || tempKeys) || '';
        result.code = 0;
        res.send(result);
    });
});
app.all('*', function (req, res, next) {
    res.writeHead(404);
    res.send({code: -1, message: '404 Not Found'});
});

// 启动签名服务
app.listen(3000);
console.log('app is listening at http://127.0.0.1:3000');
