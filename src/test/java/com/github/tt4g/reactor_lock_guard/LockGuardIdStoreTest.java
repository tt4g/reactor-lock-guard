package com.github.tt4g.reactor_lock_guard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockGuardIdStoreTest {

    private LockGuardIdStore lockGuardIdStore;

    @BeforeEach
    void setUp() {
        this.lockGuardIdStore = new LockGuardIdStore();
    }

    @Test
    void add() {
        LockGuardId lockGuardId = new LockGuardId(19);

        assertThat(this.lockGuardIdStore.add(lockGuardId)).isTrue();
        assertThat(this.lockGuardIdStore.add(lockGuardId)).isFalse();
    }

    @Test
    void remove() {
        LockGuardId lockGuardId = new LockGuardId(38);

        assertThat(this.lockGuardIdStore.remove(lockGuardId)).isFalse();

        this.lockGuardIdStore.add(lockGuardId);
        assertThat(this.lockGuardIdStore.remove(lockGuardId)).isTrue();
        assertThat(this.lockGuardIdStore.remove(lockGuardId)).isFalse();
    }

}
