package io.github.serg10arg.springailab.labs.chatclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// La misma API fluida, pero la respuesta llega token a token via StreamingChatModel.
@Component
@Profile("stream")
class StreamingRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    StreamingRunner(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "Explain in three short sentences what a Spring Boot starter is.";
        System.out.printf("User> %s%n%nAI> ", question);

        // Es un CLI: hay que bloquear hasta el ultimo fragmento o la JVM termina antes.
        chatClient.prompt()
                .user(question)
                .stream()
                .content()
                .doOnNext(System.out::print)
                .blockLast();

        System.out.printf("%n%n");
    }
}
