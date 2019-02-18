package com.tencent.cloud;

import java.util.ArrayList;
import java.util.List;


public class Scope {

	private List<String> action = new ArrayList<String>(); 
	private String bucket; 
	private String region;
	private List<String> sourcePrefix = new ArrayList<String>(); 
	
	public Scope() {}
	
	/**
	 * 
	 * @param action 操作名称，如 "name/cos:PutObject"
	 * @param bucket 存储桶名称，格式：test-1250000000
	 * @param region 园区名称，如 ap-guangzhou
	 * @param prefix 拼接 resource 字段所需的 key 前缀，客户端 SDK 默认传固定文件名如 "dir/1.txt"，支持 * 结尾如 "dir/*"
	 */
	public Scope(String action, String bucket, String region, String sourcePrefix) {
		this.action.add(action);
		this.bucket = bucket;
		this.region = region;
		this.sourcePrefix.add(sourcePrefix);
	}
	
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public void addAction(String action) {
		this.action.add(action);
	}
	
	public void addResourcePrefix(String sourcePrefix) {
		this.sourcePrefix.add(sourcePrefix);
	}
	
	public List<String> getAction() {
		return action;
	}
	
	public List<String> getResourcefix() {
		return sourcePrefix;
	}
	
	public String getBucket() {
		return bucket;
	}
	
	public String getRegion() {
		return region;
	}
	
}
