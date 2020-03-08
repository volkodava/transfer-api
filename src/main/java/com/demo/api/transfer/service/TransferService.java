package com.demo.api.transfer.service;


import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;
import com.demo.api.transfer.model.Transfer;

import java.math.BigDecimal;
import java.util.Collection;

public interface TransferService {
    TransferId createNew(AccountId sourceAccountId, AccountId targetAccountId, BigDecimal amount);

    Transfer findById(TransferId transferId);

    Collection<Transfer> findAll();
}
