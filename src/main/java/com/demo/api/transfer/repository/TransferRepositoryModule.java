package com.demo.api.transfer.repository;

import com.demo.api.transfer.model.TransferId;
import com.demo.api.transfer.model.Transfer;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransferRepositoryModule extends AbstractModule {

    protected void configure() {
        Map<TransferId, Transfer> transferMapStorage = new ConcurrentHashMap<>();
        bind(new TypeLiteral<Map<TransferId, Transfer>>() {
        }).annotatedWith(Names.named("transferMapStorage")).toInstance(transferMapStorage);
        bind(TransferRepository.class).to(MapTransferRepository.class);
    }
}
