package com.tencent.tac;

import com.qcloud.Module.Sts;
import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.Utilities.Json.JSONObject;

import java.util.TreeMap;

public class StorageSts {

    private static final String POLICY = "{\"statement\": [{\"action\": [\"name/cos:*\"],\"effect\": \"allow\",\"resource\":\"*\"}],\"version\": \"2.0\"}";
    private static final int DEFAULT_DURATION_IN_SECONDS = 1800;

    public static JSONObject getCredential(TreeMap<String, Object> config) {
        config.put("RequestMethod", "GET");

        QcloudApiModuleCenter module = new QcloudApiModuleCenter(new Sts(),
                config);

        TreeMap<String, Object> params = new TreeMap<String, Object>();

        params.put("name", "tac-storage-sts-java");
        String policy = config.get("policy") == null ? POLICY : (String) config.get("policy");
        params.put("policy", policy);
        int durationInSeconds = config.get("durationInSeconds") == null ? DEFAULT_DURATION_IN_SECONDS :
                (Integer) config.get("durationInSeconds");
        params.put("durationSeconds", durationInSeconds);

        try {
            /* call 方法正式向指定的接口名发送请求，并把请求参数 params 传入，返回即是接口的请求结果。 */
            String result = module.call("GetFederationToken", params);
            return new JSONObject(result);
        } catch (Exception e) {
            System.out.println("error..." + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
