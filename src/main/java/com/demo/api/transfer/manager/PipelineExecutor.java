package com.demo.api.transfer.manager;

import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.store.EventStore;
import com.demo.common.BootstrapConfig;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class PipelineExecutor {
    private final EventStore<TransferEvent> eventSource;
    private final BootstrapConfig config;
    private final AtomicBoolean running;

    private final Function<TransferEvent, TransferEvent> registerTransferFn;
    private final Function<TransferEvent, TransferEvent> validateTransferFn;
    private final Predicate<TransferEvent> isValidTransferFn;
    private final Function<TransferEvent, TransferEvent> withdrawSourceFn;
    private final Function<TransferEvent, TransferEvent> depositTargetFn;
    private final Consumer<TransferEvent> completeTransferFn;
    private final Consumer<Throwable> errorHandler;

    private Disposable validSubscriber;
    private Disposable nonValidSubscriber;

    private PipelineExecutor(EventStore<TransferEvent> eventSource,
                             BootstrapConfig config,
                             Function<TransferEvent, TransferEvent> registerTransferFn,
                             Function<TransferEvent, TransferEvent> validateTransferFn,
                             Predicate<TransferEvent> isValidTransferFn,
                             Function<TransferEvent, TransferEvent> withdrawSourceFn,
                             Function<TransferEvent, TransferEvent> depositTargetFn,
                             Consumer<TransferEvent> completeTransferFn,
                             Consumer<Throwable> errorHandler) {
        this.eventSource = Objects.requireNonNull(eventSource, "Event source must be provided");
        this.config = Objects.requireNonNull(config, "Config must be provided");
        this.registerTransferFn = Objects.requireNonNull(registerTransferFn, "Register transfer function must be provided");
        this.validateTransferFn = Objects.requireNonNull(validateTransferFn, "Validate transfer function must be provided");
        this.isValidTransferFn = Objects.requireNonNull(isValidTransferFn, "IsValid transfer function must be provided");
        this.withdrawSourceFn = Objects.requireNonNull(withdrawSourceFn, "Withdraw source function must be provided");
        this.depositTargetFn = Objects.requireNonNull(depositTargetFn, "Deposit target function must be provided");
        this.completeTransferFn = Objects.requireNonNull(completeTransferFn, "Complete transfer function must be provided");
        this.errorHandler = Objects.requireNonNull(errorHandler, "Error handler must be provided");
        this.running = new AtomicBoolean(false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start() {
        running.set(true);
        AtomicInteger partitioner = new AtomicInteger(0);
        Predicate<TransferEvent> isNonValidTransferFn = isValidTransferFn.negate();
        // withdraw in single thread
        ConnectableFlowable<TransferEvent> flow = createFlow()
                .onErrorResumeNext(this::onError)
                .subscribeOn(Schedulers.single()) // withdraw in single thread
                .map(registerTransferFn::apply)
                .map(validateTransferFn::apply)
                .publish();
        validSubscriber = flow.filter(isValidTransferFn::test)
                .map(withdrawSourceFn::apply)
                .groupBy(event -> partitioner.updateAndGet(i -> Math.max(i + 1, 0)) % config.getMaxThreads())
                .flatMap(grp -> grp.observeOn(Schedulers.io())
                        .map(depositTargetFn::apply)) // deposit in parallel
                .subscribe(completeTransferFn::accept);
        nonValidSubscriber = flow.filter(isNonValidTransferFn::test)
                .subscribe(completeTransferFn::accept);

        flow.connect();
    }

    public void stop() {
        running.set(false);
        validSubscriber.dispose();
        nonValidSubscriber.dispose();
    }

    private Publisher<TransferEvent> onError(Throwable throwable) {
        errorHandler.accept(throwable);
        return Flowable.empty();
    }

    private Flowable<TransferEvent> createFlow() {
        return Flowable.generate(emitter -> {
            Optional<TransferEvent> event = Optional.empty();
            while (!Thread.currentThread().isInterrupted()
                    && running.get()
                    && event.isEmpty()) {
                event = eventSource.take();
                event.ifPresent(emitter::onNext);
            }

            if (!running.get()) {
                emitter.onComplete();
            }
        });
    }

    public static final class Builder {
        private EventStore<TransferEvent> eventSource;
        private BootstrapConfig config;
        private Function<TransferEvent, TransferEvent> registerTransferFn;
        private Function<TransferEvent, TransferEvent> validateTransferFn;
        private Predicate<TransferEvent> isValidTransferFn;
        private Function<TransferEvent, TransferEvent> withdrawSourceFn;
        private Function<TransferEvent, TransferEvent> depositTargetFn;
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

        public Builder withIsValidTransferFn(Predicate<TransferEvent> isValidTransferFn) {
            this.isValidTransferFn = isValidTransferFn;
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

        public Builder withCompleteTransferFn(Consumer<TransferEvent> completeTransferFn) {
            this.completeTransferFn = completeTransferFn;
            return this;
        }

        public Builder withErrorHandler(Consumer<Throwable> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public PipelineExecutor build() {
            return new PipelineExecutor(eventSource, config, registerTransferFn, validateTransferFn, isValidTransferFn, withdrawSourceFn,
                    depositTargetFn, completeTransferFn, errorHandler);
        }
    }
}
