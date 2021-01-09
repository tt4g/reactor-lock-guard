package com.github.tt4g.reactor_lock_guard;

import java.io.Serializable;
import java.util.Objects;

public class LockGuardId implements Serializable {

    private static final long serialVersionUID = 6264849564005641581L;

    private final int id;

    LockGuardId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LockGuardId that = (LockGuardId) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "LockGuardId(" + this.id + ")";
    }

}
