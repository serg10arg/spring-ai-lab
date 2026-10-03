package io.github.serg10arg.springailab.labs.embeddings;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.transformer.splitter.TextSplitter;

// Un chunk por estrofa: bloques separados por una o mas lineas en blanco.
// Reemplaza al ParagraphTextSplitter de las extensiones del libro (D-015).
class StanzaTextSplitter extends TextSplitter {

    @Override
    protected List<String> splitText(String text) {
        return Arrays.stream(text.replace("\r\n", "\n").split("\n\\s*\n"))
                .map(String::strip)
                .filter(stanza -> !stanza.isEmpty())
                .toList();
    }
}
