package br.com.gopro.api.service;

import br.com.gopro.api.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public void checkRateLimit(String key, int maxAttempts, long windowSeconds) {
        ensureWithinLimit(key, maxAttempts, windowSeconds);
        registerAttempt(key, windowSeconds);
    }

    public void ensureWithinLimit(String key, int maxAttempts, long windowSeconds) {
        Instant now = Instant.now();
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            pruneExpiredAttempts(attempts, now, windowSeconds);

            if (attempts.size() >= maxAttempts) {
                throw new TooManyRequestsException("Muitas tentativas. Tente novamente em alguns minutos.");
            }
        }
    }

    public void registerAttempt(String key, long windowSeconds) {
        Instant now = Instant.now();
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            pruneExpiredAttempts(attempts, now, windowSeconds);
            attempts.addLast(now);
        }
    }

    public void reset(String key) {
        attemptsByKey.remove(key);
    }

    private void pruneExpiredAttempts(Deque<Instant> attempts, Instant now, long windowSeconds) {
        Instant threshold = now.minusSeconds(windowSeconds);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
            attempts.pollFirst();
        }
    }
}
