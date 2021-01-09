package com.github.tt4g.reactor_lock_guard;

import java.util.Objects;

import reactor.core.publisher.Mono;

public class LockGuard {

    private final LockGuardIdStore lockGuardIdStore;

    public LockGuard() {
        this.lockGuardIdStore = new LockGuardIdStore();
    }

    public <T> Mono<T> run(LockGuardId id, LockGuardAction<T> action) throws IdConflictException {
        Objects.requireNonNull(id);
        Objects.requireNonNull(action);

        if (!this.lockGuardIdStore.add(id)) {
            throw new IdConflictException(id, "The LockGuardId is conflict. id=" + id);
        }

        return action.execute()
            .doFinally(_ignore -> this.lockGuardIdStore.remove(id));
    }

}
