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
        public static string genCredential(Dictionary<string, object> values) {
            Credential cred = new Credential {
                SecretId = (string) values["secretId"],
                SecretKey = (string) values["secretKey"]
            };

            ClientProfile clientProfile = new ClientProfile();
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.Endpoint = ("sts.tencentcloudapi.com");
            clientProfile.HttpProfile = httpProfile;

            string region = (string) values["region"];
            string bucket = (string) values["bucket"];
            string allowPrefix = (string) values["allowPrefix"];
            string[] allowActions = (string[]) values["allowActions"];
            string policy = getPolicy(region, bucket, allowPrefix, allowActions);

            Dictionary<string, object> body = new Dictionary<string, object>();
            body.Add("DurationSeconds", (Int32) values["durationSeconds"]);
            body.Add("Name", "cos-sts-sdk-dotnet");
            body.Add("Policy", policy);

            StsClient client = new StsClient(cred, region, clientProfile);
            GetFederationTokenRequest req = new GetFederationTokenRequest();

            string strParams = JsonConvert.SerializeObject(body);
            req = GetFederationTokenRequest.FromJsonString<GetFederationTokenRequest>(strParams);
            GetFederationTokenResponse resp = client.GetFederationToken(req).
                ConfigureAwait(false).GetAwaiter().GetResult();
            return JsonConvert.SerializeObject(resp);
        }

        public static string getPolicy(string region, string bucket, string allowPrefix,
            string[] allowActions) {
            Dictionary<string, object> policy = new Dictionary<string, object>();
            List<Dictionary<string, string>> states = new List<Dictionary<string, string>>();
            foreach (string action in allowActions) {
                Dictionary<string, string> dic = new Dictionary<string, string>();
                dic.Add("action", action);
                dic.Add("effect", "allow");

                string[] splitParts = bucket.Split('-');
                string appId = splitParts[splitParts.Length - 1];
                string bucketName = bucket.Substring(0, bucket.Length - appId.Length - 1);
                if (!allowPrefix.StartsWith("/")) {
                    allowPrefix = "/" + allowPrefix;
                }
                dic.Add("resource", string.Format("qcs::cos:{0}:uid/{1}:{2}{3}",
                    region, appId, bucketName, allowPrefix));
                states.Add(dic);
            }
            policy.Add("version", "2.0");
            policy.Add("statement", states);
            
            return JsonConvert.SerializeObject(policy);
        }
    }
}
