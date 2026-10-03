package io.github.serg10arg.springailab.labs.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// La cadena se ordena por getOrder(), no por el orden de registro: el de menor order
// procesa el pedido primero y la respuesta ultimo, como capas de una cebolla.
@Component
@Profile("order")
class OrderRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    OrderRunner(ChatClient.Builder builder) {
        // Registrados desordenados a proposito.
        this.chatClient = builder
                .defaultAdvisors(
                        new TracingAdvisor("C", 300),
                        new TracingAdvisor("A", 100),
                        new TracingAdvisor("B", 200))
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "Reply with just the word OK.";
        System.out.printf("User> %s%n", question);
        String answer = chatClient.prompt().user(question).call().content();
        System.out.printf("AI> %s%n%n", answer);
    }
}
