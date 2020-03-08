package com.demo.common;

import java.util.Optional;

public class BootstrapConfig {
    private static final String SWAGGER_UI_BASE_URL = "swagger-ui";
    private static final String SWAGGER_DOCS_BASE_URL = "swagger-docs";
    private static final String REDOC_BASE_URL = "redoc";

    private final int port;
    private final int bufferSize;
    private final int maxThreads;
    private final String swaggerUiBaseUrl;
    private final String swaggerDocsBaseUrl;
    private final String redocBaseUrl;

    private BootstrapConfig(int port, int bufferSize, int maxThreads, String swaggerUiBaseUrl, String swaggerDocsBaseUrl, String redocBaseUrl) {
        if (port < 0) {
            throw new IllegalArgumentException("Port number must be greater or equal to 0");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Max number of threads must be greater than 0");
        }
        this.port = port;
        this.bufferSize = bufferSize;
        this.maxThreads = maxThreads;
        this.swaggerUiBaseUrl = Optional.ofNullable(swaggerUiBaseUrl).orElse(SWAGGER_UI_BASE_URL);
        this.swaggerDocsBaseUrl = Optional.ofNullable(swaggerDocsBaseUrl).orElse(SWAGGER_DOCS_BASE_URL);
        this.redocBaseUrl = Optional.ofNullable(redocBaseUrl).orElse(REDOC_BASE_URL);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPort() {
        return port;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public String getSwaggerUiBaseUrl() {
        return swaggerUiBaseUrl;
    }

    public String getSwaggerDocsBaseUrl() {
        return swaggerDocsBaseUrl;
    }

    public String getRedocBaseUrl() {
        return redocBaseUrl;
    }

    @Override
    public String toString() {
        return "port=" + port +
                ", bufferSize=" + bufferSize +
                ", maxThreads=" + maxThreads +
                ", swaggerUiBaseUrl='" + swaggerUiBaseUrl + '\'' +
                ", swaggerDocsBaseUrl='" + swaggerDocsBaseUrl + '\'' +
                ", redocBaseUrl='" + redocBaseUrl + '\'';
    }

    public static class Builder {
        private int port;
        private int bufferSize;
        private int maxThreads;
        private String swaggerUiBaseUrl;
        private String swaggerDocsBaseUrl;
        private String redocBaseUrl;

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder withMaxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public Builder withSwaggerUiBaseUrl(String swaggerUiBaseUrl) {
            this.swaggerUiBaseUrl = swaggerUiBaseUrl;
            return this;
        }

        public Builder withSwaggerDocsBaseUrl(String swaggerDocsBaseUrl) {
            this.swaggerDocsBaseUrl = swaggerDocsBaseUrl;
            return this;
        }

        public Builder withRedocBaseUrl(String redocBaseUrl) {
            this.redocBaseUrl = redocBaseUrl;
            return this;
        }

        public BootstrapConfig build() {
            return new BootstrapConfig(port, bufferSize, maxThreads, swaggerUiBaseUrl, swaggerDocsBaseUrl, redocBaseUrl);
        }
    }
}
