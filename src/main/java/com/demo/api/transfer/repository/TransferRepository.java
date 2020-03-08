package com.demo.api.transfer.repository;

import com.demo.api.model.TransferId;
import com.demo.api.transfer.model.Transfer;

import java.util.Collection;
import java.util.function.BiFunction;


public interface TransferRepository {
    void save(TransferId accountId, Transfer account);

    Transfer findById(TransferId accountId);

    Transfer executeUpdate(TransferId accountId, BiFunction<TransferId, Transfer, Transfer> executor);

    Collection<Transfer> findAll();
}
