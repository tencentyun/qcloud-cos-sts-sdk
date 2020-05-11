package com.tencent.cloud;

import com.tencent.cloud.cos.util.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CosStsClient {

    private static final int DEFAULT_DURATION_SECONDS = 1800;

    public static JSONObject getCredential(TreeMap<String, Object> config) throws IOException {
        TreeMap<String, Object> params = new TreeMap<String, Object>();
        Parameters parameters = new Parameters();
        parameters.parse(config);

        if(parameters.secretId == null) throw new IllegalArgumentException("secretId is null");
        if(parameters.secretKey == null) throw new IllegalArgumentException("secretKey is null");
        
        String policy = parameters.policy;
        if (policy != null) {
            params.put("Policy", policy);
        } else {
            params.put("Policy", getPolicy(parameters).toString());
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

        String host = "sts.tencentcloudapi.com";
        String path = "/";

        String result = null;
        JSONObject jsonResult = null;
        try {
            result = Request.send(params, (String) parameters.secretId,
                    parameters.secretKey,
                    "POST", host, path);
            jsonResult = new JSONObject(result);
            JSONObject data = jsonResult.optJSONObject("Response");
            if (data == null) {
                data = jsonResult;
            }
            long expiredTime = data.getLong("ExpiredTime");
            data.put("startTime", expiredTime - parameters.duration);
            return downCompat(data);
        } catch (Exception e) {
            if (jsonResult != null) {
                JSONObject response = jsonResult.optJSONObject("Response");
                if (response != null) {
                    JSONObject error = response.optJSONObject("Error");
                    if (error != null) {
                        String message = error.optString("Message");
                        String code = error.optString("Code");
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
    	if(scopes == null || scopes.size() == 0)return null;
    	STSPolicy stsPolicy = new STSPolicy();
    	stsPolicy.addScope(scopes);
    	return stsPolicy.toString();
    }

    // v2接口的key首字母小写，v3改成大写，此处做了向下兼容
    private static JSONObject downCompat(JSONObject resultJson) {
        JSONObject dcJson = new JSONObject();

        for (String key : resultJson.keySet()) {
            Object value = resultJson.get(key);
            if (value instanceof JSONObject) {
                dcJson.put(headerToLowerCase(key), downCompat((JSONObject) value));
            } else {
                String newKey = "Token".equals(key) ? "sessionToken" : headerToLowerCase(key);
                dcJson.put(newKey, resultJson.get(key));
            }
        }

        return dcJson;
    }

    private static String headerToLowerCase(String source) {
        return Character.toLowerCase(source.charAt(0)) + source.substring(1);
    }

    private static JSONObject getPolicy(Parameters parameters) {
        if(parameters.bucket == null) {
            throw new IllegalArgumentException("bucket == null");
        }
        if(parameters.allowPrefixes == null) {
            throw new IllegalArgumentException("allowPrefixes == null");
        }
        if(parameters.region == null) {
            throw new IllegalArgumentException("region == null");
        }
        String bucket = parameters.bucket;
        String region = parameters.region;

        int lastSplit = bucket.lastIndexOf("-");
        String appId = bucket.substring(lastSplit + 1);

        String[] allowPrefixes = parameters.allowPrefixes;
        List<String> resources = new ArrayList<String>(allowPrefixes.length);
        for (String prefix : allowPrefixes) {
            String p = prefix;
            if(!p.startsWith("/")) {
                p = "/" + p;
            }
            String resource = String.format("qcs::cos:%s:uid/%s:%s%s",
                    region, appId, bucket, p);
            resources.add(resource);
        }

        JSONObject policy = new JSONObject();
        policy.put("version", "2.0");

        JSONArray statements = new JSONArray();
        JSONObject statement = new JSONObject();
        statement.put("effect", "allow");
        statement.put("action", parameters.allowActions);
        statement.put("resource", resources.toArray(new String[0]));
        statements.put(statement);

        policy.put("statement", statements);
        return policy;
    }
    
    private static class Parameters{
    	String secretId;
    	String secretKey;
    	int duration = DEFAULT_DURATION_SECONDS;
    	String bucket;
    	String region;
    	String[] allowPrefixes;
    	String[] allowActions;
    	String policy;
    	Integer secretType; // option argument, only can choose 0 or 1, 0 for long certificate, 1 for short certificate
    	
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
				}
			}
		}
    }
}
