package com.demo.api.transfer.store;

import com.demo.api.transfer.model.TransferEvent;
import com.demo.common.BootstrapConfig;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventStoreModule extends AbstractModule {

    private final BootstrapConfig config;

    public EventStoreModule(BootstrapConfig config) {
        this.config = Objects.requireNonNull(config, "Config must be provided");
    }

    protected void configure() {
        BlockingQueue<TransferEvent> eventSource = new ArrayBlockingQueue<>(config.getBufferSize());
        bind(new TypeLiteral<EventStore<TransferEvent>>() {
        }).toInstance(new EventStore<>(eventSource));
    }
}
