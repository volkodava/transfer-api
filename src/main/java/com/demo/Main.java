package com.demo;

import com.demo.common.BootstrapConfig;
import com.demo.util.SocketUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        BootstrapConfig config = buildConfig();
        LOGGER.info(String.format("Application configuration: %s", config));

        Injector injector = Guice.createInjector(new BootstrapModule(config));
        WebContext webContext = injector.getInstance(WebContext.class);
        Runtime.getRuntime().addShutdownHook(new Thread(webContext::shutdown));

        webContext.boot();
    }

    private static BootstrapConfig buildConfig() {
        int port = getEnvAsInt("PORT").orElse(SocketUtils.findAvailablePort());
        int bufferSize = getEnvAsInt("BUFFER_SIZE").orElse(10000);
        int maxThreads = getEnvAsInt("MAX_THREADS").orElse(Runtime.getRuntime().availableProcessors());
        return BootstrapConfig.builder()
                .withPort(port)
                .withBufferSize(bufferSize)
                .withMaxThreads(maxThreads)
                .withSwaggerUiBaseUrl("swagger-ui")
                .withSwaggerDocsBaseUrl("swagger-docs")
                .withRedocBaseUrl("redoc")
                .build();
    }

    private static Optional<Integer> getEnvAsInt(String envVar) {
        String var = System.getenv(envVar);
        if (var != null
                && !var.isEmpty()) {
            return Optional.of(Integer.parseInt(var));
        }
        return Optional.empty();
    }
}