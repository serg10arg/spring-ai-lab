package io.github.serg10arg.springailab.labs.embeddings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// Texto -> vector -> VectorStore -> busqueda por similitud, repetido para cada forma de
// cortar el corpus y cada umbral. La salida es la matriz de H-004.
@Component
@Profile("search")
class SearchRunner implements ApplicationRunner {

    private static final List<String> QUERIES = List.of("awake", "copper in the rain", "do I still belong here");
    private static final List<Double> THRESHOLDS = List.of(0.55, 0.70, 0.95);

    private final EmbeddingModel embeddingModel;
    private final boolean taskPrefixes;

    SearchRunner(EmbeddingModel embeddingModel, @Value("${lab.embeddings.task-prefixes}") boolean taskPrefixes) {
        this.embeddingModel = embeddingModel;
        this.taskPrefixes = taskPrefixes;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.printf("Task prefixes> %s%n%n", taskPrefixes);
        for (ChunkingStrategy strategy : ChunkingStrategy.values()) {
            List<Document> chunks = strategy.split(SongCorpus.load()).stream()
                    .map(chunk -> new Document(documentText(chunk.getText()), chunk.getMetadata()))
                    .toList();

            // SimpleVectorStore no lo autoconfigura nadie: es un mapa en memoria que
            // embebe cada chunk al agregarlo.
            SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
            store.add(chunks);

            System.out.printf("== %s (%d chunks)%n", strategy, chunks.size());
            for (String query : QUERIES) {
                System.out.printf("Query> %s%n", query);
                System.out.printf("  best score per song> %s%n", bestScorePerSong(store, query, chunks.size()));
                for (double threshold : THRESHOLDS) {
                    System.out.printf("  >= %.2f> %s%n", threshold, songsAbove(store, query, threshold, chunks.size()));
                }
            }
            System.out.println();
        }
    }

    // topK = cantidad de chunks: el umbral es el unico filtro. Con el default de
    // SearchRequest (topK 4) el corte por linea perderia resultados que si pasan el umbral.
    private List<Document> search(SimpleVectorStore store, String query, double threshold, int topK) {
        return store.similaritySearch(SearchRequest.builder()
                .query(queryText(query))
                .similarityThreshold(threshold)
                .topK(topK)
                .build());
    }

    private String bestScorePerSong(SimpleVectorStore store, String query, int topK) {
        Map<String, Double> best = new LinkedHashMap<>();
        SongCorpus.songIds().forEach(id -> best.put(id, 0.0));
        for (Document doc : search(store, query, SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL, topK)) {
            best.merge((String) doc.getMetadata().get(SongCorpus.SONG), doc.getScore(), Math::max);
        }
        return best.entrySet().stream()
                .map(entry -> "%s=%.3f".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));
    }

    private TreeSet<String> songsAbove(SimpleVectorStore store, String query, double threshold, int topK) {
        return search(store, query, threshold, topK).stream()
                .map(doc -> (String) doc.getMetadata().get(SongCorpus.SONG))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private String documentText(String text) {
        return taskPrefixes ? "search_document: " + text : text;
    }

    private String queryText(String query) {
        return taskPrefixes ? "search_query: " + query : query;
    }
}
