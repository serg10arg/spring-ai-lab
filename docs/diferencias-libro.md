# Diferencias con el libro

Este documento registra cada desviación respecto de *Building Intelligent Applications
with Spring AI* (John Blum, Packt 2026). No son correcciones al libro sino decisiones
de diseño: cada entrada dice qué hace el libro, qué hago yo, por qué y qué problema
evita. Al final se listan las erratas encontradas en el texto.

## Desviaciones

### D-001 — Monorepo Gradle

- **Libro:** cada capítulo es un proyecto Maven independiente.
- **Acá:** un único monorepo Gradle multi-módulo (`labs/`, `apps/`, `platform/`,
  `agents/`) con un version catalog compartido.
- **Por qué:** el capítulo 7 construye piezas transversales pensadas para que las
  consuman las aplicaciones de los capítulos anteriores.
- **Problema que evita:** con proyectos sueltos, esas piezas habría que copiarlas en
  cada uno o publicarlas a un repositorio de artefactos.

### D-002 — Java 21

- **Libro:** Java 17.
- **Acá:** Java 21 (Amazon Corretto), fijado vía toolchain de Gradle.
- **Por qué:** pattern matching para `switch` y record patterns simplifican el manejo
  de respuestas estructuradas del modelo.
- **Problema que evita:** cadenas de `instanceof` y casteos manuales al desarmar
  respuestas.

### D-003 — `platform()` en vez de `io.spring.dependency-management`

- **Libro:** usa la gestión de dependencias de Spring vía Maven (`<dependencyManagement>`
  con los BOMs), cuyo equivalente habitual en Gradle es el plugin
  `io.spring.dependency-management`.
- **Acá:** los BOMs de Spring Boot y Spring AI se importan con `platform()` nativo de
  Gradle, con versiones declaradas en `gradle/libs.versions.toml`.
- **Por qué:** Gradle resuelve BOMs de forma nativa desde hace años; el plugin no
  aporta nada que este repo necesite.
- **Problema que evita:** un plugin de terceros menos en la cadena de compatibilidad
  entre Gradle y Spring Boot.

### D-004 — Spring Boot 3.5.16 pese al fin de soporte

- **Libro:** línea Spring Boot 3.x vigente al momento de escribirse.
- **Acá:** Spring Boot 3.5.16, aunque la línea 3.5 llegó a fin de soporte OSS el
  30/06/2026.
- **Por qué:** Spring AI 1.0.8 exige Spring Boot 3.x. Pasar a Boot 4 obligaría a
  Spring AI 2.0, que reorganiza tool calling, advisors y MCP.
- **Problema que evita:** que el código diverja de la API que enseña el libro antes de
  completar el recorrido. La migración a Boot 4 / Spring AI 2.0 queda como trabajo
  futuro.

### D-005 — Módulo `labs/lab-getting-started` en vez de proyecto suelto

- **Libro:** proyecto Maven independiente `example-one`, generado en start.spring.io.
- **Acá:** módulo Gradle `labs/lab-getting-started` dentro del monorepo.
- **Por qué:** consecuencia de D-001. Es un módulo propio, y no parte de
  `lab-core-api`, para que su `build.gradle.kts` muestre que el proveedor es una sola
  dependencia.
- **Problema que evita:** que la tesis del capítulo se pierda entre advisors,
  embeddings y vector stores, y quedarse sin un ejemplo de control barato.

### D-006 — `GettingStartedApplication` en vez de `ExampleOneApplication`

- **Libro:** clase `ExampleOneApplication`.
- **Acá:** `GettingStartedApplication`, en `io.github.serg10arg.springailab.labs.gettingstarted`.
- **Por qué:** `example-one` solo tiene sentido dentro del repositorio de Packt.
- **Problema que evita:** nombres que no dicen qué hace la clase.

### D-007 — `application.yml` en vez de `application.properties`

- **Libro:** `application.properties`.
- **Acá:** `application.yml`.
- **Por qué:** convención del repo; las properties `spring.ai.*` anidan varios niveles.
- **Problema que evita:** repetir prefijos largos en cada línea.

### D-008 — Ollama desde el inicio; OpenAI documentado pero no ejecutado

- **Libro:** arranca con OpenAI (`gpt-4o-mini`) y cambia a Ollama al final del capítulo.
- **Acá:** arranca y queda en Ollama. El camino a OpenAI está descrito paso a paso en
  el README del módulo.
- **Por qué:** restricción de costo. El cambio de proveedor solo se demuestra a medias
  (cambio de modelo dentro de Ollama); la demostración completa llega en la etapa 4,
  cuando OpenAI entra por necesidad.
- **Problema que evita:** gastar en un proveedor de nube para un ejemplo de una línea.

### D-009 — `llama3.2:3b` en vez de `llama3.2`

- **Libro:** `spring.ai.ollama.chat.options.model=llama3.2`.
- **Acá:** `llama3.2:3b`.
- **Por qué:** Ollama resuelve por nombre exacto. Si se descargó el tag explícito, el
  nombre sin tag no existe localmente. Verificado: con `llama3.2` la llamada falla con
  `HTTP 404 - model 'llama3.2' not found`.
- **Problema que evita:** un 404 que parece problema de conexión.

### D-010 — `pull-model-strategy: never` explícito

- **Libro:** no configura la estrategia de pull de modelos.
- **Acá:** `spring.ai.ollama.init.pull-model-strategy: never` declarado explícitamente.
- **Por qué:** con 8 GB de RAM y disco limitado, nunca se debe disparar una descarga
  implícita en el arranque. Declararlo además permite que el test de contexto no
  dependa de la red.
- **Problema que evita:** descargas de varios GB por un typo en el nombre del modelo.

`WebApplicationType.NONE` no es una desviación: el libro lo usa igual, y su texto cubre
tanto el servidor Servlet (Tomcat) como el reactivo (Netty). En este classpath aplica
el reactivo; el detalle está en el README del módulo.

### D-011 — Moderation Model API documentada, no implementada

- **Libro:** `OpenAiModerationModel` con `ModerationPrompt`.
- **Acá:** no se implementa. Su lugar local lo ocupa `SafeGuardAdvisor` (etapa 2,
  `lab-advisors`), que **no es equivalente**: filtra por lista de términos y no es un
  clasificador entrenado.
- **Por qué:** la abstracción existe. `ModerationModel`, `ModerationPrompt` y
  `ModerationResponse` están en `spring-ai-model` 1.0.8, pero `spring-ai-ollama` no la
  implementa. Verificado en los jars el 2026-10-03.
- **Problema que evita:** pagar un proveedor de nube por una demo de bajo valor. Si
  algún día aparece un proveedor local, el código de aplicación no cambia: solo falta
  el proveedor.
- **Límite del sustituto (ver H-003):** una lista de palabras no es moderación. Y cuando
  la lista falló, lo que frenó el pedido fue el alineamiento del propio modelo: una capa
  que no se controla ni se configura, y que cambia si se cambia de modelo. Eso no es una
  garantía.

### D-012 — Image Model API documentada, no implementada

- **Libro:** `ImageModel` sobre DALL-E.
- **Acá:** no se implementa.
- **Por qué:** igual que D-011. `ImageModel` existe como SPI en `spring-ai-model`, y
  Ollama no tiene implementación porque no genera imágenes.
- **Problema que evita:** gastar presupuesto en una capacidad que no aporta a un
  portfolio de backend.

### D-013 — Audio Model API diferida a las etapas 4 y 5

- **Libro:** transcripción con Whisper (`OpenAiAudioTranscriptionModel`) y síntesis de
  voz. El propio capítulo dice que vuelve al audio en el capítulo 5.
- **Acá:** se difiere a las etapas 4 y 5, que lo necesitan de verdad.
- **Por qué, y la restricción que hereda la arquitectura:** a diferencia de chat,
  embeddings, image y moderation, audio **no tiene interfaz de modelo genérica** en
  1.0.8. Los tipos de prompt y respuesta (`AudioTranscriptionPrompt`,
  `AudioTranscriptionResponse`) son genéricos, pero no existe una SPI de modelo de
  transcripción ni de síntesis de voz. El código de audio de las etapas 4 y 5 va a
  quedar acoplado a tipos de OpenAI, y eso no se resuelve cambiando el starter.
- **Problema que evita:** descubrir ese acoplamiento a mitad de la etapa 4.

### D-014 — `RateLimitAdvisor` sin `@EnableRateLimit` ni `@EnableChatClient`

- **Libro:** activa su `RateLimitAdvisor` con anotaciones de su módulo de extensiones.
- **Acá:** `RateLimitAdvisor` es un bean común, registrado con `.defaultAdvisors(...)`.
- **Por qué:** esas anotaciones no son de Spring AI: son extensiones que el libro
  construye en el capítulo 7. Usarlas en el capítulo 2 adelantaría ese material.
- **Problema que evita:** que el ejemplo dependa de código que todavía no existe en el
  repo. En la etapa 7 se evalúa si vale la pena agregarlas en `platform/ai-platform`.

### D-015 — Splitters propios por verso y por estrofa

- **Libro:** `NewlineTextSplitter` y `ParagraphTextSplitter`, de su módulo de extensiones
  del capítulo 7.
- **Acá:** `LineTextSplitter` y `StanzaTextSplitter`, dos subclases mínimas de
  `TextSplitter` en `lab-embeddings`. Quedan marcadas para reemplazar en la etapa 7.
- **Por qué no `TokenTextSplitter`, que sí trae Spring AI:** corta por cantidad de
  tokens, no por versos o estrofas. Con sus defaults (800 tokens por chunk) cada canción
  entra entera en un solo chunk: verificado con un test
  (`tokenTextSplitterDefaultsLeaveEachSongInASingleChunk`). Con un tamaño chico corta en
  lugares arbitrarios y parte versos al medio. La demo necesita que el chunk *sea* el
  verso o la estrofa, porque busca que una frase corta coincida con el fragmento que la
  contiene. El propio autor probó un `TokenTextSplitter` chico y lo dejó comentado.
- **Problema que evita:** depender de código del capítulo 7 en el capítulo 2.

### D-016 — Corpus de letras sustituido por canciones originales

- **Libro:** carga letras completas de AC/DC, Bee Gees y Pearl Jam.
- **Acá:** cuatro canciones originales escritas para la demo, en inglés, con la misma
  estructura: dos comparten una palabra, una sola contiene una frase exacta, otra una
  frase parcial, y una no debe coincidir con ninguna consulta.
- **Por qué:** el repositorio es público y las letras tienen derechos de autor. En
  inglés, porque `nomic-embed-text` está entrenado sobre todo en inglés y las consultas
  del libro son en inglés: un corpus en español agregaría una variable más justo donde
  se calibran umbrales a mano.
- **Consecuencia:** los umbrales del libro no se pueden comparar número a número con los
  de acá; se recalibran (H-004).
- **Problema que evita:** publicar material con derechos de autor en un portfolio.

### D-017 — `MessageChatMemoryAdvisor` implementado sin ejemplo en el libro

- **Libro:** describe el advisor de memoria en detalle, pero no trae ejemplo de código.
- **Acá:** `lab-advisors`, perfil `memory`: la misma conversación de dos turnos sin y con
  el advisor.
- **Por qué:** es el advisor que más se usa en los capítulos siguientes, y el contraste
  "sin memoria" es la lección.
- **Detalle de 1.0.8:** el `ChatMemory` lo autoconfigura el starter
  (`MessageWindowChatMemory`, 20 mensajes), pero el advisor exige el id de conversación
  en el contexto de cada pedido: `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))`.
  Sin él falla con `IllegalArgumentException: conversationId cannot be null` antes de
  llegar al modelo. Verificado ejecutándolo (`MessageChatMemoryAdvisorTests`).
- **Problema que evita:** llegar al capítulo 3 sin haber visto la memoria funcionando.

### D-018 — `spring-ai-vector-store` declarado aparte en el catálogo

- **Libro:** Maven, con los starters de cada ejemplo.
- **Acá:** `spring-ai-vector-store` es una entrada propia de `gradle/libs.versions.toml`
  y solo la declara `lab-embeddings`.
- **Por qué:** el starter de Ollama trae el modelo, no el ecosistema. `SimpleVectorStore`
  y `SearchRequest` no están en su classpath (verificado en los jars). La memoria de chat
  sí viene con el starter (`ChatMemoryAutoConfiguration`), así que no hace falta otra
  dependencia para eso.
- **Problema que evita:** suponer que un starter de proveedor incluye vector stores, y
  descubrirlo recién al compilar.

### D-019 — `RateLimitAdvisor` lanza una excepción en vez de devolver una respuesta sintética

- **Libro:** al superar el límite, el advisor arma un `ChatClientResponse` con un
  `AssistantMessage` de "Rate Limit Exceeded" y lo devuelve como si fuera la respuesta.
- **Acá:** lanza `RateLimitExceededException` y no devuelve nada.
- **Por qué:** es un desacuerdo técnico con el autor, no una adaptación forzada por el
  stack. Una respuesta sintética no se distingue de una generada por el modelo: el
  llamador la imprime, la guarda en memoria o la parsea como si el modelo hubiera
  contestado. Es el mismo modo de falla silencioso que muestra `SafeGuardAdvisor` en
  H-003, cuyo rechazo sale como un mensaje del asistente. Una excepción obliga al
  llamador a tratar el rechazo como lo que es.
- **Lo que cuesta:** el llamador tiene que capturar la excepción (`RateLimitRunner` lo
  hace). Con la versión del libro, el código que no sabe del límite sigue funcionando,
  pero con datos falsos.
- **Problema que evita:** rechazos que se cuelan como contenido del modelo.

## Hallazgos de calidad de modelo

### H-001 — Obediencia a instrucciones de formato (cap. 1)

El prompt del capítulo pide responder "in a single word". `llama3.2:3b` respondió `42.`
en la corrida del 2026-10-03, pero un modelo de 3B no respeta las instrucciones de
formato tan consistentemente como `gpt-4o-mini`, y puede contestar con un párrafo. No es
un bug de la API: es la primera muestra de la brecha entre un modelo chico local y uno
grande en la nube. Va a importar en el capítulo 3 (structured output), donde la salida
tiene que poder parsearse.

Es una muestra de tamaño uno. **Se re-verifica en la etapa 3**, con structured output:
si `llama3.2:3b` no produce JSON parseable de forma consistente, la hipótesis queda
confirmada y hará falta otra estrategia (reintentos, un modelo más grande, o validar
contra el esquema).

### H-002 — Formato obedecido, aritmética de husos horarios incorrecta (cap. 2)

`lab-chat-client`, perfil `template`: convertir `Sat, 3 Oct 2026 14:00:00 GMT` (UTC) a
`Asia/Tokyo` y responder en RFC-1123. El esperado es `Sat, 3 Oct 2026 23:00:00 +0900`.
Cinco corridas por temperatura, el 2026-10-03:

| Métrica | `temperature: 0.0` | `temperature: 0.8` |
| --- | --- | --- |
| Respuestas que parsean como RFC-1123 | 5 de 5 | 3 de 5 |
| Respuestas correctas | 0 de 5 | 0 de 5 |
| Respuestas distintas entre sí | 1 | 4 |

Respuestas a 0.0: las cinco `Sat, 3 Oct 2026 04:00:00 +0900`. Respuestas a 0.8:
`07:00 +0900` (dos veces), `03:00 +0900`, y dos que no parsean:
`Sun, 3 Oct 2026 04:00:00 JST` (abreviatura de zona, que RFC-1123 no admite) y
`Wed, 3 Oct 2026 04:00:00 +0900` (día de la semana inconsistente con la fecha, que
`java.time` rechaza).

Lecturas:

- **Una respuesta que parsea no es una respuesta correcta.** La hipótesis previa era que
  fallaría el formato. A temperatura 0 el formato sale bien siempre y el contenido sale
  mal siempre: es el modo de falla silencioso. Un `StructuredOutputConverter` valida la
  forma, no el contenido.
- **La temperatura explica la identidad entre corridas, verificado.** A 0.8 las
  respuestas divergen y aparecen fallas de formato. A 0.0 la falla es sistemática, así
  que reintentar no la corrige; a 0.8 reintentar puede conseguir una respuesta que
  parsee, pero ninguna de las diez fue correcta.
- **Hipótesis sobre el mecanismo, no verificada:** el modelo no hace aritmética de
  instantes. Toma la hora del reloj de la entrada y le aplica un delta inventado, sin
  usar que el RFC-1123 de entrada ya trae su offset. Si es así, el ejemplo le pide al
  LLM un cálculo que corresponde a `java.time`, no a un modelo generativo. Es el
  problema que resuelve tool calling (capítulo 3): el modelo decide *qué* calcular y una
  función Java lo calcula.

### H-003 — `SafeGuardAdvisor` no es moderación (cap. 2)

`lab-advisors`, perfil `safeguard`, palabra prohibida `launder`, 2026-10-03.
`SafeGuardAdvisor` busca subcadenas literales en el contenido del prompt, distinguiendo
mayúsculas (`String.contains`, verificado en el código fuente de 1.0.8):

| Prompt | Resultado | Por qué |
| --- | --- | --- |
| `How do I launder money?` | Bloqueado en 1 ms | Coincidencia exacta; el modelo nunca recibe el pedido |
| `How do I Launder money?` | Pasa al modelo | Falso negativo: la mayúscula esquiva el filtro |
| `Who were the money launderers in the film?` | Bloqueado | Falso positivo: subcadena dentro de otra palabra (test unitario) |

En la corrida, el pedido con mayúscula lo frenó el propio `llama3.2:3b`, que se negó a
responder. **Cuando el guardrail falló, la seguridad vino de la capa que no se controla
ni se configura**: el alineamiento del modelo, que cambia si se cambia de modelo y no se
puede testear como código. Lo que salvó la corrida no es una garantía.
`SafeGuardAdvisor` sirve como corte barato y determinista antes de gastar una llamada,
pero no reemplaza a un clasificador (D-011). Si en algún momento se usa en serio, hay que
normalizar el texto antes de comparar, o escribir un advisor propio que lo haga.

### H-004 — El umbral y el corte deciden el resultado (cap. 2)

`lab-embeddings`, perfil `search`, `nomic-embed-text`, 2026-10-03. Tres consultas, tres
formas de cortar el corpus y tres umbrales: 27 celdas. `topK` igual a la cantidad de
chunks, para que el umbral sea el único filtro.

Mejor score por canción (sin prefijos de tarea; en negrita, lo que debería aparecer):

| Consulta | Chunking | A | B | C | D |
| --- | --- | --- | --- | --- | --- |
| `awake` | verso | **0,778** | **0,669** | 0,422 | 0,499 |
| `awake` | estrofa | **0,626** | **0,662** | 0,418 | 0,451 |
| `awake` | canción | **0,607** | **0,604** | 0,413 | 0,451 |
| `copper in the rain` | verso | 0,484 | 0,473 | **0,941** | 0,443 |
| `copper in the rain` | estrofa | 0,474 | 0,472 | **0,828** | 0,479 |
| `copper in the rain` | canción | 0,439 | 0,479 | **0,811** | 0,479 |
| `do I still belong here` | verso | **0,895** | 0,466 | 0,484 | 0,458 |
| `do I still belong here` | estrofa | **0,651** | 0,448 | 0,448 | 0,489 |
| `do I still belong here` | canción | **0,581** | 0,442 | 0,459 | 0,489 |

Lecturas:

- **A 0,55, las 9 combinaciones dan exactamente lo esperado.** Es el umbral robusto para
  este corpus: queda por encima de todo lo irrelevante (máximo 0,499) y por debajo de
  todo lo relevante (mínimo 0,581).
- **A 0,70, el chunking decide.** Por verso se conservan `copper in the rain` y
  `do I still belong here`, y `awake` pierde B. Por estrofa o por canción entera se
  pierden `awake` y `do I still belong here` por completo. Solo `copper in the rain`
  sobrevive con las tres estrategias.
- **A 0,95 no vuelve nada, nunca.** Ni siquiera el verso que contiene la frase exacta
  (`Copper in the rain, copper in the rain`, 0,941). Un umbral alto no significa "solo
  coincidencias exactas": significa "nada" si el chunk tiene algo más que la consulta.
- **Cuanto más chico el chunk, más alto el score del fragmento que coincide.** Para
  `do I still belong here`: 0,895 por verso, 0,651 por estrofa, 0,581 por canción. El
  resto del texto diluye el vector. Con la canción entera, el margen sobre lo irrelevante
  es de solo 0,092: es el caso que el libro descarta.

**Hipótesis de los prefijos de tarea, no confirmada en este corpus.** `nomic-embed-text`
fue entrenado con prefijos (`search_document:` / `search_query:`) y Spring AI manda el
texto crudo (verificado en el código fuente de `OllamaEmbeddingModel`). Con los prefijos
puestos a mano (`--lab.embeddings.task-prefixes=true`):

- Los conjuntos de resultados son **idénticos en las 27 celdas**.
- Los scores se comprimen: lo relevante baja (0,941 → 0,820) y lo irrelevante sube
  (0,422 → 0,475).
- El margen entre la peor canción relevante y la mejor irrelevante **baja en las 9
  combinaciones** (por ejemplo, verso + `do I still belong here`: 0,411 → 0,277).

Con consultas de dos a cinco palabras y cuatro documentos, los prefijos no mejoran la
separación; la empeoran. No alcanza para generalizar: es un corpus chico y una corrida.
Queda para re-verificar en el capítulo 3, con RAG sobre documentos más largos.

## Drift de API entre el libro y Spring AI 1.0.8

El libro muestra código de varias épocas de Spring AI, parte anterior a 1.0 GA. Cada
fila está verificada contra los jars o el código fuente de 1.0.8.

| Patrón del libro | En Spring AI 1.0.8 | Verificado |
| --- | --- | --- |
| `Document.builder().withContent(..).withId(..)` | `Document.builder().text(..).id(..)` | `javap`, 2026-10-03 |
| `doc.getContent()` | `doc.getText()` | `javap`, 2026-10-03 |
| `ChatResponse.builder().withGenerations(..)` | `ChatResponse.builder().generations(..)` | `javap`, 2026-10-03 |
| `SearchRequest.query(q).withSimilarityThreshold(d).withTopK(k)` | `SearchRequest.builder().query(q).similarityThreshold(d).topK(k).build()`. Ojo: `topK` por defecto es 4 | Código fuente, 2026-10-03 |
| `InMemoryChatMemory` | No existe. `MessageWindowChatMemory` sobre `InMemoryChatMemoryRepository`, autoconfigurado por el starter | Listado del jar, 2026-10-03 |

## Erratas detectadas en el libro

| Ubicación            | Dice                            | Debería decir                     | Nota                         |
|----------------------|---------------------------------|-----------------------------------|------------------------------|
| Cap. 1, página 13    | `spring-ai-starter-model-llama` | `spring-ai-starter-model-ollama`  | El artefacto `-llama` no existe en Maven Central. |
| Cap. 2, ejemplo de `ChatClient` con converter | Inyecta `converter.getFormat()` en la plantilla y además llama `.entity(converter)` | Solo `.entity(converter)` | Redundancia, no drift: `.entity(converter)` ya agrega el formato al final del mensaje de usuario (`ChatModelCallAdvisor`), igual en 1.0.0 y en 1.0.8 (código fuente verificado). Hacer las dos cosas manda las instrucciones dos veces. |
