package com.tencent.cloud;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;

public class getPolicyTest {

    @Test
    public void testScopesPolicy() {
        Scope scope = new Scope("name/cos:PutObject", "test-1250000000", "ap-guangzhou", "/test.txt");
        List<Scope> scopes = new LinkedList<Scope>();
        scopes.add(scope);

        System.out.println(CosStsClient.getPolicy(scopes));
    }

    @Test
    public void testScopesPolicy2() {
        List<Scope> scopes = new LinkedList<Scope>();

        Scope scope = new Scope("name/cos:PutObject", "test-1250000000", "ap-guangzhou", "/test.txt");
        scopes.add(scope);
        scope = new Scope("name/cos:GetObject", "test-1250000000", "ap-guangzhou", "/test.txt");
        scopes.add(scope);
        scope = new Scope("name/cos:HeadObject", "test-1250000000", "ap-guangzhou", "/test.txt");
        scopes.add(scope);

        System.out.println(CosStsClient.getPolicy(scopes));
    }

    @Test
    public void testParametersPolicy() {
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("bucket", "123123123-1253653367");
        config.put("region", "ap-beijing");

        config.put("allowPrefixes", new String[] {
                "exampleobject",
                "exampleobject2"
        });

        String[] allowActions = new String[] {
                // 简单上传
                "name/cos:PutObject",
                "name/cos:PostObject",
                // 分片上传
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload"
        };
        config.put("allowActions", allowActions);

        CosStsClient.Parameters parameters = new CosStsClient.Parameters();
        parameters.parse(config);

        System.out.println(CosStsClient.getPolicy(parameters));
    }
}
