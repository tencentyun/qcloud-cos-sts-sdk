// 临时密钥服务例子
const STS = require('qcloud-cos-sts');
const express = require('express');
const pathLib = require('path');

// 配置参数
const config = {
  secretId: process.env.SecretId,
  secretKey: process.env.SecretKey,
  proxy: process.env.Proxy,
  durationSeconds: 1800,
  bucket: process.env.Bucket,
  region: process.env.Region,
  // 允许操作（上传）的对象前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子： user1/* 或者 * 或者a.jpg
  // 请注意当使用 * 时，可能存在安全风险，详情请参阅：https://cloud.tencent.com/document/product/436/40265
  allowPrefix: '_ALLOW_DIR_/*',
  // 密钥的上传操作权限列表
  allowActions: [
    // 简单上传
    'name/cos:PutObject',
    // 分块上传
    'name/cos:InitiateMultipartUpload',
    'name/cos:ListMultipartUploads',
    'name/cos:ListParts',
    'name/cos:UploadPart',
    'name/cos:CompleteMultipartUpload',
  ],
};

// 生成要上传的 COS 文件路径文件名
const generateCosKey = function (ext) {
  const date = new Date();
  const m = date.getMonth() + 1;
  const ymd = `${date.getFullYear()}${m < 10 ? `0${m}` : m}${date.getDate()}`;
  const r = ('000000' + Math.random() * 1000000).slice(-6);
  const cosKey = `file/${ymd}/${ymd}_${r}${ext ? `${ext}` : ''}`;
  return cosKey;
};

// 创建临时密钥服务
const app = express();

app.use(function (req, res, next) {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  next();
});

// 获取临时密钥
function getSts({ cosKey, condition }) {
  return new Promise((resolve, reject) => {
    // 获取临时密钥
    const AppId = config.bucket.substr(config.bucket.lastIndexOf('-') + 1);
    // 数据万象DescribeMediaBuckets接口需要resource为*,参考 https://cloud.tencent.com/document/product/460/41741
    let resource =
      'qcs::cos:' +
      config.region +
      ':uid/' +
      AppId +
      ':' +
      config.bucket +
      '/' +
      config.allowPrefix;
    if (cosKey) {
      resource += cosKey;
    }
    console.log('检查resource是否正确', resource);
    const policy = {
      version: '2.0',
      statement: [
        {
          action: config.allowActions,
          effect: 'allow',
          resource: [
            // cos相关授权路径
            resource,
            // ci相关授权路径 按需使用
            // 'qcs::ci:' + config.region + ':uid/' + AppId + ':bucket/' + config.bucket + '/' + 'job/*',
          ],
          condition
        },
      ],
    };
    const startTime = Math.round(Date.now() / 1000);
    STS.getCredential(
      {
        secretId: config.secretId,
        secretKey: config.secretKey,
        proxy: config.proxy,
        region: config.region,
        durationSeconds: config.durationSeconds,
        // endpoint: 'sts.internal.tencentcloudapi.com', // 支持设置sts内网域名
        policy: policy,
      },
      function (err, tempKeys) {
        if (tempKeys) tempKeys.startTime = startTime;
        if (err) {
          reject(err);
        } else {
          resolve(tempKeys);
        }
      }
    );
  });
}

// 返回临时密钥和上传信息，客户端自行计算签名
app.get('/getKeyAndCredentials', function (req, res, next) {
  // TODO 这里根据自己业务需要做好放行判断
  if (config.allowPrefix === '_ALLOW_DIR_/*') {
    res.send({ error: '请修改 allowPrefix 配置项，指定允许上传的路径前缀' });
    return;
  }

  // 业务自行实现 用户登录态校验，比如对 token 校验
  // const userToken = req.query.userToken;
  // const canUpload = checkUserRole(userToken);
  // if (!canUpload) {
  //   res.send({ error: '当前用户没有上传权限' });
  //   return;
  // }

  // 上传文件可控制类型、大小，按需开启
  const permission = {
    limitExt: false, // 限制上传文件后缀
    extWhiteList: ['jpg', 'jpeg', 'png', 'gif', 'bmp'], // 限制的上传后缀
    limitContentType: false, // 限制上传 contentType
    limitContentLength: false, // 限制上传文件大小
  };

  // 客户端传进原始文件名，这里根据文件后缀生成随机 Key
  const filename = req.query.filename;
  const ext = pathLib.extname(filename);
  const cosKey = generateCosKey(ext);
  const condition = {};

  // 1. 限制上传文件后缀
  if (permission.limitExt) {
    const extInvalid = !ext || !extWhiteList.includes(ext);
    if (extInvalid) {
      res.send({ error: '非法文件，禁止上传' });
    }
  }

  // 2. 限制上传文件 content-type
  if (permission.limitContentType) {
    Object.assign(condition, {
      'string_like': {
        // 只允许上传 content-type 为图片类型
        'cos:content-type': 'image/*'
      }
    });
  }

  // 3. 限制上传文件大小
  if (permission.limitContentLength) {
    Object.assign(condition, {
      'numeric_less_than_equal': {
        // 上传大小限制不能超过 5MB
        'cos:content-length': 5 * 1024 * 1024
      },
    });
  }

  getSts({ cosKey, condition })
    .then((data) => {
      res.send({
        TmpSecretId: data.credentials.tmpSecretId,
        TmpSecretKey: data.credentials.tmpSecretKey,
        SessionToken: data.credentials.sessionToken,
        StartTime: Math.round(Date.now() / 1000),
        ExpiredTime: data.expiredTime,
        Bucket: config.bucket,
        Region: config.region,
        Key: cosKey,
      });
    })
    .catch((err) => {
      console.log('sts error', err);
      res.send(err);
    });
});

app.all('*', function (req, res, next) {
  res.send({ code: -1, message: '404 Not Found' });
});

// 启动签名服务
app.listen(3000);
console.log('app is listening at http://127.0.0.1:3000');
