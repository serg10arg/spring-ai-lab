package io.github.serg10arg.springailab.labs.advisors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

// Nivel 0: con el perfil "test" no se activa ningun runner, asi que nada llama a Ollama.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AdvisorsApplicationTests {

    @Autowired
    ApplicationContext context;

    @Test
    void noRunnerIsActiveUnderTheTestProfile() {
        assertThat(context.getBeansOfType(ApplicationRunner.class)).isEmpty();
    }

    @Test
    void chatMemoryIsAutoConfiguredByTheOllamaStarter() {
        assertThat(context.getBean(ChatMemory.class)).isInstanceOf(MessageWindowChatMemory.class);
    }

    @Test
    void rateLimitAdvisorIsAPlainBean() {
        assertThat(context.getBean(RateLimitAdvisor.class)).isNotNull();
    }

    @Test
    void embeddingAutoConfigurationIsDisabled() {
        assertThat(context.getBeansOfType(EmbeddingModel.class)).isEmpty();
    }
}
