package com.demo.api.transfer.repository;


import com.demo.api.transfer.model.TransferId;
import com.demo.api.transfer.model.Transfer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@Singleton
public class MapTransferRepository implements TransferRepository {

    private final Map<TransferId, Transfer> mapImpl;

    @Inject
    public MapTransferRepository(@Named("transferMapStorage") Map<TransferId, Transfer> mapStorage) {
        this.mapImpl = Objects.requireNonNull(mapStorage, "Map storage must be provided");
    }

    @Override
    public void save(TransferId key, Transfer value) {
        Objects.requireNonNull(key, "Key must be provided");
        Objects.requireNonNull(value, "Value must be provided");

        mapImpl.put(key, value);
    }

    @Override
    public Transfer findById(TransferId key) {
        Objects.requireNonNull(key, "Key must be provided");

        return mapImpl.get(key);
    }

    @Override
    public Transfer executeUpdate(TransferId key, BiFunction<TransferId, Transfer, Transfer> executor) {
        Objects.requireNonNull(key, "Key must be provided");
        Objects.requireNonNull(executor, "Executor must be provided");

        return mapImpl.compute(key, executor);
    }

    @Override
    public Collection<Transfer> findAll() {
        return mapImpl.values();
    }
}
