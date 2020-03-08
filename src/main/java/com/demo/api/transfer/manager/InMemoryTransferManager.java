package com.demo.api.transfer.manager;

import com.demo.api.transfer.model.TransferEvent;
import com.google.inject.Singleton;

@Singleton
public class InMemoryTransferManager implements TransferManager {

    @Override
    public boolean submitEvent(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException();
    }
}
