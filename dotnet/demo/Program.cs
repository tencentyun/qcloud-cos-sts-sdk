using System;
using System.Collections.Generic;
using COSSTS;

namespace demo
{
    class Program
    {
        static void Main(string[] args)
        {

            string bucket = "examplebucket-1253653367";  // 您的 bucket
            string region = "ap-guangzhou";  // bucket 所在区域
            string allowPrefix = "exampleobject"; // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
            string[] allowActions = new string[] {  // 允许的操作范围，这里以上传操作为例
                "name/cos:PutObject",
                "name/cos:PostObject",
                "name/cos:InitiateMultipartUpload",
                "name/cos:ListMultipartUploads",
                "name/cos:ListParts",
                "name/cos:UploadPart",
                "name/cos:CompleteMultipartUpload"
            };
            // Demo 这里是从环境变量读取，如果是直接硬编码在代码中，请参考：
            // string secretId = "AKIDXXXXXXXXX";
            string secretId = Environment.GetEnvironmentVariable("COS_KEY"); // 云 API 密钥 Id
            string secretKey = Environment.GetEnvironmentVariable("COS_SECRET"); // 云 API 密钥 Key

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
            values.Add("durationSeconds", 1800);

            values.Add("secretId", secretId);
            values.Add("secretKey", secretKey);
            
            values.Add("Domain", "sts.tencentcloudapi.com");

            // Credentials = {
            //   "Token": "4oztDXOAAI3c6qUE5TkNuVzSP1tUQz15f3f946eb08f9411d3d61505cc4bc74cczCZLchkvRmmrqzE09ELVw35gzYlBXsQp03PBpL79ubLvoAMWbBgSMmI6eApmhqv7NFeDdKJlikVe0fNCU2NNUe7cHrgttfTIK87ZnC86kww-HysFgIGeBNWpwo4ih0lV0z9a2WiTIjPoeDBwPU4YeeAVQAGPnRgHALoL2FtxNsutFzDjuryRZDK7Am4Cs9YxpZHhG7_F_II6363liKNsHTk8ONRZrNxKiOqvFvyhsJ-oTTUg0I0FT4_xo0lq5zR9yyySXHbE7z-2im4rgnK3sBagN47zkgltJyefJmaPUdDgGmvaQBO6TqxiiszOsayS7CxCZK1yi90H2KS3xRUYTLf94aVaZlufrIwntXIXZaHOKHmwuZuXl7HnHoXbfg_YENoLP6JAkDCw0GOFEGNOrkCuxRtcdJ08hysrwBw1hmYawDHkbyxYkirY-Djg7PswiC4_juBvG0iwjzVwE0W_rhxIa7YtamLnZJxQk9dyzbbl0F4DTYwS101Hq9wC7jtifkXFjBFTGRnfPe85K-hEnJLaEy7eYfulIPI9QiIUxi4BLPbzjD9j3qJ4Wdt5oqk9XcF9y5Ii2uQx1eymNl7qCA",
            //   "TmpSecretId": "xxxxxxxxxxxx",
            //   "TmpSecretKey": "PZ/WWfPZFYqahPSs8URUVMc8IyJH+T24zdn8V1cZaMs="
            // }
            // ExpiredTime = 1597916602
            // Expiration = 2020/8/20 上午9:43:22
            // RequestId = 2b731be1-ebe8-4638-8a72-906bc564a55a
            // StartTime = 1597914802
            Dictionary<string, object> credential = STSClient.genCredential(values);
            foreach (KeyValuePair<string, object> kvp in credential)
            {
                Console.WriteLine("{0} = {1}", kvp.Key, kvp.Value);
            }
        }
    }
}
