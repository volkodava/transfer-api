package com.demo.api.transfer.store;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventStore<T> {
    private final BlockingQueue<T> eventSource;

    public EventStore(BlockingQueue<T> eventSource) {
        this.eventSource = Objects.requireNonNull(eventSource, "Event source must be provided");
    }

    public boolean put(T event) {
        boolean submitted = false;
        try {
            submitted = eventSource.offer(event, 1, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // preserve interruption status
            Thread.currentThread().interrupt();
        }

        return submitted;
    }

    public Optional<T> take() {
        try {
            return Optional.ofNullable(eventSource.poll(1, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            // preserve interruption status
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }

    public void clear() {
        eventSource.clear();
    }
}
