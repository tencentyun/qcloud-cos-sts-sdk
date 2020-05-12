<?php
require 'vendor/autoload.php';

use QCloud\COSSTS\STS;
use QCloud\COSSTS\Scope;

$sts = new STS();
$scopes = array();
array_push($scopes, new Scope("name/cos:PutObject", "test-1253653367", "ap-guangzhou", "/exampleobject"));
array_push($scopes, new Scope("name/cos:GetObject", "test-1253653367", "ap-guangzhou", "/1.txt"));
$config = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
    'proxy' => '',
    'region' => 'ap-guangzhou', // 换成 bucket 所在园区
    'secretId' => getenv('GROUP_SECRET_ID'), // 固定密钥
    'secretKey' => getenv('GROUP_SECRET_KEY'), // 固定密钥
    'durationSeconds' => 1800, // 密钥有效期
    'policy' => $sts->getPolicy($scopes)
);
// 获取临时密钥，计算签名
$tempKeys = $sts->getTempKeys($config);
echo json_encode($tempKeys)
?>
