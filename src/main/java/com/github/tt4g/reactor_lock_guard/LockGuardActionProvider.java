package com.github.tt4g.reactor_lock_guard;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface LockGuardActionProvider<T> {

    Mono<T> get();

}
