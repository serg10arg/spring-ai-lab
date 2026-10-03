package io.github.serg10arg.springailab.labs.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// El modelo no recuerda nada entre llamadas: cada prompt viaja solo. La memoria es un
// advisor que antepone los mensajes anteriores de la conversacion a cada pedido.
// El libro describe este advisor pero no trae ejemplo (D-017).
@Component
@Profile("memory")
class MemoryRunner implements ApplicationRunner {

    private static final String CONVERSATION_ID = "memory-demo";

    private final ChatClient withMemory;
    private final ChatClient withoutMemory;

    // ChatMemory lo autoconfigura Spring AI: MessageWindowChatMemory en memoria, 20 mensajes.
    MemoryRunner(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.withoutMemory = builder.build();
        this.withMemory = builder.clone()
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("== Sin MessageChatMemoryAdvisor");
        converse(withoutMemory);
        System.out.println("== Con MessageChatMemoryAdvisor");
        converse(withMemory);
    }

    private void converse(ChatClient chatClient) {
        ask(chatClient, "My name is Ada. Please remember it.");
        ask(chatClient, "What is my name? Answer with just the name.");
    }

    private void ask(ChatClient chatClient, String question) {
        System.out.printf("User> %s%n", question);
        String answer = chatClient.prompt()
                .user(question)
                // En 1.0.8 el advisor exige el id de conversacion en el contexto del pedido.
                // El cliente sin memoria lo ignora.
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
                .call()
                .content();
        System.out.printf("AI> %s%n%n", answer);
    }
}
