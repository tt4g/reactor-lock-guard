package com.github.tt4g.reactor_lock_guard;

import java.util.Objects;

import reactor.core.publisher.Mono;

public class LockGuard {

    private final LockGuardIdStore lockGuardIdStore;

    public LockGuard() {
        this.lockGuardIdStore = new LockGuardIdStore();
    }

    public <T> Mono<T> run(LockGuardId id, LockGuardActionProvider<T> action) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(action);

        return action.get().transformDeferred(original -> {
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

}
