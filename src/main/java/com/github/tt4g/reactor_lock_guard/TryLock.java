package com.github.tt4g.reactor_lock_guard;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import reactor.core.publisher.Mono;

public abstract class TryLock<T> implements AutoCloseable {

    abstract boolean isLocked();

    public abstract Mono<T> get();

    @Override
    public abstract void close();

    /**
     * If subscribe {@link Mono} that returns {@link Locked#get()} then
     * run clean up action in {@link Mono#doFinally(Consumer)}.<br>
     * If don't subscribe {@link Mono} then, run clean up action in
     * {@link Locked#close()}.<br>
     *
     * @param <T>
     */
    static class Locked<T> extends TryLock<T> {

        private final AtomicBoolean subscribed;

        private final AtomicBoolean cleaned;

        private final Runnable cleanUpAction;

        private final Mono<T> action;

        Locked(Mono<T> action, Runnable cleanUpAction) {
            Objects.requireNonNull(action);
            Objects.requireNonNull(cleanUpAction);

            this.subscribed = new AtomicBoolean(false);
            this.cleaned = new AtomicBoolean(false);

            this.cleanUpAction = cleanUpAction;
            this.action =
                action
                    .doOnSubscribe(_ignore ->
                        this.subscribed.compareAndSet(false, true))
                    .doFinally(_ignore ->
                        this.runCleanUp());
        }

        @Override
        boolean isLocked() {
            return true;
        }

        @Override
        public Mono<T> get() {
            return this.action;
        }

        @Override
        public void close() {
            if (this.subscribed.get()) {
                return;
            }

            runCleanUp();
        }

        boolean isSubscribed() {
            return this.subscribed.get();
        }

        boolean isCleaned() {
            return this.cleaned.get();
        }

        private void runCleanUp() {
            if (!this.cleaned.compareAndSet(false, true)) {
                return;
            }

            this.cleanUpAction.run();
        }

    }

    static class NotLocked<T> extends TryLock<T> {

        @Override
        boolean isLocked() {
            return false;
        }

        @Override
        public Mono<T> get() {
            throw new NoSuchElementException("NotLocked");
        }

        @Override
        public void close() {

        }

    }

}
