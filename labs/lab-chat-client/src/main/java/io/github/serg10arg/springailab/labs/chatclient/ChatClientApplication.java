package io.github.serg10arg.springailab.labs.chatclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatClientApplication {

    public static void main(String[] args) {
        // Es un CLI, como lab-getting-started: sin NONE, spring-webflux en el classpath
        // hace que Boot deduzca una app REACTIVE y falle al arrancar.
        new SpringApplicationBuilder(ChatClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    // Spring AI autoconfigura el ChatClient.Builder (prototype), no el ChatClient: una app
    // puede necesitar varios clientes con defaults distintos sobre el mismo ChatModel.
    // Cada builder llega con todos los ChatClientCustomizer del contexto ya aplicados.
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    // Configuracion programatica: estas opciones pisan las de application.yml propiedad
    // por propiedad. El modelo no se toca aca, asi que sigue saliendo del yml.
    @Bean
    ChatClientCustomizer deterministicOptions() {
        return builder -> builder.defaultOptions(ChatOptions.builder()
                .temperature(0.0)
                .build());
    }
}
