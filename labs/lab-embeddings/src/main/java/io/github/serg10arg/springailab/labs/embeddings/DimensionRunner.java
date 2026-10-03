package io.github.serg10arg.springailab.labs.embeddings;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// El ejemplo minimo: texto entra, vector sale. El largo del vector lo fija el modelo.
@Component
@Profile("dimension")
class DimensionRunner implements ApplicationRunner {

    private final EmbeddingModel embeddingModel;

    DimensionRunner(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(ApplicationArguments args) {
        String text = "copper in the rain";
        float[] vector = embeddingModel.embed(text);

        System.out.printf("Text> %s%n", text);
        System.out.printf("Dimensions> %d%n", vector.length);
        System.out.printf("First values> %.4f, %.4f, %.4f, ...%n%n", vector[0], vector[1], vector[2]);
    }
}
