
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Net.Mail;
using COSSTS;
using Newtonsoft.Json;
using Formatting = System.Xml.Formatting;


namespace COSSnippet
{
    public class GetKeyAndCredentials
    {
        //永久密钥
        string secretId = "";
        string secretKey = "";

        string bucket = "bucket-125000000";
        string appId = "125000000";
        string region = "ap-guangzhou";
        string ext = "jpg";
        int time = 1800;
        
        
        // 限制
        Boolean limitExt = false; // 限制上传文件后缀
        List<string> extWhiteList = new List<String> { "jpg", "jpeg", "png", "gif", "bmp" }; // 限制的上传后缀
        Boolean limitContentType = false; // 限制上传 contentType
        Boolean limitContentLength = false; // 限制上传文件大小

        
        public string generateCosKey(string ext)
        {
            DateTime date = DateTime.Now;
            int m = date.Month;
            string ymd = $"{date.Year}{(m < 10 ? $"0{m}" : m.ToString())}{date.Day}";
        
            Random random = new Random();
            string r = random.Next(0, 1000000).ToString("D6"); // 生成6位随机数，前面补零
        
            string cosKey = $"file/{ymd}/{ymd}_{r}.{(string.IsNullOrEmpty(ext) ? "" : ext)}";
            return cosKey;
        }
        
        public Dictionary<string, object> getConfig()
        {
            Dictionary<string, object> config = new Dictionary<string, object>();
            string[] allowActions = new string[] {  // 允许的操作范围，这里以上传操作为例
                "name/cos:PutObject",
                "name/cos:PostObject",
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload",
            };
            
            string resource = $"qcs::cos:{region}:uid/{appId}:{bucket}/{generateCosKey(ext)}";
            
            var condition = new Dictionary<string, object>();
            
            // 1. 限制上传文件后缀
            if (limitExt)
            {
                var extInvalid = string.IsNullOrEmpty(ext) || !extWhiteList.Contains(ext);
                if (extInvalid)
                {
                    Console.WriteLine("非法文件，禁止上传");
                    return null;
                }
            }

            // 2. 限制上传文件 content-type
            if (limitContentType)
            {
                condition["string_like"] = new Dictionary<string, string>
                {
                    { "cos:content-type", "image/*" } // 只允许上传 content-type 为图片类型
                };
            }

            // 3. 限制上传文件大小
            if (limitContentLength)
            {
                condition["numeric_less_than_equal"] = new Dictionary<string, long>
                {
                    { "cos:content-length", 5 * 1024 * 1024 } // 上传大小限制不能超过 5MB
                };
            }

            var policy = new Dictionary<string, object>
            {
                { "version", "2.0" },
                { "statement", new List<Dictionary<string, object>>
                    {
                        new Dictionary<string, object>
                        {
                            { "action", allowActions },
                            { "effect", "allow" },
                            { "resource", new List<string>
                                {
                                    resource,
                                }
                            },
                            { "condition", condition }
                        }
                    }
                }
            };

            // 序列化为 JSON 并输出
            string jsonPolicy = JsonConvert.SerializeObject(policy);
            
            config.Add("bucket", bucket);
            config.Add("region", region);
            config.Add("durationSeconds", time);

            config.Add("secretId", secretId);
            config.Add("secretKey", secretKey);
            config.Add("policy", jsonPolicy);
            return config;
        }
        
        // 获取联合身份临时访问凭证 https://cloud.tencent.com/document/product/1312/48195
        public Dictionary<string, object> GetCredential()
        {

            var config = getConfig();
            //获取临时密钥
            Dictionary<string, object> credential = STSClient.genCredential(config);
            return credential;
        }
        
        static void Main(string[] args)
        {
            GetKeyAndCredentials m = new GetKeyAndCredentials();
            Dictionary<string, object> result = m.GetCredential();
            Console.WriteLine("Credentials:" + result["Credentials"]);
            Console.WriteLine("ExpiredTime:" + result["ExpiredTime"]);
            Console.WriteLine("StartTime:" + result["StartTime"]);
        }
    }
}
