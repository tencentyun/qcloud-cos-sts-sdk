package com.tencent.cloud;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Statement {
    public String effect;
    public List<String> action = new LinkedList<String>();
    public List<String> resource = new LinkedList<String>();
    public Map<String, ConditionTypeValue> condition = null;

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public void addAction(String action) {
        this.action.add(action);
    }

    public void addActions(String[] actions) {
        for (String action:actions) {
            this.action.add(action);
        }
    }

    public void addResource(String resource) {
        this.resource.add(resource);
    }

    public void addResources(String[] resources) {
        for (String resource:resources) {
            this.resource.add(resource);
        }
    }

    public void addCondition(String type, ConditionTypeValue value) {
        if (condition == null) {
            condition = new HashMap<String, ConditionTypeValue>();
        }

        condition.put(type, value);
    }
}
