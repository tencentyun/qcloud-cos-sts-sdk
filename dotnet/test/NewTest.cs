using System;
using System.Collections.Generic;
using System.Threading.Tasks;

using COSSTS;

using Newtonsoft.Json;

using NUnit.Framework;

using TencentCloud.Common;
using TencentCloud.Sts.V20180813;

namespace Tests
{
    [TestFixture()]
    public class NewTest
    {
        private Dictionary<string, object> _values;
        private StsClient _client;

        /// <summary>
        /// 您的 bucket
        /// </summary>
        private readonly string _bucket = "examplebucket-1253653367";

        /// <summary>
        /// bucket 所在区域
        /// </summary>
        private readonly string _region = "ap-guangzhou";

        /// <summary>
        /// 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
        /// </summary>
        private readonly string _allowPrefix = "exampleobject";

        /// <summary>
        /// 允许的操作范围，这里以上传操作为例
        /// </summary>
        private readonly string[] _allowActions = new string[] {
                "name/cos:PutObject",
                "name/cos:PostObject",
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload"
            };

        [SetUp]
        public void Setup()
        {
            _values = new Dictionary<string, object>();

            string secretId = Environment.GetEnvironmentVariable("COS_KEY"); // 云 API 密钥 Id
            string secretKey = Environment.GetEnvironmentVariable("COS_SECRET"); // 云 API 密钥 Key

            _values.Add("bucket", _bucket);
            _values.Add("region", _region);
            _values.Add("allowPrefix", _allowPrefix);
            _values.Add("allowActions", _allowActions);
            _values.Add("durationSeconds", 1800);

            _values.Add("secretId", secretId);
            _values.Add("secretKey", secretKey);

            Credential cred = new Credential
            {
                SecretId = secretId,
                SecretKey = secretKey
            };

            _client = new StsClient(cred, _region);
        }

        [Test]
        public async Task TestSTSClientAsync()
        {
            string credentialOld = STSClient.genCredential(_values);
            TestContext.Progress.WriteLine(credentialOld);
            Assert.NotNull(credentialOld);

            var tokenResponse = await _client.GenCredentialAsync(_region, _bucket, _allowPrefix, _allowActions, 1800);
            var credentialNew = JsonConvert.SerializeObject(tokenResponse);
            TestContext.Progress.WriteLine(credentialNew);
            Assert.NotNull(tokenResponse.Credentials?.Token);
        }

        [Test]
        public void TestPolicy()
        {
            var policyOld = STSClient.getPolicy(
                     (string)_values["region"], (string)_values["bucket"],
                     (string)_values["allowPrefix"], (string[])_values["allowActions"]);
            TestContext.Progress.WriteLine(policyOld);
            Assert.NotNull(policyOld);

            var policy = CosStsCredential.GetPolicy(_region, _bucket, _allowPrefix, _allowActions);
            var policyNew = JsonConvert.SerializeObject(policy);

            TestContext.Progress.WriteLine(policyNew);
            Assert.NotNull(policyNew);

            Assert.AreEqual(policyOld, policyNew);
        }
    }
}