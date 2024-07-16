using COSSTS;
using COSXML;
using COSXML.Auth;
using COSXML.Model.Object;

namespace Demo
{
    class Program
    {
        private CosXmlServer cosXml;
        
        private string regionVar = "ap-guangzhou";//填充用户桶所属的region
        private string bucketVar = "bucketname-123242345";//填充用户桶 bucketname-123242345 格式的

         // 获取联合身份临时访问凭证 https://cloud.tencent.com/document/product/1312/48195
        private Dictionary<string, object> GetCredentialDemo()
        {
            string bucket = bucketVar; // 您的 bucket
            string region = regionVar; // bucket 所在区域

            // 改成允许的路径前缀，根据自己网站的用户判断允许上传的路径，例子:a.jpg 或者 a/* 或者 * (通配符*存在重大安全风险, 谨慎评估使用)
            string allowPrefix = "*";

            /*
             * 密钥的权限列表。必须在这里指定本次临时密钥所需要的权限。权限列表请参见 https://cloud.tencent.com/document/product/436/31923
             * 规则为 {project}:{interfaceName}
             * project : 产品缩写  cos相关授权为值为cos,数据万象(数据处理)相关授权值为ci
             * 授权所有接口用*表示，例如 cos:*,ci:*
             */
            string[] allowActions = new string[]
            {
                "name/cos:PutObject",
                "name/cos:PostObject",
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload"
            };
            
            //设置参数
            Dictionary<string, object> values = new Dictionary<string, object>();
            values.Add("bucket", bucket);
            values.Add("region", region);
            values.Add("allowPrefix", allowPrefix);
            // 也可以通过 allowPrefixes 指定路径前缀的集合
            // values.Add("allowPrefixes", new string[] {
            //     "path/to/dir1/*",
            //     "path/to/dir2/*",
            // });
            values.Add("allowActions", allowActions);
            values.Add("durationSeconds", 1800);//指定临时证书的有效期, 参考 https://cloud.tencent.com/document/product/1312/48195
            
            // Demo 这里是从环境变量读取，如果是直接硬编码在代码中可以直接（ string secretId = "secretId-DSFser";)：
            string secretId = Environment.GetEnvironmentVariable("COS_KEY"); // 云 API 密钥 Id
            string secretKey = Environment.GetEnvironmentVariable("COS_SECRET"); // 云 API 密钥 Key
            
            values.Add("secretId", secretId);
            values.Add("secretKey", secretKey);

            // 设置域名
            // values.Add("Domain", "sts.tencentcloudapi.com");
            
            /*  STSClient.genCredential打印的返回值示例
             *  Credentials = {
             *     "Token": "4oztDXOAAI3c6qUE5TkNudfkmkjgnwlirngwjngmcwkfzSP...",
             *     "TmpSecretId": "xxxxxxxxxxxx",
             *     "TmpSecretKey": "PZ/WWfPZFYqahPSs8URUVMc8IyJH+T24zdn8V1cZaMs="
             *   }
             *   ExpiredTime = 1597916602
             *   Expiration = 2020/8/20 上午9:43:22
             *   RequestId = 2b731be1-ebe8-4638-8a72-906bc564a55a
             *   StartTime = 1597914802
             */
            Dictionary<string, object> credential = STSClient.genCredential(values); //返回值说明见README.md
            // foreach (KeyValuePair<string, object> kvp in credential)
            // {
            //     Console.WriteLine("{0} = {1}", kvp.Key, kvp.Value);
            // }
            return credential;
        }
        
        static void Main(string[] args)
        {
            Program demo = new Program(); 
            
            //根据永久密钥通过接口获取临时密钥
            Dictionary<string, object> credentialDict = demo.GetCredentialDemo();

            //根据永久密钥通过接口获取临时密钥
            var credentials = credentialDict["Credentials"] as Newtonsoft.Json.Linq.JObject; 
            demo.InitCosXml(credentials["TmpSecretId"].ToString(), credentials["TmpSecretKey"].ToString(), credentials["Token"].ToString());
            
            //使用临时密钥初始化的cos服务上传数据
            demo.PutObjectByte();
        }

         //初始化COS服务
        private void InitCosXml(string TmpSecretId, string TmpSecretKey, string Token)
        {
            CosXmlConfig config = new CosXmlConfig.Builder().SetRegion(regionVar).Build();
            // //时间设置
            long sratTime = new DateTimeOffset(DateTime.UtcNow).ToUnixTimeSeconds();//生效开始时间
            long endtime = sratTime + 600;//生效结束时间
            QCloudCredentialProvider qCloudCredentialProvider = new DefaultSessionQCloudCredentialProvider(TmpSecretId, TmpSecretKey, sratTime, endtime, Token);
            cosXml = new CosXmlServer(config, qCloudCredentialProvider);//初始化服务到数据成员
        }
        
        //上传数据示例
        public void PutObjectByte()
        {
            try
            {
                string key = "demoObjectKey"; //对象键
                byte[] data = new byte[1024];
                
                PutObjectRequest request = new PutObjectRequest(bucketVar, key, data);
                //设置进度回调
                request.SetCosProgressCallback(delegate (long completed, long total)
                {
                    Console.WriteLine("progress = {0:##.##}%", completed * 100.0 / total);
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
                Console.WriteLine("CosClientException: " + clientEx);
            }
            catch (COSXML.CosException.CosServerException serverEx)
            {
                Console.WriteLine("CosServerException: " + serverEx.GetInfo());
            }
        }
    }
}
