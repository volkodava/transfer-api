package com.demo.api.transfer;

import com.demo.api.transfer.manager.TransferManagerModule;
import com.demo.api.transfer.repository.TransferRepositoryModule;
import com.demo.api.transfer.service.TransferServiceModule;
import com.demo.api.transfer.validator.TransferValidatorModule;
import com.google.inject.AbstractModule;

public class TransferModule extends AbstractModule {

    protected void configure() {
        install(new TransferRepositoryModule());
        install(new TransferValidatorModule());
        install(new TransferManagerModule());
        install(new TransferServiceModule());
    }
}
