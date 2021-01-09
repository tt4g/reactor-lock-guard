package com.github.tt4g.reactor_lock_guard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockGuardIdGeneratorTest {

    private LockGuardIdGenerator lockGuardIdGenerator;

    @BeforeEach
    void setUp() {
        this.lockGuardIdGenerator = new LockGuardIdGenerator();
    }

    @Test
    void generate() {
        assertThat(this.lockGuardIdGenerator.generate())
            .isEqualTo(new LockGuardId(0));
        assertThat(this.lockGuardIdGenerator.generate())
            .isEqualTo(new LockGuardId(1));
        assertThat(this.lockGuardIdGenerator.generate())
            .isEqualTo(new LockGuardId(2));
    }

}
