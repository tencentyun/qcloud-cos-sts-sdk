package com.tencent.cloud;

/**
 * 
 * @author bradyxiao
 * policy 格式如下
 * {
 * "version": "2.0",
 * "statement": [
 *    {
 *      "action": [
 *       "name/cos:PutBucket"
 *      ],
 *      "effect": "allow",
 *      "resource": [
 *        "qcs::cos:ap-beijing:uid/1250000000:examplebucket-1250000000/*"
 *      ]
 *   }
 *  ]
 * }
 * @since 3.0.3
 */
public class Scope {

	private static final String ALLOW = "allow";
	private static final String DENY = "deny";
	
	private String action;
	private String bucket; 
	private String region;
	private String sourcePrefix;
	private String effect = ALLOW;
	private String condition = null;
	/**
	 * 
	 * @param action 操作名称，如 "name/cos:PutObject"
	 * @param bucket 存储桶名称，格式：test-1250000000
	 * @param region 园区名称，如 ap-guangzhou
	 * @param prefix 拼接 resource 字段所需的 key 前缀，客户端 SDK 默认传固定文件名如 "dir/1.txt"，支持 * 结尾如 "dir/*, 或 *"
	 */
	public Scope(String action, String bucket, String region, String sourcePrefix) {
		this.action = action;
		this.bucket = bucket;
		this.region = region;
		this.sourcePrefix = sourcePrefix;
	}
	
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public void setResourcePrefix(String sourcePrefix) {
		this.sourcePrefix = sourcePrefix;
	}
	
	/**
	 * isAllow is true that means allow, otherwise, deny
	 * @param isAllow
	 */
	public void isAllow(boolean isAllow) {
		if(isAllow) {
			this.effect = ALLOW;
		}else {
			this.effect = DENY;
		}
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getAction() {
		if(this.action == null) throw new NullPointerException("action == null");
		return this.action;
	}
	
	public String getEffect() {
		return this.effect;
	}
	
	/**
	 * it format as follows:"qcs::cos:ap-beijing:uid/1250000000:examplebucket-1250000000/*"
	 * @return the resource of policy.
	 */
	public String getResource() {
		if(bucket == null) throw new NullPointerException("bucket == null");
		if(sourcePrefix == null) throw new NullPointerException("sourcePrefix == null");
		int index = bucket.lastIndexOf('-');
		if(index < 0) throw new IllegalStateException("bucket format is invalid: " + bucket);
		String appid = bucket.substring(index + 1).trim();
		if(!sourcePrefix.startsWith("/")) {
			sourcePrefix = '/' + sourcePrefix;
		}
		StringBuilder resource = new StringBuilder();
		resource.append("qcs::cos")
		.append(':')
		.append(region)
		.append(':')
		.append("uid/").append(appid)
		.append(':')
		.append(bucket)
		.append(sourcePrefix);
		return resource.toString();
	}
	
	public String getCondition() {
		return this.condition;
	}
	
}
