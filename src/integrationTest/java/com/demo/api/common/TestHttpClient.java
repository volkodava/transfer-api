package com.demo.api.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestHttpClient.class);
    private final HttpClient client;
    private final TestContext context;

    public TestHttpClient(TestContext context) {
        this.context = Objects.requireNonNull(context, "Context must be provided");
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public <I, O> ApiResponse<O> get(String contextPath, Class<O> cls) {
        try {
            return doGet(contextPath, cls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <I, O> ApiResponse<O> post(String contextPath, I inputObj, Class<O> cls) {
        try {
            return doPost(contextPath, inputObj, cls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl(String contextPath) {
        if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }
        return String.format("http://localhost:%s/%s", context.getPort(), contextPath);
    }

    private <O> ApiResponse<O> doGet(String contextPath, Class<O> cls) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(getUrl(contextPath)))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return processRequest(response, cls);
    }

    private <I, O> ApiResponse<O> doPost(String contextPath, I inputObj, Class<O> cls) throws Exception {
        String json = context.getMapper().writeValueAsString(inputObj);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(getUrl(contextPath)))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return processRequest(response, cls);
    }

    private <O> ApiResponse<O> processRequest(HttpResponse<String> response, Class<O> cls) {
        int statusCode = response.statusCode();
        Map<String, List<String>> headers = response.headers().map();
        String bodyContent = response.body();

        if (statusCode >= 200
                && statusCode < 300) {
            O bodyObj = tryParse(bodyContent, cls);
            return new ApiResponse<>(statusCode, headers, bodyObj, null);
        } else {
            ErrorResponse error = tryParse(bodyContent, ErrorResponse.class);
            return new ApiResponse<>(statusCode, headers, null, error);
        }
    }

    private <O> O tryParse(String content, Class<O> cls) {
        if (content == null
                || content.isEmpty()) {
            return null;
        }

        try {
            return context.getMapper().readValue(content, cls);
        } catch (IOException e) {
            LOGGER.warn(String.format("Can't parse response payload as %s", cls.getCanonicalName()), e);
            return null;
        }
    }
}
