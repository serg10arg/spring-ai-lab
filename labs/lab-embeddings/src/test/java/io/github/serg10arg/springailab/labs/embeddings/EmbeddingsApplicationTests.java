package io.github.serg10arg.springailab.labs.embeddings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

// Nivel 0: con el perfil "test" no se activa ningun runner, asi que nada llama a Ollama.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class EmbeddingsApplicationTests {

    @Autowired
    ApplicationContext context;

    @Test
    void noRunnerIsActiveUnderTheTestProfile() {
        assertThat(context.getBeansOfType(ApplicationRunner.class)).isEmpty();
    }

    @Test
    void embeddingModelIsOllamaWithNomicEmbedText() {
        EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);

        assertThat(embeddingModel).isInstanceOf(OllamaEmbeddingModel.class);
        assertThat(context.getEnvironment().getProperty("spring.ai.ollama.embedding.options.model"))
                .isEqualTo("nomic-embed-text");
    }

    // Invariante de RAM: este modulo no puede pedir el modelo de chat.
    @Test
    void chatAutoConfigurationIsDisabled() {
        assertThat(context.getBeansOfType(ChatModel.class)).isEmpty();
    }
}
