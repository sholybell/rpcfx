package com.holybell.rpcfx.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private HttpUtil() {

    }

    public static final CloseableHttpClient httpClient;

    static {
        //当然，也可以把CloseableHttpClient定义为Bean，然后在@PreDestroy标记的方法内close这个HttpClient
        httpClient = HttpClients.custom().setMaxConnPerRoute(20).setMaxConnTotal(200).evictIdleConnections(60, TimeUnit.SECONDS).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                httpClient.close();
            } catch (IOException ignored) {
                logger.warn("关闭httpClient异常", ignored);
            }
        }));
    }

    // Get ---------------------------------------------------------------

    public static String get(String url) throws IOException {
        return get(url, null, 5000);
    }

    public static String get(String url, int timeout) throws IOException {
        return get(url, null, timeout);
    }

    public static String get(String url, Map<String, String> header) throws IOException {
        return get(url, header, 5000);
    }

    public static String get(String url, Map<String, String> header, int timeout) throws IOException {
        HttpGet httpGet = genGetEntity(url, header, timeout);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    private static HttpGet genGetEntity(String url, Map<String, String> header, int timeout) {
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
        httpGet.setConfig(requestConfig);
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return httpGet;
    }


    // Post --------------------------------------------------------------

    public static String post(String url, String requestData) throws IOException {
        return post(url, requestData, null, 5000);
    }

    public static String post(String url, List<NameValuePair> requestDataList) throws IOException {
        return post(url, requestDataList, null, 5000);
    }

    public static String post(String url, Map<String, String> requestDataMap) throws IOException {
        List<NameValuePair> params = new ArrayList<>(requestDataMap.size());
        for (Map.Entry<String, String> entry : requestDataMap.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return post(url, params, null, 5000);
    }

    public static String post(String url, String requestData, int timeout) throws IOException {
        return post(url, requestData, null, timeout);
    }

    public static String post(String url, List<NameValuePair> requestDataList, int timeout) throws IOException {
        return post(url, requestDataList, null, timeout);
    }

    public static String post(String url, String requestData, Map<String, String> header, int timeout) throws IOException {
        return post(url, requestData, header, timeout, "UTF-8");
    }

    public static String post(String url, List<NameValuePair> requestDataList, Map<String, String> header, int timeout) throws IOException {
        return post(url, requestDataList, header, timeout, "UTF-8");
    }

    public static String post(String url, String requestData, Map<String, String> header, int timeout, String charset) throws IOException {
        HttpPost httpPost = genPostEntity(url, requestData, header, timeout, charset);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), StringUtils.isNotEmpty(charset) ? Charset.forName(charset) : StandardCharsets.UTF_8);
        }
    }

    public static String post(String url, List<NameValuePair> requestDataList, Map<String, String> header, int timeout, String charset) throws IOException {
        HttpPost httpPost = genPostEntity(url, requestDataList, header, timeout, charset);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), StringUtils.isNotEmpty(charset) ? Charset.forName(charset) : StandardCharsets.UTF_8);
        }
    }

    private static HttpPost genPostEntity(String url, String requestData, Map<String, String> header, int timeout, String charset) {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
        httpPost.setConfig(requestConfig);
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity httpEntity = new StringEntity(requestData, StringUtils.isNotEmpty(charset) ? Charset.forName(charset) : StandardCharsets.UTF_8);
        httpPost.setEntity(httpEntity);
        return httpPost;
    }

    private static HttpPost genPostEntity(String url, List<NameValuePair> requestDataList, Map<String, String> header, int timeout, String charset) {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
        httpPost.setConfig(requestConfig);
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity httpEntity = new UrlEncodedFormEntity(requestDataList, StringUtils.isNotEmpty(charset) ? Charset.forName(charset) : StandardCharsets.UTF_8);
        httpPost.setEntity(httpEntity);
        return httpPost;
    }
}