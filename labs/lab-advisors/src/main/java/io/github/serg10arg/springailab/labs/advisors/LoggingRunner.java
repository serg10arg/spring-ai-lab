package io.github.serg10arg.springailab.labs.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// El advisor mas simple: no cambia nada, solo deja ver el pedido y la respuesta en el log.
@Component
@Profile("logging")
class LoggingRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    LoggingRunner(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "What is the capital of France? Answer in one word.";
        System.out.printf("User> %s%n", question);
        System.out.printf("AI> %s%n%n", chatClient.prompt().user(question).call().content());
    }
}
