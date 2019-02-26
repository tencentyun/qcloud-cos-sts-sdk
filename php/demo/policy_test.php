<?php
include '../sts/sts.php';
$scope = new Scope("name/cos:PutObject", "test-12500000", "ap-guangzhou", "/dir/*");
echo $scope->get_action() . '|' . $scope->get_resource() . '<br>';
$scopes = array();
array_push($scopes, $scope);
array_push($scopes, new Scope("name/cos:GetObject", "test-12500000", "ap-guangzhou", "/1.txt"));
$sts = new STS();
$policy= $sts->getPolicy($scopes);
$policyStr = str_replace('\\/', '/', json_encode($policy));
echo $policyStr;
?>