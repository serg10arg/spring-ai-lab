package io.github.serg10arg.springailab.labs.chatclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

class ZonedDateTimeOutputConverterTests {

    private final ZonedDateTimeOutputConverter converter = new ZonedDateTimeOutputConverter();

    @Test
    void parsesRfc1123WithOffset() {
        assertThat(converter.convert(" Sat, 3 Oct 2026 23:00:00 +0900\n"))
                .isEqualTo(ZonedDateTime.of(2026, 10, 3, 23, 0, 0, 0, ZoneOffset.ofHours(9)));
    }

    @Test
    void rejectsAnswersWithExtraText() {
        assertThatThrownBy(() -> converter.convert("The time in Tokyo is Sat, 3 Oct 2026 23:00:00 +0900"))
                .isInstanceOf(DateTimeParseException.class);
    }

    // RFC-1123 solo admite GMT u offsets numericos: abreviaturas como JST no parsean.
    @Test
    void rejectsZoneAbbreviations() {
        assertThatThrownBy(() -> converter.convert("Sat, 3 Oct 2026 23:00:00 JST"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
