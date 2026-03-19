package br.com.gopro.api.service;

import br.com.gopro.api.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitServiceTest {

    private final RateLimitService service = new RateLimitService();

    @Test
    void ensureWithinLimit_shouldBlockOnlyTheSameKey() {
        service.ensureWithinLimit("login:10.0.0.1:alice", 1, 900L);
        service.registerAttempt("login:10.0.0.1:alice", 900L);

        assertThatCode(() -> service.ensureWithinLimit("login:10.0.0.1:bob", 1, 900L))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.ensureWithinLimit("login:10.0.0.1:alice", 1, 900L))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Muitas tentativas");
    }

    @Test
    void reset_shouldAllowNewAttemptsAgain() {
        service.ensureWithinLimit("login:10.0.0.1:alice", 1, 900L);
        service.registerAttempt("login:10.0.0.1:alice", 900L);

        service.reset("login:10.0.0.1:alice");

        assertThatCode(() -> service.ensureWithinLimit("login:10.0.0.1:alice", 1, 900L))
                .doesNotThrowAnyException();
    }
}
