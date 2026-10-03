package io.github.serg10arg.springailab.labs.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

// Solo imprime cuando entra el pedido y cuando sale la respuesta, para ver el orden.
class TracingAdvisor implements CallAdvisor {

    private final String name;
    private final int order;

    TracingAdvisor(String name, int order) {
        this.name = name;
        this.order = order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        System.out.printf("  -> %s (order %d) procesa el pedido%n", name, order);
        ChatClientResponse response = chain.nextCall(request);
        System.out.printf("  <- %s (order %d) procesa la respuesta%n", name, order);
        return response;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
