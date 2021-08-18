package com.tencent.cloud;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Policy {
    public String version = "2.0";
    public List<Statement> statement = new LinkedList<Statement>();
    public Principal principal = null;

    public void setVersion(String version) {
        this.version = version;
    }

    public void addStatement(Statement statement) {
        this.statement.add(statement);
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
}
