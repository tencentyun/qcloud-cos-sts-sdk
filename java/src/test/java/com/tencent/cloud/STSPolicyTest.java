package com.tencent.cloud;

import org.json.JSONObject;
import org.junit.Test;

public class STSPolicyTest {
	
	@Test
	public void testSTSPolicy() {
		STSPolicy stsPolicy = new STSPolicy();
		Scope scope = new Scope("name/cos:PutObject", "test-1250000000", "ap-guangzhou", "/test.txt");
		stsPolicy.addScope(scope);
		JSONObject jsonObject = new JSONObject(stsPolicy.toString());
		System.out.println(jsonObject.toString(4));
	}
	
	@Test
	public void testSTSPolicy2() {
		STSPolicy stsPolicy = new STSPolicy();
		Scope scope = new Scope("name/cos:PutObject", "test-1250000000", "ap-guangzhou", "/test.txt");
		stsPolicy.addScope(scope);
		scope = new Scope("name/cos:GetObject", "test-1250000000", "ap-guangzhou", "/test.txt");
		stsPolicy.addScope(scope);
		scope = new Scope("name/cos:HeadObject", "test-1250000000", "ap-guangzhou", "/test.txt");
		stsPolicy.addScope(scope);
		JSONObject jsonObject = new JSONObject(stsPolicy.toString());
		System.out.println(jsonObject.toString(4));
	}

}
