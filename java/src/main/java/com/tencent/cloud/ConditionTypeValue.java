package com.tencent.cloud;

import java.util.LinkedList;
import java.util.List;

@Deprecated
public class ConditionTypeValue {
    public String key;
    //cam接口不支持valueList关键字
    public List<String> valueList = new LinkedList<String>();

    public void setKey(String key) {
        this.key = key;
    }

    public void addValue(String value) {
        this.valueList.add(value);
    }
}
