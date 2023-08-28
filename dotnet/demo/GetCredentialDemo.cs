using COSXML.Common;
using COSXML.CosException;
using COSXML.Model;
using COSXML.Model.Object;
using COSXML.Model.Tag;
using COSXML.Model.Bucket;
using COSXML.Model.Service;
using COSXML.Utils;
using COSXML.Auth;
using COSXML.Transfer;
using System;
using COSXML;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Net.Mail;
using COSSTS;

namespace COSSnippet
{
    public class PutObjectModel
    {

        private CosXml cosXml;

        //永久密钥
        string secretId = "AKIDOjrkRzMXWJ80f7Xmoa8bz94kDSXQ6aru";
        string secretKey = "rkkdIAoFfv8OTM2f9RhirrgMr9HCwZR5";

        string bucket = "dotnet-ut-obj-1318182572";
        string region = "ap-guangzhou";

        //临时密钥
        string Token = "";
        string TmpSecretId = "";
        string TmpSecretKey = "";

        int time = 600;

        /// 上传Byte数组
        public void PutObjectByte()
        {
            try
            {
                string key = "objectName1"; //对象键
                byte[] data = new byte[1024];

                PutObjectRequest request = new PutObjectRequest(bucket, key, data);
                //设置进度回调
                request.SetCosProgressCallback(delegate (long completed, long total)
                {
                    Console.WriteLine(String.Format("progress = {0:##.##}%", completed * 100.0 / total));
                });
                //执行请求
                PutObjectResult result = cosXml.PutObject(request);
                //对象的 eTag
                string eTag = result.eTag;
                //对象的 crc64ecma 校验值
                string crc64ecma = result.crc64ecma;
                //打印请求结果
                Console.WriteLine(result.GetResultInfo());
            }
            catch (COSXML.CosException.CosClientException clientEx)
            {
                //请求失败
                Console.WriteLine("CosClientException: " + clientEx);
            }
            catch (COSXML.CosException.CosServerException serverEx)
            {
                //请求失败
                Console.WriteLine("CosServerException: " + serverEx.GetInfo());
            }
        }

        // 获取联合身份临时访问凭证 https://cloud.tencent.com/document/product/1312/48195
        public void GetCredential()
        {
            string allowPrefix = "*"; // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
            string[] allowActions = new string[] {  // 允许的操作范围，这里以上传操作为例
                "name/cos:PutObject",
                "name/cos:PostObject",
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload",
            };
            Dictionary<string, object> values = new Dictionary<string, object>();
            values.Add("bucket", bucket);
            values.Add("region", region);
            values.Add("allowPrefix", allowPrefix);
            // 也可以通过 allowPrefixes 指定路径前缀的集合
            values.Add("allowActions", allowActions);
            values.Add("durationSeconds", time);

            values.Add("secretId", secretId);
            values.Add("secretKey", secretKey);

            //获取临时密钥
            Dictionary<string, object> credential = STSClient.genCredential(values);

            var data = credential["Credentials"] as Newtonsoft.Json.Linq.JObject;
            Token = data["Token"].ToString();
            TmpSecretId = data["TmpSecretId"].ToString();
            TmpSecretKey = data["TmpSecretKey"].ToString();
        }

        //初始化cosXml
        public void InitCosXml()
        {
            CosXmlConfig config = new CosXmlConfig.Builder().SetRegion(region).Build();

            //时间设置
            long sratTime = new DateTimeOffset(DateTime.UtcNow).ToUnixTimeSeconds();//生效开始时间
            long endtime = sratTime + 600;//生效结束时间

            QCloudCredentialProvider qCloudCredentialProvider = new DefaultSessionQCloudCredentialProvider(TmpSecretId, TmpSecretKey, sratTime, endtime, Token);
            this.cosXml = new CosXmlServer(config, qCloudCredentialProvider);
        }


        static void Main(string[] args)
        {
            PutObjectModel m = new PutObjectModel();
            m.GetCredential();
            m.InitCosXml();
            m.PutObjectByte();
        }

    }

}