package com.demo.api.transfer.model;

import com.demo.api.model.Id;

public final class TransferId extends Id {
    public TransferId(String value) {
        super(value);
    }

    public static TransferId valueOf(String val) {
        return new TransferId(val);
    }
}
