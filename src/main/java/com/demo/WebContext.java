package com.demo;

import com.demo.api.transfer.manager.TransferManager;
import com.demo.common.BootstrapConfig;
import com.demo.api.common.Routing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class WebContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebContext.class);

    @SuppressWarnings("rawtypes")
    @Inject(optional = true)
    private Set<Routing> routes = Collections.emptySet();
    private final BootstrapConfig config;
    private final Javalin app;
    private final TransferManager transferManager;
    private final AtomicBoolean started;

    @Inject
    public WebContext(BootstrapConfig config,
                      Javalin app,
                      TransferManager transferManager) {
        this.config = Objects.requireNonNull(config, "Config must be provided");
        this.app = Objects.requireNonNull(app, "App must be provided");
        this.transferManager = Objects.requireNonNull(transferManager, "Transfer manager must be provided");
        this.started = new AtomicBoolean(false);
    }

    public void boot() {
        routes.forEach(Routing::bindRoutes);
        app.events(event -> {
            event.serverStarted(() -> started.set(true));
            event.serverStopped(() -> started.set(false));
        });

        transferManager.start();
        app.start(config.getPort());

        LOGGER.info(String.format("Swagger: http://localhost:%s/%s", app.port(), config.getSwaggerUiBaseUrl()));
        LOGGER.info(String.format("ReDoc: http://localhost:%s/%s", app.port(), config.getRedocBaseUrl()));
    }

    public void shutdown() {
        transferManager.stop();
        app.stop();
    }

    public boolean isStarted() {
        return started.get();
    }
}
