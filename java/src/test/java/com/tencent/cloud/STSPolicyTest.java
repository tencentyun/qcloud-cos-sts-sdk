package com.tencent.cloud;

import org.junit.Test;

public class STSPolicyTest {
	
	@Test
	public void testSTSPolicy() {
		STSPolicy stsPolicy = new STSPolicy();
		Scope scope = new Scope("name/cos:PutObject", "test-1250000000", "ap-guangzhou", "/test.txt");
		stsPolicy.addScope(scope);
		System.out.println(stsPolicy.toString());
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
		System.out.println(stsPolicy.toString());
	}

}
