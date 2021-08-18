package com.tencent.cloud;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.tencent.cloud.cos.util.Jackson;
import com.tencent.cloud.cos.util.Request;

public class CosStsClient {

    private static final int DEFAULT_DURATION_SECONDS = 1800;
    public static final String STS_DEFAULT_HOST = "sts.tencentcloudapi.com";

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
