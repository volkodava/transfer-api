package com.demo.api.transfer.validator;

import com.google.inject.AbstractModule;

public class TransferValidatorModule extends AbstractModule {

    protected void configure() {
        bind(TransferValidator.class).to(DebitTransferValidator.class);
    }
}
