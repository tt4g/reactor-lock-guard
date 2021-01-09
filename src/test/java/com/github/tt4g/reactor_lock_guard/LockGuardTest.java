package com.github.tt4g.reactor_lock_guard;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MockitoSettings
class LockGuardTest {

    private LockGuard lockGuard;

    @Mock
    private LockGuardAction<String> lockGuardAction;

    @BeforeEach
    void setUp() {
        this.lockGuard = new LockGuard();

        BDDMockito.given(this.lockGuardAction.execute())
            .willReturn(Mono.just("foo"));
    }

    @Test
    void runWhenNoOtherActionThenReturnMono() throws IdConflictException {
        LockGuardId lockGuardId = new LockGuardId(96);

        String actual =
            this.lockGuard.run(lockGuardId, this.lockGuardAction)
                .block(Duration.ofMillis(100));

        assertThat(actual).isEqualTo("foo");
    }

    @Test
    void runWhenConflictIdThenThrowException() {
        LockGuardId lockGuardId = new LockGuardId(356);

        assertThatNoException()
            .isThrownBy(() ->
                this.lockGuard.run(lockGuardId, this.lockGuardAction));
        assertThatThrownBy(() ->
            this.lockGuard.run(lockGuardId, this.lockGuardAction))
            .isExactlyInstanceOf(IdConflictException.class)
            .hasMessage("The LockGuardId is conflict. id=LockGuardId(356)");
    }

    @Test
    void runWhenSameIdAfterRunThenNoException() throws IdConflictException {
        LockGuardId lockGuardId = new LockGuardId(128);

        this.lockGuard.run(lockGuardId, this.lockGuardAction)
            .block(Duration.ofMillis(100));
        assertThatNoException()
            .isThrownBy(() ->
                this.lockGuard.run(lockGuardId, this.lockGuardAction));
    }

}
