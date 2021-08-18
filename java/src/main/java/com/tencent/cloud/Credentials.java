package com.tencent.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Credentials {
    public String tmpSecretId;
    public String tmpSecretKey;
    public String sessionToken;
    // 为了兼容，需要解析这个字段
    public String token;
}
