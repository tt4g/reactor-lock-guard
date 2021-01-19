package com.github.tt4g.reactor_lock_guard;

import java.util.Objects;

import reactor.core.publisher.Mono;

public class LockGuard {

    private final LockGuardIdStore lockGuardIdStore;

    public LockGuard() {
        this.lockGuardIdStore = new LockGuardIdStore();
    }

    public <T> Mono<T> run(LockGuardId id, LockGuardActionProvider<T> actionProvider) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(actionProvider);

        return actionProvider.get()
            .transformDeferred(original -> {
                if (this.lockGuardIdStore.add(id)) {
                    return original
                        .doFinally(_ignore ->
                            this.lockGuardIdStore.remove(id));
                } else {
                    return Mono.error(() ->
                        new IdConflictException(id, "The LockGuardId is conflict. id=" + id));
                }
            });
    }

    public <T> TryLock<T> tryLock(LockGuardId id, LockGuardActionProvider<T> actionProvider) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(actionProvider);

        if (this.lockGuardIdStore.add(id)) {
            Mono<T> action = actionProvider.get();
            Runnable cleanUp = () -> this.lockGuardIdStore.remove(id);

            return new TryLock.Locked<>(action, cleanUp);
        } else {
            return new TryLock.NotLocked<>();
        }
    }

}
