<?php
require_once __DIR__ . '/vendor/autoload.php';

use QCloud\COSSTS\Sts;
use QCloud\COSSTS\Scope;

$scope = new Scope("name/cos:PutObject", "test-12500000", "ap-guangzhou", "/exampleobject");
echo $scope->get_action() . '|' . $scope->get_resource() . '<br>';
$scopes = array();
array_push($scopes, $scope);
array_push($scopes, new Scope("name/cos:GetObject", "test-12500000", "ap-guangzhou", "/1.txt"));
$sts = new Sts();
$policy= $sts->getPolicy($scopes);
$policyStr = str_replace('\\/', '/', json_encode($policy));
echo $policyStr;
?>
