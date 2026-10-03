package io.github.serg10arg.springailab.labs.chatclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// El ejemplo del capitulo 1, ahora con el rol del mensaje explicito.
@Component
@Profile("simple")
class SimplePromptRunner implements ApplicationRunner {

    private final ChatClient chatClient;

    SimplePromptRunner(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "In a single word, 'what is the answer to life, the universe, and everything?'";
        System.out.printf("User> %s%n%n", question);

        String answer = chatClient.prompt()
                .user(user -> user.text(question))
                .call()
                .content();

        System.out.printf("AI> %s%n%n", answer);
    }
}
