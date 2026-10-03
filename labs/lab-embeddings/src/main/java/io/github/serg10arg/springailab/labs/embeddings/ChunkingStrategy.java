package io.github.serg10arg.springailab.labs.embeddings;

import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.ai.document.Document;

// Las tres formas de cortar el corpus que compara el runner search.
enum ChunkingStrategy {

    LINE(new LineTextSplitter()::apply),
    STANZA(new StanzaTextSplitter()::apply),
    DOCUMENT(UnaryOperator.identity());

    private final UnaryOperator<List<Document>> splitter;

    ChunkingStrategy(UnaryOperator<List<Document>> splitter) {
        this.splitter = splitter;
    }

    List<Document> split(List<Document> documents) {
        return splitter.apply(documents);
    }
}
