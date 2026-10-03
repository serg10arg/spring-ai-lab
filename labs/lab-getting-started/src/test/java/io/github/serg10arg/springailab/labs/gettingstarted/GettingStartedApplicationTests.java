package io.github.serg10arg.springailab.labs.gettingstarted;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Nivel 0: no toca Ollama. Con pull-model-strategy: never, construir el OllamaChatModel
// no hace llamadas de red; la unica llamada real esta en el runner, que se reemplaza
// por un mock porque @SpringBootTest ejecuta los ApplicationRunner.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GettingStartedApplicationTests {

    @MockitoBean(name = "programRunner")
    ApplicationRunner programRunner;

    @Autowired
    ChatModel chatModel;

    @Test
    void autoConfigurationResolvesOllamaChatModelFromYaml() {
        assertThat(chatModel).isInstanceOf(OllamaChatModel.class);
        assertThat(chatModel.getDefaultOptions().getModel()).isEqualTo("llama3.2:3b");
    }
}
