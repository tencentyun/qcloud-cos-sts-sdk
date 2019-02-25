<?php
// 临时密钥计算样例
// 配置参数
$config = array(
    'url' => 'https://sts.tencentcloudapi.com/',
    'domain' => 'sts.tencentcloudapi.com',
    'proxy' => '',
    'secretId' => 'AKIDxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    'secretKey' => 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', // 固定密钥
    'bucket' => 'test-1250000000', // 换成你的 bucket
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
function _hex2bin($data) {
    $len = strlen($data);
    return pack("H" . $len, $data);
}
// obj 转 query string
function json2str($obj, $notEncode = false) {
    ksort($obj);
    $arr = array();
    foreach ($obj as $key => $val) {
        array_push($arr, $key . '=' . ($notEncode ? $val : rawurlencode($val)));
    }
    return join('&', $arr);
}
// 计算临时密钥用的签名
function getSignature($opt, $key, $method) {
    global $config;
    $formatString = $method . $config['domain'] . '/?' . json2str($opt, 1);
    $sign = hash_hmac('sha1', $formatString, $key);
    $sign = base64_encode(_hex2bin($sign));
    return $sign;
}
// v2接口的key首字母小写，v3改成大写，此处做了向下兼容
function backwardCompat($result) {
    $compat = array();
    foreach ($result as $key => $value) {
        if(is_array($value)) {
            $compat[lcfirst($key)] = backwardCompat($value);
        } elseif ($key == 'Token') {
            $compat['sessionToken'] = $value;
        } else {
            $compat[lcfirst($key)] = $value;
        }
    }
    return $compat;
}
class Scope{
	var $action;
	var $bucket;
	var $region;
	var $resourcePrefix;
	function __construct($action, $bucket, $region, $resourcePrefix){
		$this->action = $action;
		$this->bucket = $bucket;
		$this->region = $region;
		$this->resourcePrefix = $resourcePrefix;
	}
	function get_action(){
		return $this->action;
	}
	
	function get_resource(){
		$index = strripos($this->bucket, '-');
		$bucketName = substr($this->bucket, 0, $index);
		$appid = substr($this->bucket, $index + 1);
		if(!(strpos($this->resourcePrefix, '/') === 0)){
			$this->resourcePrefix = '/' . $this->resourcePrefix;
		}
		return 'qcs::cos:' . $this->region . ':uid/' . $appid . ':prefix//' . $appid . '/' . $bucketName . $this->resourcePrefix;
	}
}
function getPolicy($scopes){
	if (!is_array($scopes)){
		return null;
	}
	$statements = array();
	
	for($i=0, $counts=count($scopes); $i < $counts; $i++){
		$actions=array();
		$resources = array();
		array_push($actions, $scopes[$i]->get_action());
		array_push($resources, $scopes[$i]->get_resource());
		$principal = array(
		'qcs' => array('*')
		);
		$statement = array(
		'actions' => $actions,
		'effect' => 'allow',
		'principal' => $principal,
		'resource' => $resources
		);
		array_push($statements, $statement);
	}
		
	$policy = array(
		'version' => '2.0',
		'statement' => $statements
	);
	return $policy;
}
// 获取临时密钥
function getTempKeys() {
    global $config;
    if(array_key_exists('bucket', $config)){
		$ShortBucketName = substr($config['bucket'],0, strripos($config['bucket'], '-'));
		$AppId = substr($config['bucket'], 1 + strripos($config['bucket'], '-'));
	}
	if(array_key_exists('policy', $config)){
		$policy = $config['policy'];
	}else{
	    $policy = array(
			'version'=> '2.0',
			'statement'=> array(
				array(
					'action'=> $config['allowActions'],
					'effect'=> 'allow',
					'principal'=> array('qcs'=> array('*')),
					'resource'=> array(
						'qcs::cos:' . $config['region'] . ':uid/' . $AppId . ':prefix//' . $AppId . '/' . $ShortBucketName . '/' . $config['allowPrefix']
					)
				)
			)
		);	
	}
    $policyStr = str_replace('\\/', '/', json_encode($policy));
    $Action = 'GetFederationToken';
    $Nonce = rand(10000, 20000);
    $Timestamp = time();
    $Method = 'POST';
    $params = array(
        'SecretId'=> $config['secretId'],
        'Timestamp'=> $Timestamp,
        'Nonce'=> $Nonce,
        'Action'=> $Action,
        'DurationSeconds'=> $config['durationSeconds'],
        'Version'=>'2018-08-13',
        'Name'=> 'cos',
        'Region'=> 'ap-guangzhou',
        'Policy'=> urlencode($policyStr)
    );
    $params['Signature'] = getSignature($params, $config['secretKey'], $Method);
    $url = $config['url'];
    $ch = curl_init($url);
    $config['proxy'] && curl_setopt($ch, CURLOPT_PROXY, $config['proxy']);
    curl_setopt($ch, CURLOPT_HEADER, 0);
    curl_setopt($ch,CURLOPT_SSL_VERIFYPEER,0);
    curl_setopt($ch,CURLOPT_SSL_VERIFYHOST,0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json2str($params));
    $result = curl_exec($ch);
    if(curl_errno($ch)) $result = curl_error($ch);
    curl_close($ch);
    $result = json_decode($result, 1);
    if (isset($result['Response'])) {
        $result = $result['Response'];
        $result['startTime'] = $result['ExpiredTime'] - $config['durationSeconds'];
    }
    $result = backwardCompat($result);
    return $result;
}
?>