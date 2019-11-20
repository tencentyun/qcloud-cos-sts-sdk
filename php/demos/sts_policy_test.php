<?php

require_once __DIR__ . '/../vendor/autoload.php';

$sts = new \Qcloud\STS\STS();
$scopes = array();
array_push(
    $scopes,
    new \Qcloud\STS\Scope(
        "name/cos:PutObject",
        "test-125000000",
        "ap-guangzhou",
        "/exampleobject"
    )
);
array_push(
    $scopes,
    new \Qcloud\STS\Scope(
        "name/cos:GetObject",
        "test-125000000",
        "ap-guangzhou",
        "/1.txt"
    )
);
$config = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
    'proxy' => 'XX:XXX',
    'secretId' => 'AKIXXX', // 固定密钥
    'secretKey' => 'EH8XX', // 固定密钥
    'durationSeconds' => 1800, // 密钥有效期
    'policy' => $sts->getPolicy($scopes)
);
// 获取临时密钥，计算签名
try {
    $tempKeys = $sts->getTempKeys($config);
} catch (\Exception $e) {
    var_dump($e->getCode());
    var_dump($e->getMessage());
    exit;
}

echo json_encode($tempKeys);
