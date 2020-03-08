package com.demo.api.common;

import com.demo.BootstrapModule;
import com.demo.WebContext;
import com.demo.common.BootstrapConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TestContext {
    private static final long WAIT_APP_TIMEOUT_IN_MS = 5000;

    private final BootstrapConfig config;
    private Javalin app;
    private WebContext webContext;
    private ObjectMapper mapper;

    public TestContext(BootstrapConfig config) {
        this.config = Objects.requireNonNull(config, "Config must be provided");
    }

    public void init() {
        try {
            doInit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        try {
            doDestroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return app.port();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    private void doInit() throws Exception {
        Injector injector = Guice.createInjector(new BootstrapModule(config));

        webContext = injector.getInstance(WebContext.class);
        app = injector.getInstance(Javalin.class);
        mapper = injector.getInstance(ObjectMapper.class);

        Runtime.getRuntime().addShutdownHook(new Thread(webContext::shutdown));

        webContext.boot();
        if (!webContext.isStarted()) {
            webContext.boot();
            CompletableFuture.runAsync(() -> {
                while (!webContext.isStarted()) {
                    // wait for app to be started
                }
            }).get(WAIT_APP_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void doDestroy() throws Exception {
        if (webContext != null
                && webContext.isStarted()) {
            webContext.shutdown();
            CompletableFuture.runAsync(() -> {
                while (webContext.isStarted()) {
                    // wait for app to be stopped
                }
            }).get(WAIT_APP_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        }
    }
}
