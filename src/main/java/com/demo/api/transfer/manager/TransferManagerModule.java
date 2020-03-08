package com.demo.api.transfer.manager;

import com.google.inject.AbstractModule;

public class TransferManagerModule extends AbstractModule {

    protected void configure() {
        bind(TransferManager.class).to(InMemoryTransferManager.class);
    }
}
