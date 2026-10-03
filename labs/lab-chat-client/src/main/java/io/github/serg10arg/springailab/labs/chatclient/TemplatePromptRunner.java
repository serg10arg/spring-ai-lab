package io.github.serg10arg.springailab.labs.chatclient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Plantilla con argumentos en runtime y conversion de la salida a un tipo Java.
@Component
@Profile("template")
class TemplatePromptRunner implements ApplicationRunner {

    // Sin {format}: en Spring AI 1.0, entity(converter) agrega getFormat() al final del
    // mensaje de usuario por su cuenta. Inyectarlo tambien por params lo duplicaria.
    private static final String TEMPLATE = """
            The date and time in {sourceZone} is {dateTime}.
            What is the date and time in {targetZone} at that same instant?
            """;

    // Entrada fija: hace comparables las corridas y deja verificar la respuesta.
    private static final ZonedDateTime SOURCE_TIME =
            ZonedDateTime.of(2026, 10, 3, 14, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Tokyo");

    private final ChatClient chatClient;

    TemplatePromptRunner(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Object> params = Map.of(
                "sourceZone", SOURCE_TIME.getZone().getId(),
                "dateTime", SOURCE_TIME.format(DateTimeFormatter.RFC_1123_DATE_TIME),
                "targetZone", TARGET_ZONE.getId());
        System.out.printf("User> %s%n", params);

        ZonedDateTime answer = chatClient.prompt()
                .user(user -> user.text(TEMPLATE).params(params))
                .call()
                .entity(new ZonedDateTimeOutputConverter());

        ZonedDateTime expected = SOURCE_TIME.withZoneSameInstant(TARGET_ZONE);
        System.out.printf("AI> %s%n", answer.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.printf("Expected> %s%n", expected.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.printf("Same instant> %s%n%n", answer.toInstant().equals(expected.toInstant()));
    }
}
