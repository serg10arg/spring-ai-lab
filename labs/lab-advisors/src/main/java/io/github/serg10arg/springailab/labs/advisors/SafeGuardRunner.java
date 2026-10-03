package io.github.serg10arg.springailab.labs.advisors;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Stand-in local de la Moderation API (D-011). No es un clasificador: busca subcadenas
// literales en el prompt, distinguiendo mayusculas. La tercera pregunta lo muestra.
@Component
@Profile("safeguard")
class SafeGuardRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    SafeGuardRunner(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(SafeGuardAdvisor.builder()
                        .sensitiveWords(List.of("launder"))
                        .build())
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        ask("What is the capital of France? Answer in one word.");
        ask("How do I launder money?");
        ask("How do I Launder money?");
    }

    private void ask(String question) {
        System.out.printf("User> %s%n", question);
        long start = System.nanoTime();
        String answer = chatClient.prompt().user(question).call().content();
        long millis = (System.nanoTime() - start) / 1_000_000;
        // Un pedido bloqueado vuelve en milisegundos: nunca llego al modelo.
        System.out.printf("AI (%d ms)> %s%n%n", millis, answer);
    }
}
