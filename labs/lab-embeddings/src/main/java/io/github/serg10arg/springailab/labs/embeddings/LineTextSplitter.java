package io.github.serg10arg.springailab.labs.embeddings;

import java.util.List;

import org.springframework.ai.transformer.splitter.TextSplitter;

// Un chunk por verso. TextSplitter copia la metadata del documento a cada chunk.
// Reemplaza al NewlineTextSplitter de las extensiones del libro (D-015).
class LineTextSplitter extends TextSplitter {

    @Override
    protected List<String> splitText(String text) {
        return text.replace("\r\n", "\n")
                .lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
    }
}
