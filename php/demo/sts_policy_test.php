<?php
include '../sts/sts.php';
$scopes = array();
array_push($scopes, new Scope("name/cos:PutObject", "test-12500000", "ap-guangzhou", "/dir/*"));
array_push($scopes, new Scope("name/cos:GetObject", "test-12500000", "ap-guangzhou", "/1.txt"));
$temp = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
    'proxy' => 'https://web-proxy.tencent.com:8080', //网络请求代理
    'secretId' => 'AKIDXXX', // 固定密钥
    'secretKey' => 'EH8XXX', // 固定密钥
    'durationSeconds' => 1800, // 密钥有效期
    'policy' => getPolicy($scopes)
);
$config = $temp;
// 获取临时密钥，计算签名
$tempKeys = getTempKeys();
echo json_encode($tempKeys)
?>