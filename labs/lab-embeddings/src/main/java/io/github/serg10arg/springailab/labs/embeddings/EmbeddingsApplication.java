package io.github.serg10arg.springailab.labs.embeddings;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class EmbeddingsApplication {

    public static void main(String[] args) {
        // Es un CLI, como los otros labs: sin NONE, spring-webflux en el classpath hace
        // que Boot deduzca una app REACTIVE y falle al arrancar.
        new SpringApplicationBuilder(EmbeddingsApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
