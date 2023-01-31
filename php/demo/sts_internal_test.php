<?php
require dirname(__FILE__) . '/../vendor/autoload.php';

use QCloud\COSSTS\Sts;

$sts = new Sts();

//---------------------获取临时密钥---------------------//
$config = array(
    'endpoint' => "internal.tencentcloudapi.com", //接入点，内网填写"internal.tencentcloudapi.com"，外网填写"tencentcloudapi.com"
    'proxy' => '',
    'secretId' => getenv('GROUP_SECRET_ID'), // 固定密钥,若为明文密钥，请直接以'xxx'形式填入，不要填写到getenv()函数中
    'secretKey' => getenv('GROUP_SECRET_KEY'), // 固定密钥,若为明文密钥，请直接以'xxx'形式填入，不要填写到getenv()函数中
    'bucket' => 'test-1253653367', // 换成你的 bucket
    'region' => 'ap-guangzhou', // 换成 bucket 所在园区
    'durationSeconds' => 1800, // 密钥有效期
    'allowPrefix' => array('exampleobject.jpg','exampleobject.png','exampleobject/*'), // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allowActions' => array (
        // 简单上传
        'name/cos:PutObject',
        'name/cos:PostObject',
        // 分片上传
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    ),
    // 临时密钥生效条件，关于condition的详细设置规则和COS支持的condition类型可以参考 https://cloud.tencent.com/document/product/436/71306
    "condition" => array(
        "ip_equal" => array(
            "qcs:ip" => array(
                "10.217.182.3/24",
                "111.21.33.72/24",
            )
        )
    )
);

// 获取临时密钥，计算签名
$tempKeys = $sts->getTempKeys($config);
echo json_encode($tempKeys);



//---------------------申请角色---------------------//
$roleConfig = array(
    'roleArn' => 'qcs::cam::uin/100000000000:roleName/test', //角色的资源描述，可在 [访问管理](https://console.cloud.tencent.com/cam/role) 点击角色名获取
    'endpoint' => 'internal.tencentcloudapi.com', // 接入点，内网填写"internal.tencentcloudapi.com"，外网填写"tencentcloudapi.com"
    'secretId' => getenv('GROUP_SECRET_ID'), // 固定密钥,若为明文密钥，请直接以'xxx'形式填入，不要填写到getenv()函数中
    'secretKey' => getenv('GROUP_SECRET_KEY'), // 固定密钥,若为明文密钥，请直接以'xxx'形式填入，不要填写到getenv()函数中
    'bucket' => 'test-1253653367', // 换成你的 bucket
    'region' => 'ap-guangzhou', // 换成 bucket 所在园区
    'durationSeconds' => 1800, // 密钥有效期
    'allowPrefix' => 'exampleobject', // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
    // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
    'allowActions' => array (
        // 简单上传
        'name/cos:PutObject',
        'name/cos:PostObject',
        // 分片上传
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    )
);

// 申请扮演角色
$tempRoleKeys = $sts->getRoleCredential($roleConfig);
echo json_encode($tempRoleKeys);