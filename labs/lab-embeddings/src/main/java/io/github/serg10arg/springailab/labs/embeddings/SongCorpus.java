package io.github.serg10arg.springailab.labs.embeddings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;

// Corpus original escrito para esta demo; sustituye a las letras con derechos de autor
// del libro (D-016). Cada cancion cumple un rol fijo frente a las tres consultas.
final class SongCorpus {

    static final String SONG = "song";

    private record Song(String id, String title, String artist, String resource) {
    }

    private static final List<Song> SONGS = List.of(
            new Song("A", "Still Awake", "Paper Anchor", "still-awake.txt"),
            new Song("B", "Awake in the Garden", "The Low Field", "awake-in-the-garden.txt"),
            new Song("C", "Copper in the Rain", "Ninefold", "copper-in-the-rain.txt"),
            new Song("D", "Engine Room", "Marla Quint", "engine-room.txt"));

    private SongCorpus() {
    }

    // Un Document por cancion, con su id en la metadata para saber de donde salio cada chunk.
    static List<Document> load() {
        return SONGS.stream()
                .map(song -> new Document(read(song.resource()), Map.of(
                        SONG, song.id(),
                        "title", song.title(),
                        "artist", song.artist())))
                .toList();
    }

    static List<String> songIds() {
        return SONGS.stream().map(Song::id).toList();
    }

    private static String read(String resource) {
        try {
            return new ClassPathResource("corpus/" + resource)
                    .getContentAsString(StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
