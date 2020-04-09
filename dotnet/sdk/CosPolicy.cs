using System.Collections.Generic;

using Newtonsoft.Json;

namespace COSSTS
{
    /// <summary>
    /// Cos策略
    /// </summary>
    public class CosPolicy
    {
        /// <summary>
        /// 版本
        /// </summary>
        [JsonProperty("version")]
        public string Version { get; } = "2.0";

        /// <summary>
        /// 策略说明
        /// </summary>
        [JsonProperty("statement")]
        public IEnumerable<Scope> Statement { get; set; }
    }
}