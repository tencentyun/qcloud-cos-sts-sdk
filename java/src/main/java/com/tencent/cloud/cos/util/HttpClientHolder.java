package com.tencent.cloud.cos.util;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * HttpClient 连接池持有类（单例）
 * 解决高并发场景下 HttpURLConnection 连接池过小导致的性能问题
 */
public class HttpClientHolder {

    private static volatile CloseableHttpClient httpClient;

    /**
     * 获取 HttpClient 实例（懒加载单例）
     * 超时配置复用 Request 类中的 connectTimeout 和 readTimeout
     */
    public static CloseableHttpClient getClient() {
        if (httpClient == null) {
            synchronized (HttpClientHolder.class) {
                if (httpClient == null) {
                    httpClient = createHttpClient();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            if (httpClient != null) {
                                httpClient.close();
                            }
                        } catch (Exception e) {
                            // ignore
                        }
                    }));
                }
            }
        }
        return httpClient;
    }

    private static CloseableHttpClient createHttpClient() {
        // 复用 Request 类中已有的超时配置
        int connectTimeout = Request.connectTimeout;  // 默认 5000ms
        int readTimeout = Request.readTimeout;        // 默认 90000ms

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();

        // 连接池配置：使用合理的默认值，无需额外配置
        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create().build())
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(200)      // 连接池总容量
                .setMaxConnPerRoute(50)    // 每个路由（域名）的最大连接数
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.of(60, TimeUnit.SECONDS))
                .build();
    }

    private HttpClientHolder() {
    }
}
