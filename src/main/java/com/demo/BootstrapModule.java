package com.demo;

import com.demo.api.account.AccountModule;
import com.demo.api.common.ErrorResponse;
import com.demo.api.exception.InvalidDataException;
import com.demo.api.exception.NotFoundException;
import com.demo.api.exception.TooBusyException;
import com.demo.api.transfer.TransferModule;
import com.demo.common.BootstrapConfig;
import com.google.inject.AbstractModule;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import org.eclipse.jetty.http.HttpStatus;

import java.util.Objects;

public class BootstrapModule extends AbstractModule {

    private final BootstrapConfig config;

    public BootstrapModule(BootstrapConfig config) {
        this.config = Objects.requireNonNull(config, "Config must be provided");
    }

    protected void configure() {
        bind(BootstrapConfig.class).toInstance(config);
        bind(Javalin.class).toInstance(createApp());
        install(new AccountModule());
        install(new TransferModule(config));
        bind(WebContext.class);
    }

    private Javalin createApp() {
        return Javalin.create(config -> {
            config.registerPlugin(getConfiguredOpenApiPlugin());
            config.showJavalinBanner = false;
            config.enableCorsForAllOrigins();
            config.defaultContentType = "application/json";
        }).exception(HttpResponseException.class, (exception, ctx) -> {
            ctx.json(new ErrorResponse(exception.getMessage()));
            ctx.status(exception.getStatus());
        }).exception(NotFoundException.class, (exception, ctx) -> {
            ctx.json(new ErrorResponse(exception.getMessage()));
            ctx.status(HttpStatus.NOT_FOUND_404);
        }).exception(InvalidDataException.class, (exception, ctx) -> {
            ctx.json(new ErrorResponse(exception.getMessage()));
            ctx.status(HttpStatus.BAD_REQUEST_400);
        }).exception(TooBusyException.class, (exception, ctx) -> {
            ctx.json(new ErrorResponse(exception.getMessage()));
            ctx.status(HttpStatus.TOO_MANY_REQUESTS_429);
        }).exception(RuntimeException.class, (exception, ctx) -> {
            ctx.json(new ErrorResponse(exception.getMessage()));
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
        });
    }

    private OpenApiPlugin getConfiguredOpenApiPlugin() {
        Info info = new Info().version("1.0").title("Money Transfer API");
        OpenApiOptions options = new OpenApiOptions(info)
                .activateAnnotationScanningFor("com.demo")
                .path(String.format("/%s", config.getSwaggerDocsBaseUrl()))
                .swagger(new SwaggerOptions(String.format("/%s", config.getSwaggerUiBaseUrl())))
                .reDoc(new ReDocOptions(String.format("/%s", config.getRedocBaseUrl())))
                .defaultDocumentation(doc -> {
                    doc.json("500", ErrorResponse.class);
                });
        return new OpenApiPlugin(options);
    }
}
