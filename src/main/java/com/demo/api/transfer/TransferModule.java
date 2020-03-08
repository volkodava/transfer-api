package com.demo.api.transfer;

import com.demo.api.transfer.controller.TransferControllerModule;
import com.demo.api.transfer.manager.TransferManagerModule;
import com.demo.api.transfer.repository.TransferRepositoryModule;
import com.demo.api.transfer.service.TransferServiceModule;
import com.demo.api.transfer.store.EventStoreModule;
import com.demo.api.transfer.validator.TransferValidatorModule;
import com.demo.common.BootstrapConfig;
import com.google.inject.AbstractModule;

import java.util.Objects;

public class TransferModule extends AbstractModule {

    private final BootstrapConfig config;

    public TransferModule(BootstrapConfig config) {
        this.config = Objects.requireNonNull(config, "Config must be provided");
    }

    protected void configure() {
        install(new TransferRepositoryModule());
        install(new TransferValidatorModule());
        install(new EventStoreModule(config));
        install(new TransferManagerModule());
        install(new TransferServiceModule());
        install(new TransferControllerModule());
    }
}
