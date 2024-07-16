<?php
require_once __DIR__ . '/vendor/autoload.php';

use QCloud\COSSTS\Sts;

// 生成要上传的 COS 文件路径文件名
function generateCosKey($ext) {
  $ymd = date('Ymd');
  $r = substr('000000' . rand(), -6);
  $cosKey = 'file/' . $ymd. '/' . $ymd . '_' . $r;
  if ($ext) {
    $cosKey = $cosKey . '.' . $ext;
  }
  return $cosKey;
};

// 获取单一文件上传权限的临时密钥
function getKeyAndCredentials($filename) {
  // 业务自行实现 用户登录态校验，比如对 token 校验
  // $canUpload = checkUserRole($userToken);
  // if (!$canUpload) {
  //   return '当前用户没有上传权限';
  // }

  // 上传文件可控制类型、大小，按需开启
  $permission = array(
    'limitExt' => false, // 限制上传文件后缀
    'extWhiteList' => ['jpg', 'jpeg', 'png', 'gif', 'bmp'], // 限制的上传后缀
    'limitContentType' => false, // 限制上传 contentType
    'limitContentLength' => false, // 限制上传文件大小
  );
  $condition = array();

  // 客户端传进原始文件名，这里根据文件后缀生成随机 Key
  $ext = pathinfo($filename, PATHINFO_EXTENSION);

  // 1. 限制上传文件后缀
  if ($permission['limitExt']) {
    if ($ext === '' || array_key_exists($ext, $permission['extWhiteList'])) {
      return '非法文件，禁止上传';
    }
  }

  // 2. 限制上传文件 content-type
  if ($permission['limitContentType']) {
    // 只允许上传 content-type 为图片类型
    $condition['string_like'] = array('cos:content-type' => 'image/*');
  }

  // 3. 限制上传文件大小
  if ($permission['limitContentLength']) {
    // 上传大小限制不能超过 5MB
    $condition['numeric_less_than_equal'] = array('cos:content-length' => 5 * 1024 * 1024);
  }

  $cosKey = generateCosKey($ext);
  $bucket = 'test-131234567'; // 换成你的 bucket
  $region = 'ap-guangzhou'; // 换成 bucket 所在园区
  
  $config = array(
    'url' => 'https://sts.tencentcloudapi.com/', // url和domain保持一致
    'domain' => 'sts.tencentcloudapi.com', // 域名，非必须，默认为 sts.tencentcloudapi.com
    'proxy' => '',
    'secretId' => getenv('GROUP_SECRET_ID'), // 固定密钥,若为明文密钥，请直接以'xxx'形式填入，不要填写到getenv()函数中
    'secretKey' => getenv('GROUP_SECRET_KEY'), // 固定密钥,若为明文密钥，请直接以'xxx'形式填入，不要填写到getenv()函数中
    'bucket' => $bucket, // 换成你的 bucket
    'region' => $region, // 换成 bucket 所在园区
    'durationSeconds' => 1800, // 密钥有效期
    'allowPrefix' => array($cosKey), // 只分配当前 key 的路径权限
    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allowActions' => array (
        // 简单上传 
        'name/cos:PutObject',
        // 分片上传
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    ),
  );

  if (!empty($condition)) {
    $config['condition'] = $condition;
  }

  $sts = new Sts();
  $tempKeys = $sts->getTempKeys($config);
  $resTemp = array(
    'TmpSecretId' => $tempKeys['credentials']['tmpSecretId'],
    'TmpSecretKey' => $tempKeys['credentials']['tmpSecretKey'],
    'SessionToken' => $tempKeys['credentials']['sessionToken'],
    'StartTime' => time(),
    'ExpiredTime' => $tempKeys['expiredTime'],
    'Bucket' => $bucket,
    'Region' => $region,
    'Key' => $cosKey,
  );
  echo json_encode($resTemp);
  return $resTemp;
}