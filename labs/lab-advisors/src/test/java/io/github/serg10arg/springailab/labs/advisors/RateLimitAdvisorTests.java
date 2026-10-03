package io.github.serg10arg.springailab.labs.advisors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

class RateLimitAdvisorTests {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-03T12:00:00Z"));
    private final CallAdvisorChain chain = mock(CallAdvisorChain.class);
    private final ChatClientRequest request = ChatClientRequest.builder()
            .prompt(new Prompt("hi"))
            .context(Map.of())
            .build();
    private final ChatClientResponse response = ChatClientResponse.builder().build();

    private RateLimitAdvisor advisor;

    @BeforeEach
    void setUp() {
        when(chain.nextCall(request)).thenReturn(response);
        advisor = new RateLimitAdvisor(2, Duration.ofMinutes(1), clock);
    }

    @Test
    void passesRequestsWithinTheLimitDownTheChain() {
        assertThat(advisor.adviseCall(request, chain)).isSameAs(response);
        assertThat(advisor.adviseCall(request, chain)).isSameAs(response);
        verify(chain, times(2)).nextCall(request);
    }

    @Test
    void rejectsOverTheLimitWithoutCallingTheChain() {
        advisor.adviseCall(request, chain);
        advisor.adviseCall(request, chain);

        assertThatThrownBy(() -> advisor.adviseCall(request, chain))
                .isInstanceOf(RateLimitExceededException.class);
        verify(chain, times(2)).nextCall(request);
    }

    @Test
    void allowsRequestsAgainOnceTheWindowHasPassed() {
        advisor.adviseCall(request, chain);
        advisor.adviseCall(request, chain);

        clock.advance(Duration.ofMinutes(1));

        assertThat(advisor.adviseCall(request, chain)).isSameAs(response);
        verify(chain, times(3)).nextCall(request);
    }

    @Test
    void zeroLimitNeverReachesTheModel() {
        RateLimitAdvisor closed = new RateLimitAdvisor(0, Duration.ofMinutes(1), clock);

        assertThatThrownBy(() -> closed.adviseCall(request, chain))
                .isInstanceOf(RateLimitExceededException.class);
        verify(chain, never()).nextCall(request);
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
