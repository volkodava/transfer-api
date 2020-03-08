package com.demo.common;

public class BootstrapConfig {
    private final int bufferSize;
    private final int maxThreads;

    private BootstrapConfig(int bufferSize, int maxThreads) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Max number of threads must be greater than 0");
        }
        this.bufferSize = bufferSize;
        this.maxThreads = maxThreads;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public String toString() {
        return "bufferSize=" + bufferSize +
                ", maxThreads=" + maxThreads;
    }

    public static class Builder {
        private int bufferSize;
        private int maxThreads;

        public Builder withBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder withMaxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public BootstrapConfig build() {
            return new BootstrapConfig(bufferSize, maxThreads);
        }
    }
}
