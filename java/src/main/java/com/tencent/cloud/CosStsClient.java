package com.tencent.cloud;

import com.qcloud.Module.Sts;
import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.Utilities.Json.JSONArray;
import com.qcloud.Utilities.Json.JSONObject;

import java.io.IOException;
import java.util.TreeMap;

public class CosStsClient {

    private static final int DEFAULT_DURATION_SECONDS = 1800;

    public static JSONObject getCredential(TreeMap<String, Object> config) throws Exception {
        config.put("RequestMethod", "GET");

        QcloudApiModuleCenter module = new QcloudApiModuleCenter(new Sts(), config);

        TreeMap<String, Object> params = new TreeMap<String, Object>();

        params.put("name", "cos-sts-java");
        String policy = (String) config.get("policy");

        if (policy != null) {
            params.put("policy", policy);
        } else {
            params.put("policy", getPolicy(config).toString());
        }

        int durationSeconds = DEFAULT_DURATION_SECONDS;
        if (config.get("durationSeconds") != null) {
            durationSeconds = (Integer) config.get("durationSeconds");
        }
        params.put("durationSeconds", durationSeconds);

        try {
            String result = module.call("GetFederationToken", params);
            JSONObject jsonResult = new JSONObject(result);
            JSONObject data = jsonResult.optJSONObject("data");
            if (data == null) {
                data = jsonResult;
            }
            data.put("startTime", System.currentTimeMillis() / 1000);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private static JSONObject getPolicy(TreeMap<String, Object> config) {
        String bucket = (String) config.get("bucket");
        String region = (String) config.get("region");
        String allowPrefix = (String) config.get("allowPrefix");
        String[] allowActions = (String[]) config.get("allowActions");

        JSONObject policy = new JSONObject();
        policy.put("version", "2.0");

        JSONObject statement = new JSONObject();
        policy.put("statement", statement);

        statement.put("effect", "allow");
        JSONObject principal = new JSONObject();
        principal.put("qcs", "*");
        statement.put("principal", principal);

        JSONArray actions = new JSONArray();
        for (String action : allowActions) {
            actions.put(action);
        }
        statement.put("action", actions);

        int lastSplit = bucket.lastIndexOf("-");
        String shortBucketName = bucket.substring(0, lastSplit);
        String appId = bucket.substring(lastSplit + 1);

        String resource = String.format("qcs::cos:%s:uid/%s:prefix//%s/%s/%s",
                region, appId, appId, shortBucketName, allowPrefix);
        statement.put("resource", resource);

        return policy;

    }
}
