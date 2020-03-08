package com.demo.api.transfer.controller;

import com.demo.common.Routing;
import io.javalin.Javalin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static io.javalin.apibuilder.ApiBuilder.*;

@Singleton
class TransferRouting extends Routing<TransferController> {
    private final Javalin app;

    @Inject
    public TransferRouting(Javalin app) {
        this.app = Objects.requireNonNull(app, "App must be provided");
    }

    @Override
    public void bindRoutes() {
        app.routes(() -> path("transfers", () -> {
            get(ctx -> getController().findAllTransfers(ctx));
            post(ctx -> getController().createNewTransfer(ctx));
            path(":transferId", () -> get(ctx -> getController().findTransferById(ctx)));
        }));
    }
}
