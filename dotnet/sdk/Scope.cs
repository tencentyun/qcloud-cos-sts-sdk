using Newtonsoft.Json;

namespace COSSTS
{
    /// <summary>
    /// 审查项
    /// </summary>
    public struct Scope
    {
        /// <summary>
        /// 允许
        /// </summary>
        private const string Allow = "allow";

        /// <summary>
        /// 禁止
        /// </summary>
        private const string Deny = "deny";

        /// <summary>
        /// 动作
        /// </summary>
        [JsonProperty("action")]
        public readonly string Action;

        /// <summary>
        ///
        /// </summary>
        [JsonProperty("effect")]
        public readonly string Effect;

        /// <summary>
        /// 资源
        /// </summary>
        [JsonProperty("resource")]
        public readonly string Resource;

        /// <summary>
        /// 审查项
        /// </summary>
        /// <param name="action"></param>
        /// <param name="resource"></param>
        public Scope(string action, string resource)
        {
            Action = action;
            Effect = Allow;
            Resource = resource;
        }

        /// <summary>
        /// 审查项
        /// </summary>
        /// <param name="action"></param>
        /// <param name="effect"><see cref="Allow"/> <see cref="Deny"/></param>
        /// <param name="resource"></param>
        public Scope(string action, string effect, string resource)
        {
            Action = action;
            Effect = effect;
            Resource = resource;
        }
    }
}