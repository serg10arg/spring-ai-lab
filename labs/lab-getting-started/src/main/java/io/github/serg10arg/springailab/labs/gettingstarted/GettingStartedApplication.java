package io.github.serg10arg.springailab.labs.gettingstarted;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GettingStartedApplication {

    public static void main(String[] args) {
        // Es un CLI: sin contexto web, la JVM termina sola cuando el runner devuelve.
        // Sin NONE, Boot deduce el tipo de app por el classpath:
        // - Hoy el starter de Ollama solo arrastra spring-webflux (para WebClient), asi
        //   que la deduccion da REACTIVE y falla al arrancar: no hay servidor Netty.
        // - Si se agregara spring-boot-starter-web, daria SERVLET: Tomcat arranca, la
        //   app responde y el proceso no termina nunca.
        new SpringApplicationBuilder(GettingStartedApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    ApplicationRunner programRunner(ChatModel chatModel) {
        return args -> {
            // ChatModel es la abstraccion portable: esta clase no sabe que detras hay Ollama.
            String prompt = "In a single word, 'what is the answer to life, the universe, and everything?'";
            System.out.printf("User> %s%n%n", prompt);
            String response = chatModel.call(prompt);
            System.out.printf("AI> %s%n%n", response);
        };
    }
}
