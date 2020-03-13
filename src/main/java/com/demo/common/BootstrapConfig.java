package com.demo.common;

import java.util.Optional;

public class BootstrapConfig {
    private static final String DEFAULT_SWAGGER_UI_PATH = "swagger-ui";
    private static final String DEFAULT_SWAGGER_DOCS_PATH = "swagger-docs";
    private static final String DEFAULT_REDOC_PATH = "redoc";

    private final int port;
    private final int bufferSize;
    private final int maxThreads;
    private final String version;
    private final String groupId;
    private final String artifactId;
    private final String swaggerUiBaseUrl;
    private final String swaggerDocsBaseUrl;
    private final String redocBaseUrl;

    private BootstrapConfig(int port, int bufferSize, int maxThreads,
                            String version, String groupId, String artifactId,
                            String swaggerUiBaseUrl, String swaggerDocsBaseUrl, String redocBaseUrl) {
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
        this.version = Optional.ofNullable(version).orElse("");
        this.groupId = Optional.ofNullable(groupId).orElse("");
        this.artifactId = Optional.ofNullable(artifactId).orElse("");
        this.swaggerUiBaseUrl = Optional.ofNullable(swaggerUiBaseUrl).orElse(DEFAULT_SWAGGER_UI_PATH);
        this.swaggerDocsBaseUrl = Optional.ofNullable(swaggerDocsBaseUrl).orElse(DEFAULT_SWAGGER_DOCS_PATH);
        this.redocBaseUrl = Optional.ofNullable(redocBaseUrl).orElse(DEFAULT_REDOC_PATH);
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

    public String getVersion() {
        return version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
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
                ", version='" + version + '\'' +
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
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
            Package pkg = getClass().getPackage();
            String version = pkg.getImplementationVersion();
            String groupId = pkg.getName();
            String artifactId = pkg.getImplementationTitle();

            return new BootstrapConfig(port, bufferSize, maxThreads,
                    version, groupId, artifactId,
                    swaggerUiBaseUrl, swaggerDocsBaseUrl, redocBaseUrl);
        }
    }
}
