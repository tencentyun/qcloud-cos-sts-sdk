package com.tencent.cloud;

public class Response {
    public Credentials credentials = new Credentials();
    public String requestId;
    public String expiration;
    public long startTime;
    public long expiredTime;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RequestId:").append(requestId)
                .append(", TmpSecretId:").append(credentials.tmpSecretId)
                .append(", StartTime:").append(startTime)
                .append(", ExpiredTime:").append(expiredTime)
                .append(", Expiration:").append(expiration);
        return sb.toString();
    }
}
