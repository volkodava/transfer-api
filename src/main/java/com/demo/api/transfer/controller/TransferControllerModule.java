package com.demo.api.transfer.controller;

import com.demo.api.transfer.controller.validator.TransferRequestValidator;
import com.demo.common.Routing;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class TransferControllerModule extends AbstractModule {

    protected void configure() {
        bind(TransferController.class);
        Multibinder.newSetBinder(binder(), Routing.class).addBinding().to(TransferRouting.class);
    }
}
