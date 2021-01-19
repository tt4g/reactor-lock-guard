package com.github.tt4g.reactor_lock_guard;

import java.time.Duration;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MockitoSettings
class TryLockTest {

    @Nested
    class LockedTest {

        private Mono<String> action;

        @Mock
        private Runnable cleanUp;

        private TryLock.Locked<String> locked;

        @BeforeEach
        void setUp() {
            this.action = Mono.just("foo");

            this.locked = new TryLock.Locked<>(this.action, this.cleanUp);
        }

        @Test
        public void cleanUpDoesNotRunAfterInitialized() {
            BDDMockito.then(this.cleanUp)
                .shouldHaveNoInteractions();
        }

        @Test
        public void isCleanedAfterInitialized() {
            assertThat(this.locked.isCleaned()).isFalse();
        }

        @Test
        public void isSubscribedAfterInitialized() {
            assertThat(this.locked.isSubscribed()).isFalse();
        }

        @Test
        public void isLocked() {
            assertThat(this.locked.isLocked()).isTrue();
        }

        @Test
        public void cleanUpWhenCallClose() {
            this.locked.close();

            assertThat(this.locked.isCleaned()).isTrue();
            assertThat(this.locked.isSubscribed()).isFalse();

            BDDMockito.then(this.cleanUp)
                .should()
                .run();
        }

        @Test
    	@SuppressWarnings("try")
        public void autoCleanUpWithTryWithResources() {
            try (TryLock<String> autoCleanUpLocked = this.locked) {

            }

            assertThat(this.locked.isCleaned()).isTrue();
            assertThat(this.locked.isSubscribed()).isFalse();

            BDDMockito.then(this.cleanUp)
                .should()
                .run();
        }

        @Test
        public void cleanUpWhenMonoSubscribed() {
            assertThat(this.locked.get().block(Duration.ofMillis(10)))
                .isEqualTo("foo");

            assertThat(this.locked.isCleaned()).isTrue();
            assertThat(this.locked.isSubscribed()).isTrue();

            BDDMockito.then(this.cleanUp)
                .should()
                .run();
        }

        @Test
        public void cleanUpOnlyOnce() {
            this.locked.close();
            this.locked.close();

            this.locked.get().block(Duration.ofMillis(10));
            this.locked.get().block(Duration.ofMillis(10));

            BDDMockito.then(this.cleanUp)
                .should()
                .run();
        }

        @Test
        public void cleanUpDoesNotCallAfterCallClose() {
            this.locked.close();

            BDDMockito.then(this.cleanUp)
                .should()
                .run();

            BDDMockito.then(this.cleanUp)
                .shouldHaveNoMoreInteractions();

            this.locked.close();
            this.locked.get().block(Duration.ofMillis(10));
        }

        @Test
        public void cleanUpDoesNotCallAfterMonoSubscribed() {
            this.locked.get().block(Duration.ofMillis(10));

            BDDMockito.then(this.cleanUp)
                .should()
                .run();

            BDDMockito.then(this.cleanUp)
                .shouldHaveNoMoreInteractions();

            this.locked.close();
            this.locked.get().block(Duration.ofMillis(10));
        }

    }

    @Nested
    class NotLockedTest {

        private TryLock.NotLocked<String> notLocked;

        @BeforeEach
        void setUp() {
            this.notLocked = new TryLock.NotLocked<>();
        }

        @Test
        public void isLocked() {
            assertThat(this.notLocked.isLocked()).isFalse();
        }

        @Test
        public void getThenThrowNoSuchElementException() {
            assertThatThrownBy(() -> this.notLocked.get())
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("NotLocked");
        }

    }


}
