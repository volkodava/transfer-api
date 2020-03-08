package com.demo.api.common;

import com.demo.common.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApiResponse<T> {
    private final int status;
    private final Map<String, List<String>> headers;
    private final T body;
    private final ErrorResponse error;

    public ApiResponse(int status, Map<String, List<String>> headers, T body, ErrorResponse error) {
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public String getFirstHeader(String headerName) {
        Objects.requireNonNull(headerName, "Header name must be provided");

        if (headers == null
                || headers.isEmpty()) {
            return null;
        }

        List<String> values = headers.get(headerName);
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    public ErrorResponse getError() {
        return error;
    }
}
