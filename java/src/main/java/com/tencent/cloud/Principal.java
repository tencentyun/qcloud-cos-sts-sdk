package com.tencent.cloud;

import java.util.LinkedList;
import java.util.List;

public class Principal {
    public List<String> qcs = new LinkedList<String>();

    public void addQCSString(String qcs) {
        this.qcs.add(qcs);
    }
}
