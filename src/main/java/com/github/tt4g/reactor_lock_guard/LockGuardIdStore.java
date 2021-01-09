package com.github.tt4g.reactor_lock_guard;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe {@link LockGuardId} store.<br>
 */
class LockGuardIdStore {

    private final Set<LockGuardId> lockGuardIds;

    public LockGuardIdStore() {
        this.lockGuardIds = ConcurrentHashMap.newKeySet();
    }

    public boolean add(LockGuardId lockGuardId) {
        Objects.requireNonNull(lockGuardId);

        return this.lockGuardIds.add(lockGuardId);
    }

    public boolean remove(LockGuardId lockGuardId) {
        Objects.requireNonNull(lockGuardId);

        return this.lockGuardIds.remove(lockGuardId);
    }

}
