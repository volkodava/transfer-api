package com.demo.api.transfer.manager;

import com.demo.api.account.model.Account;
import com.demo.api.account.repository.AccountRepository;
import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.model.TransferState;
import com.demo.api.transfer.repository.TransferRepository;
import com.demo.api.transfer.store.EventStore;
import com.demo.api.transfer.validator.DebitTransferValidator;
import com.demo.common.BootstrapConfig;
import com.demo.common.InvalidDataException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;

@Singleton
public class InMemoryTransferManager implements TransferManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryTransferManager.class);

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final DebitTransferValidator transferValidator;
    private final EventStore<TransferEvent> eventSource;
    private PipelineExecutor pipeline;
    @Inject
    private BootstrapConfig config;

    @Inject
    public InMemoryTransferManager(TransferRepository transferRepository,
                                   AccountRepository accountRepository,
                                   DebitTransferValidator transferValidator,
                                   EventStore<TransferEvent> eventSource) {
        this.transferRepository = Objects.requireNonNull(transferRepository, "Transfer repository must be provided");
        this.accountRepository = Objects.requireNonNull(accountRepository, "Account repository must be provided");
        this.transferValidator = Objects.requireNonNull(transferValidator, "Transfer validator must be provided");
        this.eventSource = Objects.requireNonNull(eventSource, "Event source must be provided");
    }

    @Override
    public boolean submitEvent(TransferEvent event) {
        return eventSource.put(event);
    }

    @Override
    public void start() {
        pipeline = PipelineExecutor.builder()
                .withEventSource(eventSource)
                .withConfig(config)
                .withRegisterTransferFn(this::onRegisterTransfer)
                .withValidateTransferFn(this::onValidateTransfer)
                .withIsValidTransferFn(this::isValidTransfer)
                .withWithdrawSourceFn(this::onWithdrawSource)
                .withDepositTargetFn(this::onDepositTarget)
                .withCompleteTransferFn(this::onCompleteTransfer)
                .withErrorHandler(this::onError)
                .build();
        pipeline.start();
    }

    @Override
    public void stop() {
        pipeline.stop();
        eventSource.clear();
    }

    private TransferEvent onRegisterTransfer(TransferEvent event) {
        LOGGER.debug(String.format("%s --- onRegisterTransfer: %s", Thread.currentThread().getName(), event));
        TransferEvent newState = TransferEvent.builder().from(event)
                .withState(TransferState.PENDING)
                .withDetails("Transfer processing")
                .build();
        transferRepository.save(newState.getTransferId(), newState.asTransfer());
        return newState;
    }

    private TransferEvent onValidateTransfer(TransferEvent event) {
        LOGGER.debug(String.format("%s --- onValidateTransfer: %s", Thread.currentThread().getName(), event));
        TransferEvent newState;
        try {
            transferValidator.validate(event.getSourceId(), event.getTargetId(), event.getAmount());
            newState = TransferEvent.builder().from(event)
                    .withState(TransferState.VALIDATED)
                    .withDetails("Transfer is valid")
                    .build();
        } catch (InvalidDataException e) {
            newState = TransferEvent.builder().from(event)
                    .withState(TransferState.ERROR)
                    .withDetails(e.getMessage())
                    .build();
            return newState;
        }
        transferRepository.save(newState.getTransferId(), newState.asTransfer());
        return newState;
    }

    private boolean isValidTransfer(TransferEvent event) {
        return event.getState() != TransferState.ERROR;
    }

    private TransferEvent onWithdrawSource(TransferEvent event) {
        LOGGER.debug(String.format("%s --- onWithdrawSource: %s", Thread.currentThread().getName(), event));
        accountRepository.executeUpdate(event.getSourceId(), (accountId, prevAccount) -> {
            BigDecimal newBalance = prevAccount.getBalance().subtract(event.getAmount());
            return Account.builder()
                    .withId(accountId)
                    .withBalance(newBalance)
                    .build();
        });

        TransferEvent newState = TransferEvent.builder().from(event)
                .withState(TransferState.SOURCE_WITHDRAWN)
                .withDetails("Source account balance updated")
                .build();
        transferRepository.save(newState.getTransferId(), newState.asTransfer());
        return newState;
    }

    private TransferEvent onDepositTarget(TransferEvent event) {
        LOGGER.debug(String.format("%s --- onDepositTarget: %s", Thread.currentThread().getName(), event));
        accountRepository.executeUpdate(event.getTargetId(), (accountId, prevAccount) -> {
            BigDecimal newBalance = prevAccount.getBalance().add(event.getAmount());
            return Account.builder()
                    .withId(accountId)
                    .withBalance(newBalance)
                    .build();
        });

        TransferEvent newState = TransferEvent.builder().from(event)
                .withState(TransferState.TARGET_DEPOSITED)
                .withDetails("Target account balance updated")
                .build();
        transferRepository.save(newState.getTransferId(), newState.asTransfer());
        return newState;
    }

    private void onCompleteTransfer(TransferEvent event) {
        LOGGER.debug(String.format("%s --- onCompleteTransfer: %s", Thread.currentThread().getName(), event));
        TransferEvent newState;
        if (TransferState.TARGET_DEPOSITED == event.getState()) {
            newState = TransferEvent.builder().from(event)
                    .withState(TransferState.DONE)
                    .withDetails("Transfer processed")
                    .build();
        } else {
            newState = TransferEvent.builder().from(event)
                    .withState(TransferState.ERROR)
                    .withDetails(String.format("Transfer is not in a valid state: %s", event.getState()))
                    .build();
        }
        transferRepository.save(newState.getTransferId(), newState.asTransfer());
    }

    private void onError(Throwable throwable) {
        LOGGER.error("Fail to process stream of transfers", throwable);
    }
}
