using System;
using System.Threading.Tasks;
using TencentCloud.Common;
using TencentCloud.Common.Profile;
using TencentCloud.Sts.V20180813;
using TencentCloud.Sts.V20180813.Models;
using Newtonsoft.Json;

using System.Collections.Generic;

namespace COSSTS
{
    public class STSClient
    {
        public static Dictionary<string, object> genCredential(Dictionary<string, object> values) {
            checkArguments(values, new string[] {"secretId", "secretKey", "region"});

            Credential cred = new Credential {
                SecretId = (string) values["secretId"],
                SecretKey = (string) values["secretKey"]
            };
            string region = (string) values["region"];

            ClientProfile clientProfile = new ClientProfile();
            HttpProfile httpProfile = new HttpProfile();
            String endpoint = values.ContainsKey("Domain") ? (string) values["Domain"]: 
                "sts.tencentcloudapi.com";
            httpProfile.Endpoint = endpoint;
            clientProfile.HttpProfile = httpProfile;

            // get policy
            string policy = null;
            if (values.ContainsKey("policy")) {
                policy = (string) values["policy"];
            }
            if (policy == null) {
                checkArguments(values, new string[] {"bucket", "allowActions"});
                string bucket = (string) values["bucket"];
                string[] allowActions = (string[]) values["allowActions"];
                string[] allowPrefixes;
                if (values.ContainsKey("allowPrefix")) {
                    allowPrefixes = new string[] {(string) values["allowPrefix"]};
                } else if (values.ContainsKey("allowPrefixes")) {
                    allowPrefixes = (string[]) values["allowPrefixes"];
                } else {
                    throw new System.ArgumentException("allowPrefix and allowPrefixes are both null.");
                }
                policy = getPolicy(region, bucket, allowPrefixes, allowActions);
            }

            // duration
            Int32 durationSeconds = 1800;
            if (values.ContainsKey("durationSeconds")) {
                durationSeconds = (Int32) values["durationSeconds"]; 
            }

            Dictionary<string, object> body = new Dictionary<string, object>();
            body.Add("DurationSeconds", durationSeconds);
            body.Add("Name", "cos-sts-sdk-dotnet");
            body.Add("Policy", policy);

            StsClient client = new StsClient(cred, region, clientProfile);
            GetFederationTokenRequest req = new GetFederationTokenRequest();

            string strParams = JsonConvert.SerializeObject(body);
            req = GetFederationTokenRequest.FromJsonString<GetFederationTokenRequest>(strParams);
            GetFederationTokenResponse resp = client.GetFederationTokenSync(req);
            string jsonString = JsonConvert.SerializeObject(resp);
            Dictionary<string, object> dic = JsonConvert.DeserializeObject<Dictionary<string, object>>(jsonString);
            if (dic.ContainsKey("ExpiredTime")) {
                dic.Add("StartTime", Int32.Parse(dic["ExpiredTime"].ToString()) - durationSeconds);
            }
            return dic;
        }

        public static Dictionary<string, object> getRoleCredential(Dictionary<string, object> values) {
            checkArguments(values, new string[] {"secretId", "secretKey", "region"});

            Credential cred = new Credential {
                SecretId = (string) values["secretId"],
                SecretKey = (string) values["secretKey"]
            };
            string region = (string) values["region"];

            ClientProfile clientProfile = new ClientProfile();
            HttpProfile httpProfile = new HttpProfile();
            String endpoint = values.ContainsKey("Domain") ? (string) values["Domain"]: 
                "sts.tencentcloudapi.com";
            httpProfile.Endpoint = endpoint;
            clientProfile.HttpProfile = httpProfile;

            // get policy
            string policy = null;
            if (values.ContainsKey("policy")) {
                policy = (string) values["policy"];
            }
            if (policy == null) {
                checkArguments(values, new string[] {"bucket", "allowActions"});
                string bucket = (string) values["bucket"];
                string[] allowActions = (string[]) values["allowActions"];
                string[] allowPrefixes;
                if (values.ContainsKey("allowPrefix")) {
                    allowPrefixes = new string[] {(string) values["allowPrefix"]};
                } else if (values.ContainsKey("allowPrefixes")) {
                    allowPrefixes = (string[]) values["allowPrefixes"];
                } else {
                    throw new System.ArgumentException("allowPrefix and allowPrefixes are both null.");
                }
                policy = getPolicy(region, bucket, allowPrefixes, allowActions);
            }

            // duration
            Int32 durationSeconds = 1800;
            if (values.ContainsKey("durationSeconds")) {
                durationSeconds = (Int32) values["durationSeconds"]; 
            }

            // RoleArn
            string roleArn = null;
            if (values.ContainsKey("roleArn")) {
                roleArn = (string) values["roleArn"];
            } else {
                throw new System.ArgumentException("must have roleArn param in GetRoleCredential.");
            }

            // RuleSessionName
            string roleSessionName = null;
            if (values.ContainsKey("roleSessionName")) {
                roleSessionName = (string) values["roleSessionName"];
            } else {
                roleSessionName = "cos-sts-sdk-dotnet";
            }

            Dictionary<string, object> body = new Dictionary<string, object>();
            body.Add("DurationSeconds", durationSeconds);
            body.Add("Name", "cos-sts-sdk-dotnet");
            body.Add("RoleSessionName", roleSessionName);
            body.Add("Policy", policy);
            body.Add("RoleArn", roleArn);

            StsClient client = new StsClient(cred, region, clientProfile);
            AssumeRoleRequest req = new AssumeRoleRequest();

            string strParams = JsonConvert.SerializeObject(body);
            req = AssumeRoleRequest.FromJsonString<AssumeRoleRequest>(strParams);
            AssumeRoleResponse resp = client.AssumeRoleSync(req);
            string jsonString = JsonConvert.SerializeObject(resp);
            Dictionary<string, object> dic = JsonConvert.DeserializeObject<Dictionary<string, object>>(jsonString);
            if (dic.ContainsKey("ExpiredTime")) {
                dic.Add("StartTime", Int32.Parse(dic["ExpiredTime"].ToString()) - durationSeconds);
            }
            return dic;
        }

        private static void checkArguments(Dictionary<string, object> values, string[] args) {
            foreach (string arg in args) {
                if (!values.ContainsKey(arg) || values[arg] == null) {
                    // throw exception
                    throw new System.ArgumentNullException(arg);
                }
            }
        }

        public static string getPolicy(string region, string bucket,
            string[] allowPrefixes, string[] allowActions) {
            Dictionary<string, object> policy = new Dictionary<string, object>();
            List<Dictionary<string, object>> states = new List<Dictionary<string, object>>();
            Dictionary<string, object> dic = new Dictionary<string, object>();
            dic.Add("action", allowActions);
            dic.Add("effect", "allow");
            string[] splitParts = bucket.Split('-');
            string appId = splitParts[splitParts.Length - 1];
            List<String> resources = new List<String>();
            foreach (string prefix in allowPrefixes) {
                string p = prefix;
                if (!p.StartsWith("/")) {
                    p = "/" + prefix;
                }
                resources.Add(string.Format("qcs::cos:{0}:uid/{1}:{2}{3}",
                    region, appId, bucket, p));
            }
            dic.Add("resource", resources.ToArray());
            states.Add(dic);
            policy.Add("version", "2.0");
            policy.Add("statement", states);
            
            return JsonConvert.SerializeObject(policy);
        }
    }
}
