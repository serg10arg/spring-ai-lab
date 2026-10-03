package io.github.serg10arg.springailab.labs.advisors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

// Documenta el comportamiento de MessageChatMemoryAdvisor en 1.0.8 (Spring AI, no codigo
// propio): el id de conversacion es obligatorio y la memoria se separa por ese id.
class MessageChatMemoryAdvisorTests {

    private final ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
    private final MessageChatMemoryAdvisor advisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    private final CallAdvisorChain chain = mock(CallAdvisorChain.class);

    @BeforeEach
    void setUp() {
        when(chain.nextCall(any())).thenAnswer(invocation -> ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage("Hello, Ada."))))
                        .build())
                .context(Map.copyOf(invocation.<ChatClientRequest>getArgument(0).context()))
                .build());
    }

    @Test
    void failsWithoutAConversationIdBeforeReachingTheModel() {
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("My name is Ada."))
                .context(Map.of())
                .build();

        assertThatThrownBy(() -> advisor.adviseCall(request, chain))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("conversationId cannot be null");
        verify(chain, never()).nextCall(any());
    }

    @Test
    void storesBothTurnsUnderTheGivenConversationOnly() {
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(new Prompt("My name is Ada."))
                .context(Map.of(ChatMemory.CONVERSATION_ID, "a"))
                .build();

        advisor.adviseCall(request, chain);

        assertThat(chatMemory.get("a")).extracting(message -> message.getText())
                .containsExactly("My name is Ada.", "Hello, Ada.");
        assertThat(chatMemory.get("b")).isEmpty();
    }
}
