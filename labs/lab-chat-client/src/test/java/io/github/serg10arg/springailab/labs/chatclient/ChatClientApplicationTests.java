package io.github.serg10arg.springailab.labs.chatclient;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

// Nivel 0: con el perfil "test" activo no se activa ningun runner, asi que nada llama
// a Ollama. Construir el ChatClient y el OllamaChatModel no usa la red.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ChatClientApplicationTests {

    @Autowired
    ApplicationContext context;

    @Test
    void chatClientIsBuiltFromTheAutoConfiguredBuilder() {
        assertThat(context.getBean(ChatClient.class)).isNotNull();
        assertThat(context.getBeansOfType(ApplicationRunner.class)).isEmpty();
    }

    @Test
    void embeddingAutoConfigurationIsDisabled() {
        assertThat(context.getBeansOfType(EmbeddingModel.class)).isEmpty();
    }
}
