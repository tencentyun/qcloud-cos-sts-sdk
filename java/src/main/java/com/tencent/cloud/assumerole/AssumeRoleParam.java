package com.tencent.cloud.assumerole;

import com.tencent.cloud.Policy;
import com.tencent.cloud.Statement;
import com.tencent.cloud.cos.util.Jackson;
import com.tencent.cloud.cos.util.Util;
import java.util.Properties;

public class AssumeRoleParam {
    private String secretId = "";
    private String secretKey = "";
    private int durationSec = 0;
    private String region = "";
    private String host = "";

    private String roleArn = "";
    private String roleSessionName = "";
    private String signatureMethod = "";

    private String[] resources = new String[]{};
    private String[] actions = new String[]{};
    private String policy = "";

    public void Parse(Properties properties) {
        if (properties == null) {
            throw new NullPointerException("null properties");
        }

        secretId = properties.getProperty("SecretId", "");
        secretKey = properties.getProperty("SecretKey", "");
        region = properties.getProperty("region", "");

        // HmacSHA1 or HmacSHA256
        signatureMethod = properties.getProperty("SignatureMethod", "HmacSHA1");

        String tmpDurationSec = properties.getProperty("durationSeconds", "");
        durationSec =
                tmpDurationSec.isEmpty() ? 0 : Integer.parseInt(tmpDurationSec);
        
        roleArn = properties.getProperty("RoleArn", "");
        roleSessionName = properties.getProperty("RoleSessionName", "");

        host = properties.getProperty("host", Util.DEFAULT_STS_INTERNAL_HOST);

        String tmpPolicy = properties.getProperty("policy", "");
        if (tmpPolicy.isEmpty()) {
            String tmpActions = properties.getProperty("policy.actions", "");
            String tmpResources = properties.getProperty("policy.resources", "");
            if (!tmpActions.isEmpty() || !tmpResources.isEmpty()) {
                actions = tmpActions.split(Util.CONFIG_VALUE_SEPERATOR);
                resources = tmpResources.split(Util.CONFIG_VALUE_SEPERATOR);
                policy = buildPolicy();
            }
        } else {
            policy = tmpPolicy;
        }
    }

    public void check (){
        if (secretId.isEmpty()) {
            throw new IllegalArgumentException("empty secretId");
        }
        if (secretKey.isEmpty()) {
            throw new IllegalArgumentException("empty secretId");
        }
        if (region.isEmpty()) {
            throw new IllegalArgumentException("empty region");
        }
        if (host.isEmpty()) {
            throw new IllegalArgumentException("empty host");
        }
        if (signatureMethod.isEmpty()) {
            throw new IllegalArgumentException("empty signatureMethod");
        }
        if (roleArn.isEmpty()) {
            throw new IllegalArgumentException("empty roleArn");
        }
        if (roleSessionName.isEmpty()) {
            throw new IllegalArgumentException("empty roleSessionName");
        }
    }

    /**
     * 其余元素的构造可以参考：
     * https://cloud.tencent.com/document/product/598/10603
     */
    private String buildPolicy() {
        Statement statement = new Statement();
        statement.setEffect("allow");

        for (String action : actions) {
            statement.addAction(action);
        }

        for (String res : resources) {
            statement.addResource(res);;
        }

        Policy tmpPolicy = new Policy();
        tmpPolicy.setVersion("2.0");
        tmpPolicy.addStatement(statement);

        return Jackson.toJsonPrettyString(tmpPolicy);
    }


    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    public String getRoleSessionName() {
        return roleSessionName;
    }

    public void setRoleSessionName(String roleSessionName) {
        this.roleSessionName = roleSessionName;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public String[] getResources() {
        return resources;
    }

    public void setResources(String[] resources) {
        this.resources = resources;
    }

    public String[] getActions() {
        return actions;
    }

    public void setActions(String[] actions) {
        this.actions = actions;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
