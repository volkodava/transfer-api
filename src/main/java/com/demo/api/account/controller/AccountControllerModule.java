package com.demo.api.account.controller;

import com.demo.api.common.Routing;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class AccountControllerModule extends AbstractModule {

    protected void configure() {
        bind(AccountController.class);
        Multibinder.newSetBinder(binder(), Routing.class).addBinding().to(AccountRouting.class);
    }
}
