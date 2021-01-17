package com.github.tt4g.reactor_lock_guard;

import java.time.Duration;
import java.util.function.Supplier;

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
    private LockGuardActionProvider<String> lockGuardActionProvider;

    @BeforeEach
    void setUp() {
        this.lockGuard = new LockGuard();

        BDDMockito.given(this.lockGuardActionProvider.get())
            .willReturn(Mono.just("foo"));
    }

    @Test
    void runWhenNoConflictActionThenReturnValue() {
        LockGuardId lockGuardId = new LockGuardId(96);

        String actual =
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .block(Duration.ofMillis(100));

        assertThat(actual).isEqualTo("foo");
    }

    @Test
    void runWhenOtherMonoConflictIdThenThrowException(
        @Mock LockGuardActionProvider<String> conflictLockGuardActionProvider,
        @Mock Supplier<String> stringSupplier) {

        LockGuardId lockGuardId = new LockGuardId(356);

        Mono<String> deferMono = Mono.defer(() -> Mono.just(stringSupplier.get()));
        BDDMockito.given(conflictLockGuardActionProvider.get())
            .willReturn(deferMono);

        Mono<String> conflictMono =
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .map(first ->
                    this.lockGuard.run(
                        lockGuardId,
                        conflictLockGuardActionProvider)
                        .block(Duration.ofMillis(100))
                );

        assertThatThrownBy(() -> conflictMono.block(Duration.ofMillis(100)))
            .hasRootCauseExactlyInstanceOf(IdConflictException.class)
            .hasRootCauseMessage("The LockGuardId is conflict. id=LockGuardId(356)");

        BDDMockito.then(conflictLockGuardActionProvider)
            .should()
            .get();
        BDDMockito.then(stringSupplier)
            .shouldHaveNoInteractions();
    }

    @Test
    void runWhenOtherMonoConflictIdButNotRunAtSameTimeThenReturnValue(
        @Mock LockGuardActionProvider<String> conflictLockGuardActionProvider,
        @Mock Supplier<String> stringSupplier) {

        LockGuardId lockGuardId = new LockGuardId(356);

        BDDMockito.given(conflictLockGuardActionProvider.get())
            .willReturn(Mono.just("bar"));

        String actual =
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .map(first -> {
                    this.lockGuard.run(
                        lockGuardId,
                        conflictLockGuardActionProvider);

                    return first;
                })
                .block(Duration.ofMillis(100));

        assertThat(actual).isEqualTo("foo");

        BDDMockito.then(conflictLockGuardActionProvider)
            .should()
            .get();
        BDDMockito.then(stringSupplier)
            .shouldHaveNoInteractions();
    }

    @Test
    void runWhenOtherMonoSameIdAfterRunThenNoException(
        @Mock LockGuardActionProvider<String> sameIdLockGuardActionProvider) {

        LockGuardId lockGuardId = new LockGuardId(96);

        BDDMockito.given(sameIdLockGuardActionProvider.get())
            .willReturn(Mono.just("bar"));

        String actual1 =
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .block(Duration.ofMillis(100));
        String actual2 =
            this.lockGuard.run(lockGuardId, sameIdLockGuardActionProvider)
                .block(Duration.ofMillis(100));

        assertThat(actual1).isEqualTo("foo");
        assertThat(actual2).isEqualTo("bar");
    }

    @Test
    void runWhenSameMonoMultiplySubscribeThenNoException() throws IdConflictException {
        LockGuardId lockGuardId = new LockGuardId(128);

        this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
            .block(Duration.ofMillis(100));

        assertThatNoException()
            .isThrownBy(() ->
                this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                    .block(Duration.ofMillis(100)));
    }

    @Test
    void runWhenOtherMonoConflictIdAndBeforeMonoIsErrorThenNoException(
        @Mock LockGuardActionProvider<String> sameIdLockGuardActionProvider) {

        LockGuardId lockGuardId = new LockGuardId(96);

        BDDMockito.given(this.lockGuardActionProvider.get())
            .willReturn(Mono.error(new RuntimeException("before error mono")));
        BDDMockito.given(sameIdLockGuardActionProvider.get())
            .willReturn(Mono.just("qux"));

        assertThatThrownBy(() ->
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .block(Duration.ofMillis(100)))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessage("before error mono");

        String actual2 =
            this.lockGuard.run(lockGuardId, sameIdLockGuardActionProvider)
                .block(Duration.ofMillis(100));

        assertThat(actual2).isEqualTo("qux");
    }

    @Test
    void runWhenOtherMonoConflictIdAndBeforeMonoOperatorThrowErrorThenNoException(
        @Mock LockGuardActionProvider<String> sameIdLockGuardActionProvider) {

        LockGuardId lockGuardId = new LockGuardId(96);

        BDDMockito.given(sameIdLockGuardActionProvider.get())
            .willReturn(Mono.just("qux"));

        assertThatThrownBy(() ->
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .map(ignore -> {
                    throw new RuntimeException("map error");
                })
                .block(Duration.ofMillis(100)))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessage("map error");

        String actual2 =
            this.lockGuard.run(lockGuardId, sameIdLockGuardActionProvider)
                .block(Duration.ofMillis(100));

        assertThat(actual2).isEqualTo("qux");
    }

    @Test
    void runWhenOtherMonoConflictIdAndBeforeErrorMonoOperatorThrowErrorThenNoException(
        @Mock LockGuardActionProvider<String> sameIdLockGuardActionProvider) {

        LockGuardId lockGuardId = new LockGuardId(96);

        BDDMockito.given(this.lockGuardActionProvider.get())
            .willReturn(Mono.error(new RuntimeException("before error mono")));
        BDDMockito.given(sameIdLockGuardActionProvider.get())
            .willReturn(Mono.just("qux"));

        assertThatThrownBy(() ->
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .map(ignore -> {
                    throw new RuntimeException("map error");
                })
                .block(Duration.ofMillis(100)))
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessage("before error mono");

        String actual2 =
            this.lockGuard.run(lockGuardId, sameIdLockGuardActionProvider)
                .block(Duration.ofMillis(100));

        assertThat(actual2).isEqualTo("qux");
    }

    @Test
    void runWhenSomeMonoConflictIdAndOnErrorResumeThenNoException(
        @Mock LockGuardActionProvider<String> sameIdLockGuardActionProvider1,
        @Mock LockGuardActionProvider<String> sameIdLockGuardActionProvider2) {

        LockGuardId lockGuardId = new LockGuardId(96);

        BDDMockito.given(sameIdLockGuardActionProvider1.get())
            .willReturn(Mono.just("second mono"));
        BDDMockito.given(sameIdLockGuardActionProvider2.get())
            .willReturn(Mono.just("third mono"));

        String actual1 =
            this.lockGuard.run(lockGuardId, this.lockGuardActionProvider)
                .map(ignore ->
                    this.lockGuard.run(lockGuardId, sameIdLockGuardActionProvider1)
                        .onErrorResume(
                            IdConflictException.class,
                            ex -> Mono.just("error resume second mono"))
                        .block(Duration.ofMillis(100))
                )
                .block(Duration.ofMillis(100));
        String actual2 =
            this.lockGuard.run(lockGuardId, sameIdLockGuardActionProvider2)
                .block(Duration.ofMillis(100));

        assertThat(actual1).isEqualTo("error resume second mono");
        assertThat(actual2).isEqualTo("third mono");
    }

}
