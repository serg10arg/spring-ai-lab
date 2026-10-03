package io.github.serg10arg.springailab.labs.advisors;

import java.time.Duration;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(int maxRequests, Duration window) {
        super("Rate limit exceeded: at most %d requests per %s".formatted(maxRequests, window));
    }
}
