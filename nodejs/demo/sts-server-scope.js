var bodyParser = require('body-parser');
var STS = require('../index');
var express = require('express');

// 配置参数
var config = {
    secretId: 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    secretKey: 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    proxy: '',
    durationSeconds: 1800, // 密钥有效期

    // 放行判断相关参数
    bucket: 'test-1250000000', // 换成你的 bucket
    region: 'ap-guangzhou', // 换成 bucket 所在地区
    allowPrefix: 'exampleobject', // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    allowActions: [
        // 简单上传
        'name/cos:PutObject',
        'name/cos:PostObject',
        // 分片上传
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    ],
};

// 判断是否允许获取密钥
var allowScope = function (scope) {
    // TODO 这里自行定制判断放行逻辑
    var allow = (scope || []).every(function (item) {
        if (item.action === 'name/cos:GetService') {
            return config.allowActions.includes(item.action);
        } else {
            return config.allowActions.includes(item.action) &&
                item.bucket === config.bucket &&
                item.region === config.region &&
                (item.prefix || '').startsWith(config.allowPrefix);
        }
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
    if (!scope || !scope.length || !allowScope(scope)) return res.send({code: 10000, codeDesc: 'ScopeNotAllowed', message: "you are not allowed to perform the operation"});

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
        res.send(result);
    });
});
app.all('*', function (req, res, next) {
    res.writeHead(404);
    res.send({code: 404, codeDesc: 'PageNotFound', message: '404 page not found'});
});

// 启动签名服务
app.listen(3000);
console.log('app is listening at http://127.0.0.1:3000');
