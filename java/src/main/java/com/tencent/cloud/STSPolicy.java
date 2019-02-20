package com.tencent.cloud;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class STSPolicy {
	
	private List<Scope> scopes = new ArrayList<Scope>();
	
	public STSPolicy() {
		
	}
	
	public void addScope(List<Scope> scopes) {
		if(scopes != null) {
			for(Scope scope : scopes) {
				this.scopes.add(scope);
			}
		}
	}
	
	public void addScope(Scope scope) {
		this.scopes.add(scope);
	}
	
	private JSONObject createElement(List<Scope> scopes) {
		JSONObject element = new JSONObject();
		
		JSONArray actions = new JSONArray();
		JSONArray resources = new JSONArray();
		for(Scope scope : scopes) {
			actions.put(scope.getAction());
			resources.put(scope.getResource());
		}
		element.put("action", actions);
		
		element.put("effect", "allow");
		
		JSONObject principal = new JSONObject();
		JSONArray qcs = new JSONArray();
		qcs.put("*");
		principal.put("qcs", qcs);
		element.put("principal", principal);
		
		element.put("resource", resources);
		
		return element;
	}
	
	@Override
	public String toString() {
		JSONObject policy = new JSONObject();
    	policy.put("version", "2.0");
    	JSONArray statement = new JSONArray();
    	if(scopes.size() > 0) {
    		statement.put(createElement(scopes));
    		policy.put("statement", statement);
    	}
    	return policy.toString();
	}
}
