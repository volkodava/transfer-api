package com.demo.api.transfer.manager;

import com.demo.api.transfer.model.TransferEvent;

public interface TransferManager {
    boolean submitEvent(TransferEvent event);

    void start();

    void stop();
}
