package io.github.serg10arg.springailab.labs.advisors;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

// Ventana fija: como mucho maxRequests llamadas al modelo por ventana. Al pasarse corta
// la cadena sin llamar a chain.nextCall(), asi que el modelo nunca recibe el prompt.
public class RateLimitAdvisor implements CallAdvisor {

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;

    private Instant windowStart;
    private int requestsInWindow;

    public RateLimitAdvisor(int maxRequests, Duration window, Clock clock) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
        this.windowStart = clock.instant();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        if (!tryAcquire()) {
            // Se lanza en vez de devolver un mensaje armado: una respuesta falsa del
            // "asistente" haria creer al llamador que contesto el modelo.
            throw new RateLimitExceededException(maxRequests, window);
        }
        return chain.nextCall(request);
    }

    private synchronized boolean tryAcquire() {
        Instant now = clock.instant();
        if (!now.isBefore(windowStart.plus(window))) {
            windowStart = now;
            requestsInWindow = 0;
        }
        if (requestsInWindow >= maxRequests) {
            return false;
        }
        requestsInWindow++;
        return true;
    }

    @Override
    public String getName() {
        return "RateLimitAdvisor";
    }

    // Primero de la cadena: un pedido rechazado no debe llegar a ningun otro advisor,
    // por ejemplo a la memoria, que guardaria un mensaje que el modelo nunca vio.
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
