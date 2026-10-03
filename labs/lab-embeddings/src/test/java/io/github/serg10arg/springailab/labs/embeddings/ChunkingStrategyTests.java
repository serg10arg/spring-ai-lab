package io.github.serg10arg.springailab.labs.embeddings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

class ChunkingStrategyTests {

    private final List<Document> corpus = SongCorpus.load();

    @Test
    void lineStrategyProducesOneChunkPerVerseAndSkipsBlankLines() {
        List<Document> chunks = ChunkingStrategy.LINE.split(corpus);

        // 8 + 6 + 6 + 4 versos
        assertThat(chunks).hasSize(24);
        assertThat(chunks).noneMatch(chunk -> chunk.getText().isBlank());
        assertThat(chunks.get(3).getText()).isEqualTo("do I still belong here, or do I break");
    }

    @Test
    void stanzaStrategyProducesOneChunkPerStanza() {
        List<Document> chunks = ChunkingStrategy.STANZA.split(corpus);

        // 2 + 2 + 2 + 1 estrofas
        assertThat(chunks).hasSize(7);
        assertThat(chunks.get(4).getText()).startsWith("Copper in the rain, copper in the rain");
    }

    @Test
    void documentStrategyKeepsEachSongWhole() {
        assertThat(ChunkingStrategy.DOCUMENT.split(corpus)).hasSize(4);
    }

    @Test
    void everyChunkKeepsTheSongIdOfItsDocument() {
        List<Document> chunks = ChunkingStrategy.LINE.split(corpus);

        assertThat(chunks).allMatch(chunk -> chunk.getMetadata().containsKey(SongCorpus.SONG));
        assertThat(chunks).filteredOn(chunk -> chunk.getText().contains("copper"))
                .allMatch(chunk -> chunk.getMetadata().get(SongCorpus.SONG).equals("C"));
    }

    @Test
    void stanzaSplitterToleratesWindowsLineEndings() {
        Document doc = new Document("one\r\ntwo\r\n\r\nthree", Map.of(SongCorpus.SONG, "X"));

        assertThat(ChunkingStrategy.STANZA.split(List.of(doc)))
                .extracting(Document::getText)
                .containsExactly("one\ntwo", "three");
    }

    // Evidencia de D-015: con sus defaults (800 tokens por chunk), TokenTextSplitter deja
    // cada cancion entera en un solo chunk, que es el caso que el libro descarta.
    @Test
    void tokenTextSplitterDefaultsLeaveEachSongInASingleChunk() {
        assertThat(new TokenTextSplitter().apply(corpus)).hasSize(4);
    }
}
