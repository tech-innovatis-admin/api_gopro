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
        Instant now = Instant.now();
        Instant threshold = now.minusSeconds(windowSeconds);
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
                attempts.pollFirst();
            }

            if (attempts.size() >= maxAttempts) {
                throw new TooManyRequestsException("Muitas tentativas. Tente novamente em alguns minutos.");
            }

            attempts.addLast(now);
        }
    }
}
