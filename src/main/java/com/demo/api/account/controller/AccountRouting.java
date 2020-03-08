package com.demo.api.account.controller;

import com.demo.api.common.Routing;
import io.javalin.Javalin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static io.javalin.apibuilder.ApiBuilder.*;

@Singleton
class AccountRouting extends Routing<AccountController> {
    private final Javalin app;

    @Inject
    public AccountRouting(Javalin app) {
        this.app = Objects.requireNonNull(app, "App must be provided");
    }

    @Override
    public void bindRoutes() {
        app.routes(() -> path("accounts", () -> {
            post(ctx -> getController().createNewAccount(ctx));
            path(":accountId", () -> get(ctx -> getController().findAccountById(ctx)));
        }));
    }
}
