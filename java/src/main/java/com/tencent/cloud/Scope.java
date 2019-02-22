package com.tencent.cloud;


public class Scope {

	private String action;
	private String bucket; 
	private String region;
	private String sourcePrefix;
	
	/**
	 * 
	 * @param action 操作名称，如 "name/cos:PutObject"
	 * @param bucket 存储桶名称，格式：test-1250000000
	 * @param region 园区名称，如 ap-guangzhou
	 * @param prefix 拼接 resource 字段所需的 key 前缀，客户端 SDK 默认传固定文件名如 "dir/1.txt"，支持 * 结尾如 "dir/*"
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
	
	public String getAction() {
		return this.action;
	}
	
	public String getResource() {
		int index = bucket.lastIndexOf('-');
		String appid = bucket.substring(index + 1).trim();
		String bucketName = bucket.substring(0, index).trim();
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
		.append("prefix//").append(appid).append('/').append(bucketName)
		.append(sourcePrefix);
		return resource.toString();
	}
	
}
