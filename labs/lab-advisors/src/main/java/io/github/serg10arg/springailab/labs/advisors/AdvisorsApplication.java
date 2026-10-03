package io.github.serg10arg.springailab.labs.advisors;

import java.time.Clock;
import java.time.Duration;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AdvisorsApplication {

    public static void main(String[] args) {
        // Es un CLI, como los otros labs: sin NONE, spring-webflux en el classpath hace
        // que Boot deduzca una app REACTIVE y falle al arrancar.
        new SpringApplicationBuilder(AdvisorsApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    // El libro activa su RateLimitAdvisor con @EnableRateLimit, una anotacion de sus
    // extensiones del capitulo 7. Aca es un bean comun que se registra con defaultAdvisors.
    @Bean
    RateLimitAdvisor rateLimitAdvisor() {
        return new RateLimitAdvisor(2, Duration.ofMinutes(1), Clock.systemUTC());
    }
}
