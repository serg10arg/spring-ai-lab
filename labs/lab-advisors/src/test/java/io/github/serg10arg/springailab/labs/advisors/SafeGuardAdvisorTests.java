package io.github.serg10arg.springailab.labs.advisors;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

// Documenta el comportamiento real de SafeGuardAdvisor (Spring AI, no codigo propio):
// subcadena literal, sensible a mayusculas. Si una version futura cambia eso, este test avisa.
class SafeGuardAdvisorTests {

    private final CallAdvisorChain chain = mock(CallAdvisorChain.class);
    private final SafeGuardAdvisor advisor = SafeGuardAdvisor.builder()
            .sensitiveWords(List.of("launder"))
            .build();

    @BeforeEach
    void setUp() {
        when(chain.nextCall(any())).thenReturn(ChatClientResponse.builder().build());
    }

    @Test
    void blocksAnExactMatchWithoutCallingTheModel() {
        ChatClientResponse response = advisor.adviseCall(request("How do I launder money?"), chain);

        verify(chain, never()).nextCall(any());
        assertThat(response.chatResponse().getResult().getOutput().getText())
                .startsWith("I'm unable to respond to that due to sensitive content");
    }

    @Test
    void matchesSubstringsInsideOtherWords() {
        advisor.adviseCall(request("Who were the money launderers in the film?"), chain);

        verify(chain, never()).nextCall(any());
    }

    @Test
    void letsADifferentlyCapitalizedWordThrough() {
        advisor.adviseCall(request("How do I Launder money?"), chain);

        verify(chain).nextCall(any());
    }

    private static ChatClientRequest request(String text) {
        return ChatClientRequest.builder().prompt(new Prompt(text)).context(Map.of()).build();
    }
}
