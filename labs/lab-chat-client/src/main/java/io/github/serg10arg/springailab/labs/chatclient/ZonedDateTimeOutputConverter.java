package io.github.serg10arg.springailab.labs.chatclient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.converter.StructuredOutputConverter;

// Converter propio: getFormat() le dice al modelo que devolver, convert() lo parsea.
// A proposito no rescata respuestas mal formadas: si el modelo agrega texto, falla.
class ZonedDateTimeOutputConverter implements StructuredOutputConverter<ZonedDateTime> {

    @Override
    public String getFormat() {
        return """
                Respond only with the date and time in RFC-1123 format, \
                for example: Tue, 3 Jun 2008 11:05:30 +0900. Do not include any other text.""";
    }

    @Override
    public ZonedDateTime convert(String text) {
        return ZonedDateTime.parse(text.strip(), DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}
