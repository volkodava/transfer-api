package com.demo.api.transfer.manager;

import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.store.EventStore;
import com.demo.common.BootstrapConfig;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PipelineExecutor {
    private final EventStore<TransferEvent> eventSource;
    private final BootstrapConfig config;
    private final AtomicBoolean running;

    private final Function<TransferEvent, TransferEvent> registerTransferFn;
    private final Function<TransferEvent, TransferEvent> validateTransferFn;
    private final Function<TransferEvent, TransferEvent> withdrawSourceFn;
    private final Function<TransferEvent, TransferEvent> depositTargetFn;
    private final Function<TransferEvent, TransferEvent> finalizeTransferFn;
    private final Consumer<TransferEvent> completeTransferFn;
    private final Consumer<Throwable> errorHandler;

    private PipelineExecutor(EventStore<TransferEvent> eventSource,
                             BootstrapConfig config,
                             Function<TransferEvent, TransferEvent> registerTransferFn,
                             Function<TransferEvent, TransferEvent> validateTransferFn,
                             Function<TransferEvent, TransferEvent> withdrawSourceFn,
                             Function<TransferEvent, TransferEvent> depositTargetFn,
                             Function<TransferEvent, TransferEvent> finalizeTransferFn,
                             Consumer<TransferEvent> completeTransferFn,
                             Consumer<Throwable> errorHandler) {
        this.eventSource = Objects.requireNonNull(eventSource, "Event source must be provided");
        this.config = Objects.requireNonNull(config, "Config must be provided");
        this.registerTransferFn = Objects.requireNonNull(registerTransferFn, "Register transfer function must be provided");
        this.validateTransferFn = Objects.requireNonNull(validateTransferFn, "Validate transfer function must be provided");
        this.withdrawSourceFn = Objects.requireNonNull(withdrawSourceFn, "Withdraw source function must be provided");
        this.depositTargetFn = Objects.requireNonNull(depositTargetFn, "Deposit target function must be provided");
        this.finalizeTransferFn = Objects.requireNonNull(finalizeTransferFn, "Finalize transfer function must be provided");
        this.completeTransferFn = Objects.requireNonNull(completeTransferFn, "Complete transfer function must be provided");
        this.errorHandler = Objects.requireNonNull(errorHandler, "Error handler must be provided");
        this.running = new AtomicBoolean(false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start() {
        throw new UnsupportedOperationException();
    }

    public void stop() {
        throw new UnsupportedOperationException();
    }

    public static final class Builder {
        private EventStore<TransferEvent> eventSource;
        private BootstrapConfig config;
        private Function<TransferEvent, TransferEvent> registerTransferFn;
        private Function<TransferEvent, TransferEvent> validateTransferFn;
        private Function<TransferEvent, TransferEvent> withdrawSourceFn;
        private Function<TransferEvent, TransferEvent> depositTargetFn;
        private Function<TransferEvent, TransferEvent> finalizeTransferFn;
        private Consumer<TransferEvent> completeTransferFn;
        private Consumer<Throwable> errorHandler;

        public Builder withEventSource(EventStore<TransferEvent> eventSource) {
            this.eventSource = eventSource;
            return this;
        }

        public Builder withConfig(BootstrapConfig config) {
            this.config = config;
            return this;
        }

        public Builder withRegisterTransferFn(Function<TransferEvent, TransferEvent> registerTransferFn) {
            this.registerTransferFn = registerTransferFn;
            return this;
        }

        public Builder withValidateTransferFn(Function<TransferEvent, TransferEvent> validateTransferFn) {
            this.validateTransferFn = validateTransferFn;
            return this;
        }

        public Builder withWithdrawSourceFn(Function<TransferEvent, TransferEvent> withdrawSourceFn) {
            this.withdrawSourceFn = withdrawSourceFn;
            return this;
        }

        public Builder withDepositTargetFn(Function<TransferEvent, TransferEvent> depositTargetFn) {
            this.depositTargetFn = depositTargetFn;
            return this;
        }

        public Builder withFinalizeTransferFn(Function<TransferEvent, TransferEvent> finalizeTransferFn) {
            this.finalizeTransferFn = finalizeTransferFn;
            return this;
        }

        public Builder withCompleteTransferFn(Consumer<TransferEvent> completeTransferFn) {
            this.completeTransferFn = completeTransferFn;
            return this;
        }

        public Builder withErrorHandler(Consumer<Throwable> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public PipelineExecutor build() {
            return new PipelineExecutor(eventSource, config, registerTransferFn, validateTransferFn, withdrawSourceFn,
                    depositTargetFn, finalizeTransferFn, completeTransferFn, errorHandler);
        }
    }
}
