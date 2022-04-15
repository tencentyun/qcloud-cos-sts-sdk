package com.tencent.cloud;

import com.tencent.cloud.assumerole.AssumeRoleParam;
import com.tencent.cloud.cos.util.Sign;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.tencent.cloud.cos.util.Jackson;
import com.tencent.cloud.cos.util.Request;

public class CosStsClient {

    private static final int DEFAULT_DURATION_SECONDS = 1800;
    public static final String STS_DEFAULT_HOST = "sts.tencentcloudapi.com";

    /**
     * STS的GetFederationToken接口
     * https://cloud.tencent.com/document/product/1312/48195
     */
    public static Response getCredential(TreeMap<String, Object> config) throws IOException {
        TreeMap<String, Object> params = new TreeMap<String, Object>();
        Parameters parameters = new Parameters();
        parameters.parse(config);

        if(parameters.secretId == null) throw new IllegalArgumentException("secretId is null");
        if(parameters.secretKey == null) throw new IllegalArgumentException("secretKey is null");
        
        String policy = parameters.policy;
        if (policy != null) {
            params.put("Policy", policy);
        } else {
            params.put("Policy", getPolicy(parameters));
        }
        
        params.put("DurationSeconds", parameters.duration);

        params.put("Name", "cos-sts-java");
        params.put("Action", "GetFederationToken");
        params.put("Version", "2018-08-13");
        String region = RegionCodeFilter.convert(parameters.region);
        params.put("Region", region);
        if(parameters.secretType != null) {
            params.put("SecretType", parameters.secretType);
        }

        // 请求的 Host
        String host = STS_DEFAULT_HOST;
        if (parameters.host != null) {
            host = parameters.host;
        }
        // 签名的 Host
        String stsHost = STS_DEFAULT_HOST;
        if (host.startsWith("sts.") && host.endsWith(".tencentcloudapi.com")) {
            stsHost = host;
        }
        String path = "/";

        String result = null;
        JsonNode jsonResult = null;
        try {
            result = Request.send(params, (String) parameters.secretId,
                    parameters.secretKey,
                    "POST", host, stsHost, path);
            jsonResult = Jackson.jsonNodeOf(result);
            JsonNode data = jsonResult.get("Response");
            if (data == null) {
                data = jsonResult;
            }

            Response response = Jackson.fromJsonString(data.toString(), Response.class);
            long expiredTime = data.get("ExpiredTime").asLong();
            response.startTime = expiredTime - parameters.duration;

            if (response.credentials.token != null) {
                response.credentials.sessionToken = response.credentials.token;
                response.credentials.token = null;
            }

            return response;
        } catch (Exception e) {
            if (jsonResult != null) {
                JsonNode response = jsonResult.get("Response");
                if (response != null) {
                    JsonNode error = response.get("Error");
                    if (error != null) {
                        String message = error.get("Message").asText();
                        String code = error.get("Code").asText();
                        if ("InvalidParameterValue".equals(code) && message != null && message.contains("Region")) {
                            // Region is not recognized
                            if (RegionCodeFilter.block(region)) {
                                return getCredential(config);
                            }
                        }
                    }
                }
            }
            throw new IOException("result = " + result, e);
        }
    }

    /**
     * STS的AssumeRole接口
     * ref:https://cloud.tencent.com/document/product/1312/48197
     * v1签名:https://cloud.tencent.com/document/api/1312/48201#.E4.BD.BF.E7.94.A8.E7.AD.BE.E5.90.8D.E6.96.B9.E6.B3.95-v1-.E7.9A.84.E5.85.AC.E5.85.B1.E5.8F.82.E6.95.B0
     */
    public static Response getRoleCredential(AssumeRoleParam assumeRoleParam) throws Exception {
        if (assumeRoleParam == null) {
            throw new IllegalArgumentException("null assumeRoleParam");
        }
        assumeRoleParam.check();

        TreeMap<String, Object> params = new TreeMap<String, Object>();

        // 公共参数
        params.put("Action", "AssumeRole");
        params.put("Region", assumeRoleParam.getRegion());
        params.put("Timestamp", System.currentTimeMillis() / 1000);
        params.put("Version", "2018-08-13");
        params.put("Nonce", new Random().nextInt(Integer.MAX_VALUE));
        params.put("SignatureMethod", assumeRoleParam.getSignatureMethod());

        // 接口参数
        params.put("SecretId", assumeRoleParam.getSecretId());

        if (assumeRoleParam.getDurationSec() > 0) {
            params.put("DurationSeconds", assumeRoleParam.getDurationSec());
        }

        params.put("RoleArn", assumeRoleParam.getRoleArn());
        params.put("RoleSessionName", assumeRoleParam.getRoleSessionName());


        // 不填policy，默认与有申请账号或角色一样的权限
        if (!assumeRoleParam.getPolicy().isEmpty()) {
            params.put("Policy", assumeRoleParam.getPolicy());
        }


        // 请求的 Host，可能是ip, 而放在签名的 Hostn必须是域名
        String stsHost = STS_DEFAULT_HOST;
        if (assumeRoleParam.getHost().startsWith("sts.") && assumeRoleParam.getHost().endsWith(".tencentcloudapi.com")) {
            stsHost = assumeRoleParam.getHost();
        }
        String path = "/";
        String url = "https://" + assumeRoleParam.getHost() + path;
        String method = "POST";

        String plainText = Sign.makeSignPlainText(params, method, stsHost, path);
        try
        {
            /**
             * 签名参考：https://cloud.tencent.com/document/api/1312/48201#.E4.BD.BF.E7.94.A8.E7.AD.BE.E5.90.8D.E6.96.B9.E6.B3.95-v1-.E7.9A.84.E5.85.AC.E5.85.B1.E5.8F.82.E6.95.B0
             */
            params.put("Signature", Sign.sign(plainText, assumeRoleParam.getSecretKey(), assumeRoleParam.getSignatureMethod()));
        } catch (Exception e ) {
            System.out.println("fail to sign with plainText: " + plainText + ", SignatureMethod" + assumeRoleParam.getSignatureMethod());
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        String result = null;
        try {
            // 尽量复用以前的方法
            result = Request.sendRequest(url, params, method);
            System.out.println("assume role result: " + result);
        } catch (Exception e) {
            System.out.println("fail to exchange request. url=" + url);
            throw new Exception(e.getMessage(), e);
        }

        JsonNode jsonResult = Jackson.jsonNodeOf(result);

        /**
         * {
         *   "Response": {
         *     "Credentials": {
         *       "Token": "da1e9d2ee9dda83506832d5ecb903b790132dfe340001",
         *       "TmpSecretId": "AKID65zyIP0mpXtaI******WIQVMn1umNH58",
         *       "TmpSecretKey": "q95K84wrzuEGoc*******52boxvp71yoh"
         *     },
         *     "ExpiredTime": 1543914376,
         *     "Expiration": "2018-12-04T09:06:16Z",
         *     "RequestId": "4daec797-9cd2-4f09-9e7a-7d4c43b2a74c"
         *   }
         * }
         */
        if (!jsonResult.has("Response")) {
            System.out.println("there is no Response in jsonResult: " + jsonResult.toPrettyString());
            throw new Exception("there is no Response in jsonResult");
        }
        JsonNode jsonResponse = jsonResult.get("Response");

        if (!jsonResponse.has("Credentials")) {
            System.out.println("there is no Credentials in jsonResponse: " + jsonResponse.toPrettyString());
            throw new Exception("there is no Credentials in jsonResponse");
        }
        JsonNode jsonCredentials = jsonResponse.get("Credentials");

        Response response = new Response();
        if (!jsonCredentials.has("Token")) {
            System.out.println("there is no Token in jsonCredentials: " + jsonCredentials.toPrettyString());
            throw new Exception("there is no Token in jsonCredentials");
        }
        response.credentials.token = jsonCredentials.get("Token").asText();

        if (!jsonCredentials.has("TmpSecretId")) {
            System.out.println("there is no TmpSecretId in jsonCredentials: " + jsonCredentials.toPrettyString());
            throw new Exception("there is no TmpSecretId in jsonCredentials");
        }
        response.credentials.tmpSecretId = jsonCredentials.get("TmpSecretId").asText();

        if (!jsonCredentials.has("TmpSecretKey")) {
            System.out.println("there is no TmpSecretKey in jsonCredentials: " + jsonCredentials.toPrettyString());
            throw new Exception("there is no TmpSecretKey in jsonCredentials");
        }
        response.credentials.tmpSecretKey = jsonCredentials.get("TmpSecretKey").asText();

        if (!jsonResponse.has("ExpiredTime")) {
            System.out.println("there is no ExpiredTime in jsonResponse: " + jsonResponse.toPrettyString());
            throw new Exception("there is no ExpiredTime in jsonResponse");
        }
        response.expiredTime = jsonResponse.get("ExpiredTime").asLong();
        response.startTime = response.expiredTime - assumeRoleParam.getDurationSec();

        if (!jsonResponse.has("Expiration")) {
            System.out.println("there is no Expiration in jsonResponse: " + jsonResponse.toPrettyString());
            throw new Exception("there is no Expiration in jsonResponse");
        }
        response.expiration = jsonResponse.get("Expiration").asText();

        // 非必要的
        if (jsonResponse.has("RequestId")) {
            response.requestId = jsonResponse.get("RequestId").asText();
        } else {
            System.out.println("there is no RequestId in jsonResponse: " + jsonResponse.toPrettyString());
        }

        System.out.println("succeed to assume role: " + response);
        return response;
    }

    public static String getPolicy(List<Scope> scopes) {
        if(scopes == null || scopes.size() == 0) return null;

        Policy policy = new Policy();
        policy.setVersion("2.0");

        for (Scope scope: scopes) {
            Statement statement = new Statement();
            statement.setEffect(scope.getEffect());
            statement.addAction(scope.getAction());
            statement.addResource(scope.getResource());

            policy.addStatement(statement);
        }

        return Jackson.toJsonPrettyString(policy);
    }

    /**
     * construct policy string from parameters.
     * @param parameters
     * @return
     */
    static String getPolicy(Parameters parameters) {
        if(parameters.bucket == null) {
            throw new IllegalArgumentException("bucket == null");
        }
        if(parameters.allowPrefixes == null) {
            throw new IllegalArgumentException("allowPrefixes == null");
        }
        if(parameters.region == null) {
            throw new IllegalArgumentException("region == null");
        }

        Statement statement = new Statement();
        statement.setEffect("allow");

        for (String action : parameters.allowActions) {
            statement.addAction(action);
        }

        String bucket = parameters.bucket;
        String region = parameters.region;
        int lastSplit = bucket.lastIndexOf("-");
        String appId = bucket.substring(lastSplit + 1);
        for (String prefix : parameters.allowPrefixes) {
            String p = prefix;
            if(!p.startsWith("/")) {
                p = "/" + p;
            }
            String resource = String.format("qcs::cos:%s:uid/%s:%s%s",
                    region, appId, bucket, p);
            statement.addResource(resource);;
        }

        Policy policy = new Policy();
        policy.setVersion("2.0");
        policy.addStatement(statement);

        return Jackson.toJsonPrettyString(policy);
    }
    
    static class Parameters{
    	String secretId;
    	String secretKey;
    	int duration = DEFAULT_DURATION_SECONDS;
    	String bucket;
    	String region;
    	String[] allowPrefixes;
    	String[] allowActions;
    	String policy;
        Integer secretType; // option argument, only can choose 0 or 1, 0 for long certificate, 1 for short certificate
        String host;
    	
    	public void parse(Map<String, Object> config) {
			if(config == null) throw new NullPointerException("config == null");
			for(Map.Entry<String, Object> entry : config.entrySet()) {
				String key = entry.getKey();
				if("SecretId".equalsIgnoreCase(key)) {
					secretId = (String) entry.getValue();
				}else if("SecretKey".equalsIgnoreCase(key)) {
					secretKey = (String) entry.getValue();
				}else if("durationSeconds".equalsIgnoreCase(key)) {
					duration = (Integer) entry.getValue();
				}else if("bucket".equalsIgnoreCase(key)) {
					bucket = (String) entry.getValue();
				}else if("region".equalsIgnoreCase(key)) {
					region = (String) entry.getValue();
				}else if("allowPrefix".equalsIgnoreCase(key)) {
                    allowPrefixes = new String[] { (String) entry.getValue() };
				}else if("allowPrefixes".equalsIgnoreCase(key)) {
                    allowPrefixes = (String[]) entry.getValue();
                }else if("policy".equalsIgnoreCase(key)) {
					policy = (String) entry.getValue();
				}else if("allowActions".equalsIgnoreCase(key)) {
					allowActions = (String[]) entry.getValue();
				}else if("secretType".equalsIgnoreCase(key)) {
				    secretType = (Integer)entry.getValue();
				}else if("host".equalsIgnoreCase(key)) {
				    host = (String)entry.getValue();
				}
			}
		}
    }
}
