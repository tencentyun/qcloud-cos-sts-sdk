using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

using Newtonsoft.Json;

using TencentCloud.Sts.V20180813;
using TencentCloud.Sts.V20180813.Models;

namespace COSSTS
{
    /// <summary>
    ///
    /// </summary>
    public static class CosStsCredential
    {
        /// <summary>
        /// 默认持续时间
        /// </summary>
        public const ulong DefaultDurationSeconds = 1800;

        /// <summary>
        ///
        /// </summary>
        /// <param name="client"></param>
        /// <param name="region">存储桶所属地域</param>
        /// <param name="bucket">存储桶名称</param>
        /// <param name="allowPrefix">资源的前缀</param>
        /// <param name="allowActions">授予 COS API 权限集合</param>
        /// <param name="durationSeconds">持续时间</param>
        /// <returns></returns>
        public static async Task<GetFederationTokenResponse> GenCredentialAsync(this StsClient client,
                                                                                string region,
                                                                                string bucket,
                                                                                string allowPrefix,
                                                                                IEnumerable<string> allowActions,
                                                                                ulong durationSeconds = DefaultDurationSeconds)
        {
            var policy = GetPolicy(region, bucket, allowPrefix, allowActions);

            var req = new GetFederationTokenRequest()
            {
                DurationSeconds = durationSeconds,
                Name = "cos-sts-sdk-dotnet",
                Policy = JsonConvert.SerializeObject(policy),
            };

            GetFederationTokenResponse resp = await client.GetFederationToken(req).ConfigureAwait(false);
            return resp;
        }

        /// <summary>
        /// 获取策略
        /// </summary>
        /// <param name="region">存储桶所属地域</param>
        /// <param name="bucket">存储桶名称</param>
        /// <param name="allowPrefix">资源的前缀</param>
        /// <param name="allowActions">授予 COS API 权限集合</param>
        /// <returns></returns>
        public static CosPolicy GetPolicy(string region, string bucket, string allowPrefix, IEnumerable<string> allowActions)
        {
            var appId = bucket.Split('-').Last();
            if (!allowPrefix.StartsWith("/"))
            {
                allowPrefix = $"/{allowPrefix}";
            }

            var policy = new CosPolicy
            {
                Statement = allowActions.Select(action => new Scope(action, $"qcs::cos:{region}:uid/{appId}:{bucket}{allowPrefix}")).ToArray()
            };

            return policy;
        }
    }
}