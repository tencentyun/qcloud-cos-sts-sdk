<?php
include '../sts/sts.php';
$temp = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
   'proxy' => 'https://web-proxy.tencent.com:8080', //网络请求代理
    'secretId' => 'AKIDXXX', // 固定密钥
    'secretKey' => 'EH8XXX', // 固定密钥
    'bucket' => 'test-12500000', // 换成你的 bucket
    'region' => 'ap-guangzhou', // 换成 bucket 所在园区
    'durationSeconds' => 1800, // 密钥有效期
    'allowPrefix' => '*', // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 a/* 或者 a.jpg
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
    )
);
$config = $temp;
// 获取临时密钥，计算签名
$tempKeys = getTempKeys();
echo json_encode($tempKeys)
?>