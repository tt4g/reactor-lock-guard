package com.github.tt4g.reactor_lock_guard;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface LockGuardAction<T> {

    Mono<T> execute();

}
