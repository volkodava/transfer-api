package com.demo.api.transfer.service;

import com.google.inject.AbstractModule;

public class TransferServiceModule extends AbstractModule {

    protected void configure() {
        bind(TransferService.class).to(InMemoryTransferService.class);
    }
}
