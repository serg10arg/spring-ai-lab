package io.github.serg10arg.springailab.labs.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Tres pedidos contra un limite de dos por minuto: el tercero se corta antes del modelo.
@Component
@Profile("ratelimit")
class RateLimitRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    RateLimitRunner(ChatClient.Builder builder, RateLimitAdvisor rateLimitAdvisor) {
        this.chatClient = builder
                .defaultAdvisors(rateLimitAdvisor)
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        for (int i = 1; i <= 3; i++) {
            String question = "Reply with just the number %d.".formatted(i);
            System.out.printf("User> %s%n", question);
            long start = System.nanoTime();
            try {
                String answer = chatClient.prompt().user(question).call().content();
                System.out.printf("AI (%d ms)> %s%n%n", elapsedMillis(start), answer);
            }
            catch (RateLimitExceededException ex) {
                System.out.printf("Rejected (%d ms)> %s%n%n", elapsedMillis(start), ex.getMessage());
            }
        }
    }

    private static long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
