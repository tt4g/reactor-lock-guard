package com.github.tt4g.reactor_lock_guard;

import java.util.concurrent.atomic.AtomicInteger;

public class LockGuardIdGenerator {

    private final AtomicInteger counter;

    public LockGuardIdGenerator() {
        this.counter = new AtomicInteger(0);
    }

    LockGuardId generate() {
        int id = this.counter.getAndIncrement();

        return new LockGuardId(id);
    }

}
