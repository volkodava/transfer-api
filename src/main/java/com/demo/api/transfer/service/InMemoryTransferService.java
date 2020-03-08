package com.demo.api.transfer.service;

import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;
import com.demo.api.transfer.manager.TransferManager;
import com.demo.api.transfer.model.Transfer;
import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.model.TransferState;
import com.demo.api.transfer.repository.TransferRepository;
import com.demo.api.transfer.validator.TransferValidator;
import com.demo.common.TooBusyException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Singleton
public class InMemoryTransferService implements TransferService {

    private final TransferManager transferManager;
    private final TransferRepository transferRepository;
    private final TransferValidator transferValidator;

    @Inject
    public InMemoryTransferService(TransferValidator transferValidator,
                                   TransferManager transferManager,
                                   TransferRepository transferRepository) {
        this.transferValidator = Objects.requireNonNull(transferValidator, "Transfer validator must be provided");
        this.transferManager = Objects.requireNonNull(transferManager, "Transfer manager must be provided");
        this.transferRepository = Objects.requireNonNull(transferRepository, "Transfer repository must be provided");
    }

    @Override
    public TransferId createNew(AccountId sourceAccountId, AccountId targetAccountId, BigDecimal amount) {
        transferValidator.validate(sourceAccountId, targetAccountId, amount);

        TransferId transferId = TransferId.valueOf(UUID.randomUUID().toString());
        boolean submitted = transferManager.submitEvent(TransferEvent.builder()
                .withId(transferId)
                .withSourceId(sourceAccountId)
                .withTargetId(targetAccountId)
                .withAmount(amount)
                .withState(TransferState.NEW)
                .withDetails("Transfer created")
                .build());
        if (submitted) {
            return transferId;
        } else {
            // can't schedule offer execution
            throw new TooBusyException("Server is too busy, please try again later");
        }
    }

    @Override
    public Transfer findById(TransferId transferId) {
        Objects.requireNonNull(transferId, "Transfer id must be provided");

        return transferRepository.findById(transferId);
    }

    @Override
    public Collection<Transfer> findAll() {
        return transferRepository.findAll();
    }
}
