# lab-embeddings

Capítulo 2: *Embeddings Model API*.

App de consola que recorre el camino texto → vector → `VectorStore` → búsqueda por
similitud, y mide cómo cambian los resultados según el umbral de similitud y la forma de
cortar el texto en chunks.

## Qué demuestra

| Perfil | Runner | Qué muestra |
| --- | --- | --- |
| `search` (default) | `SearchRunner` | El corpus se corta de tres formas (por verso, por estrofa, canción entera), se guarda en un `SimpleVectorStore` y se consulta con tres umbrales. La salida es la matriz de H-004 |
| `dimension` | `DimensionRunner` | `embeddingModel.embed(text)` devuelve un `float[]` de 768 posiciones: el largo lo fija el modelo, no el texto |

Piezas:

- `SimpleVectorStore`: un mapa en memoria que embebe cada chunk al agregarlo y compara
  por similitud coseno. Nadie lo autoconfigura; se construye con
  `SimpleVectorStore.builder(embeddingModel)`.
- `LineTextSplitter` y `StanzaTextSplitter`: splitters propios, porque `TokenTextSplitter`
  corta por cantidad de tokens y no por versos o estrofas (D-015).
- `SongCorpus`: cuatro canciones **originales, escritas para esta demo**, que sustituyen a
  las letras con derechos de autor del libro (D-016).

## El corpus y las consultas

| Canción | Rol |
| --- | --- |
| A — *Still Awake* (Paper Anchor) | Comparte la palabra `awake` con B; es la única con `do I still belong here` |
| B — *Awake in the Garden* (The Low Field) | Comparte la palabra `awake` con A |
| C — *Copper in the Rain* (Ninefold) | La única con la frase exacta `copper in the rain` |
| D — *Engine Room* (Marla Quint) | No debe aparecer en ninguna consulta |

| Consulta | Debe devolver |
| --- | --- |
| `awake` | A y B |
| `copper in the rain` | solo C |
| `do I still belong here` | solo A |

## Resultado (2026-10-03)

Canciones que pasan cada umbral, sin prefijos de tarea:

| Consulta | Chunking | ≥ 0,55 | ≥ 0,70 | ≥ 0,95 |
| --- | --- | --- | --- | --- |
| `awake` | verso | **A, B** | A | — |
| `awake` | estrofa | **A, B** | — | — |
| `awake` | canción | **A, B** | — | — |
| `copper in the rain` | verso | **C** | **C** | — |
| `copper in the rain` | estrofa | **C** | **C** | — |
| `copper in the rain` | canción | **C** | **C** | — |
| `do I still belong here` | verso | **A** | **A** | — |
| `do I still belong here` | estrofa | **A** | — | — |
| `do I still belong here` | canción | **A** | — | — |

En negrita, las celdas que coinciden con lo esperado. D no aparece nunca. La lectura
completa, con scores y márgenes, está en H-004 de `docs/diferencias-libro.md`.

## Qué deliberadamente no hace

- PGVector: `SimpleVectorStore` alcanza y es lo que usa el capítulo. Docker llega en la
  etapa 5.
- Ningún llamado al modelo de chat: `spring.ai.model.chat: none` evita que exista el
  bean. Es el módulo que garantiza que `nomic-embed-text` nunca comparta memoria con
  `llama3.2:3b`; después de correrlo, `ollama ps` muestra solo `nomic-embed-text`.
- RAG: unir búsqueda y chat es tema del capítulo 3.

## Ejecución

```powershell
.\gradlew :labs:lab-embeddings:build
.\gradlew :labs:lab-embeddings:bootRun --console=plain
.\gradlew :labs:lab-embeddings:bootRun --console=plain --args="--spring.profiles.active=dimension"

# La misma matriz con los prefijos de tarea de nomic-embed-text
.\gradlew :labs:lab-embeddings:bootRun --console=plain --args="--lab.embeddings.task-prefixes=true"
```

La matriz completa tarda unos 11 s (embebe 35 chunks y 27 consultas, más la mejor
puntuación por canción).

## Tests

Nivel 0, sin Ollama. Pasan con `SPRING_AI_OLLAMA_BASEURL=http://localhost:1`.

- `ChunkingStrategyTests`: cantidad de chunks por estrategia (24 versos, 7 estrofas,
  4 canciones), que cada chunk conserve el id de su canción, que el corte por estrofa
  tolere fines de línea de Windows, y la evidencia de D-015: `TokenTextSplitter` con sus
  defaults deja cada canción en un solo chunk.
- `EmbeddingsApplicationTests` (perfil `test`): ningún runner activo, el
  `EmbeddingModel` es `OllamaEmbeddingModel` con `nomic-embed-text`, y no hay `ChatModel`.
  Verificado que el último no pasa por casualidad: falla con `SPRING_AI_MODEL_CHAT=ollama`.

## Desviaciones

D-015, D-016, D-018 y H-004 en
[`docs/diferencias-libro.md`](../../docs/diferencias-libro.md).
