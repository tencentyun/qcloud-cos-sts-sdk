package com.tencent.cloud.cos.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 请求调用类
 * @author robinslsun
 */
public class Request {
	protected static String requestUrl = "";
	protected static String rawResponse = "";
	protected static int connectTimeout = 5000; // ms
	protected static int readTimeout = 90000; // ms

	public static String getRequestUrl() {
		return requestUrl;
	}

	public static String getRawResponse() {
		return rawResponse;
	}

	public static String generateUrl(TreeMap<String, Object> params,
			String secretId, String secretKey, String requestMethod,
			String requestHost, String requestPath) {
		if (!params.containsKey("SecretId"))
			params.put("SecretId", secretId);

		if (!params.containsKey("Nonce"))
			params.put("Nonce",
					new Random().nextInt(Integer.MAX_VALUE));

		if (!params.containsKey("Timestamp"))
			params.put("Timestamp", System.currentTimeMillis() / 1000);

		String plainText = Sign.makeSignPlainText(params, requestMethod,
				requestHost, requestPath);

		String signatureMethod = "HmacSHA1";
		if(params.containsKey("SignatureMethod") && params.get("SignatureMethod").toString().equals("HmacSHA256"))
		{
			signatureMethod = "HmacSHA256";
		}

		try {
			params.put("Signature", Sign.sign(plainText, secretKey, signatureMethod));
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		StringBuilder url = new StringBuilder("https://");
		url.append(requestHost).append(requestPath).append("?");
		if (requestMethod.equals("GET")) {
            for ( String k : params.keySet() ) {
                try {
                    url.append(k.replace("_", "."))
                       .append("=")
                       .append(URLEncoder.encode(params.get(k).toString(), "utf-8"))
                       .append("&");
                } catch (UnsupportedEncodingException e) {
                    // 下面是一个错误的做法。
                    // 本应该抛出异常让上层捕获处理，但是出于保持兼容性的考虑，
                    // 并不想让这个方法升级后抛出一个未捕获的异常。
                    // 而且之所以会有这个异常，是因为有些特殊系统未必支持utf-8，
                    // 在这些系统上，其实根本无法正常调用云API，
                    // 所以可以直接忽略，返回一个无用的信息即可。
                    return "https://" + requestHost + requestPath;
                }
            }
		}

		return url.toString().substring(0, url.length() - 1);
	}

	public static String send(TreeMap<String, Object> params, String secretId,
			String secretKey, String requestMethod, String requestHost,
			String requestPath) {
		if (!params.containsKey("SecretId"))
			params.put("SecretId", secretId);

		if (!params.containsKey("Nonce"))
			params.put("Nonce",
					new Random().nextInt(Integer.MAX_VALUE));

		if (!params.containsKey("Timestamp"))
			params.put("Timestamp", System.currentTimeMillis() / 1000);

		params.remove("Signature");
		String plainText = Sign.makeSignPlainText(params, requestMethod,
				requestHost, requestPath);

		String signatureMethod = "HmacSHA1";
		if(params.containsKey("SignatureMethod") && params.get("SignatureMethod").toString().equals("HmacSHA256"))
		{
			signatureMethod = "HmacSHA256";
		}

		try {
			params.put("Signature", Sign.sign(plainText, secretKey, signatureMethod));
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		String url = "https://" + requestHost + requestPath;

		return sendRequest(url, params, requestMethod);
	}

	public static String sendRequest(String url,
			Map<String, Object> requestParams, String requestMethod) {
		String result = "";
		BufferedReader in = null;
		String paramStr = "";

		for (String key : requestParams.keySet()) {
			if (!paramStr.isEmpty()) {
				paramStr += '&';
			}
			try {
				paramStr += key + '='
						+ URLEncoder.encode(requestParams.get(key).toString(),"utf-8");
			} catch (UnsupportedEncodingException e) {
				result = "{\"code\":-2300,\"location\":\"com.qcloud.Common.Request:129\",\"message\":\"api sdk throw exception! "
						+ e.toString().replace("\"", "\\\"") + "\"}";
			}
		}

		try {

			if (requestMethod.equals("GET")) {
				if (url.indexOf('?') > 0) {
					url += '&' + paramStr;
				} else {
					url += '?' + paramStr;
				}
			}
			requestUrl = url;
		
			URL realUrl = new URL(url);
			URLConnection connection = null;
			if (url.toLowerCase().startsWith("https")) {
				HttpsURLConnection httpsConn = (HttpsURLConnection) realUrl
						.openConnection();

				/*httpsConn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});*/
				connection = httpsConn;
			} else {
				connection = realUrl.openConnection();
			}

			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 设置链接主机超时时间
			connection.setConnectTimeout(connectTimeout);
			connection.setReadTimeout(readTimeout);

			if (requestMethod.equals("POST")) {
				((HttpURLConnection) connection).setRequestMethod("POST");
				// 发送POST请求必须设置如下两行
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				OutputStream out = new DataOutputStream(
						connection.getOutputStream());
				out.write(paramStr.getBytes());
				out.flush();
				out.close();
			}

			// 建立实际的连接
			connection.connect();

			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}

		} catch (Exception e) {
			result = "{\"code\":-2700,\"location\":\"com.qcloud.Common.Request:225\",\"message\":\"api sdk throw exception! "
					+ e.toString().replace("\"", "\\\"") + "\"}";
		} finally {
			// 使用finally块来关闭输入流
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				result = "{\"code\":-2800,\"location\":\"com.qcloud.Common.Request:234\",\"message\":\"api sdk throw exception! "
						+ e2.toString().replace("\"", "\\\"") + "\"}";
			}
		}
		rawResponse = result;
		return result;
	}
}
