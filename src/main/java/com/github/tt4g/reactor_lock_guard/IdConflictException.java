package com.github.tt4g.reactor_lock_guard;

import java.io.Serializable;
import java.util.Objects;

public class IdConflictException extends Exception implements Serializable {

    private static final long serialVersionUID = 3609045977691840557L;

    private final LockGuardId lockGuardId;

    IdConflictException(LockGuardId lockGuardId, String message) {
        super(message);

        Objects.requireNonNull(lockGuardId);

        this.lockGuardId = lockGuardId;
    }

    public LockGuardId getLockGuardId() {
        return this.lockGuardId;
    }

}
