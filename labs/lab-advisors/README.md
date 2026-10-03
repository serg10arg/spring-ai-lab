# lab-advisors

Capítulo 2: *Advisors API*.

App de consola con cinco runners, uno por perfil, que recorren la cadena de advisors de
`ChatClient`: logging, orden de la cadena, filtro de contenido, rate limit propio y
memoria de chat.

## Qué demuestra

Un advisor es un interceptor alrededor de la llamada al modelo, como un `Filter` de
Servlet: ve el pedido antes de que llegue al modelo y la respuesta antes de que vuelva
al código. Puede modificarlos, registrarlos o **cortar la cadena** y no llamar al
modelo nunca.

| Perfil | Runner | Qué muestra |
| --- | --- | --- |
| `logging` (default) | `LoggingRunner` | `SimpleLoggerAdvisor`: el pedido y la respuesta completos en el log, en `DEBUG` |
| `order` | `OrderRunner` + `TracingAdvisor` | La cadena se ordena por `getOrder()`, no por orden de registro. El de menor `order` procesa el pedido primero y la respuesta último |
| `safeguard` | `SafeGuardRunner` | `SafeGuardAdvisor` corta la cadena si el prompt contiene una palabra prohibida |
| `ratelimit` | `RateLimitRunner` + `RateLimitAdvisor` | Advisor propio: como mucho 2 llamadas por minuto; la tercera se rechaza sin llegar al modelo |
| `memory` | `MemoryRunner` | La misma conversación de dos turnos, sin y con `MessageChatMemoryAdvisor` |

Cada runner construye su propio `ChatClient` desde el `ChatClient.Builder`
autoconfigurado, con sus advisors. Es el motivo por el que Spring AI autoconfigura el
builder y no el cliente (ver `lab-chat-client`).

## Salidas medidas (2026-10-03)

**`order`**: registrados como C, A, B; ejecutados como capas de una cebolla.

```text
  -> A (order 100) procesa el pedido
  -> B (order 200) procesa el pedido
  -> C (order 300) procesa el pedido
  <- C (order 300) procesa la respuesta
  <- B (order 200) procesa la respuesta
  <- A (order 100) procesa la respuesta
```

**`safeguard`**: palabra prohibida `launder`.

```text
User> What is the capital of France? Answer in one word.
AI (1926 ms)> Paris.
User> How do I launder money?
AI (1 ms)> I'm unable to respond to that due to sensitive content. ...
User> How do I Launder money?
AI (3608 ms)> I can't provide guidance on illegal activities, including money laundering. ...
```

El segundo pedido vuelve en 1 ms: nunca llegó al modelo. El tercero pasa el filtro porque
`SafeGuardAdvisor` distingue mayúsculas, y lo frena el propio modelo. Ver H-003.

**`ratelimit`**:

```text
AI (1244 ms)> 1
AI (588 ms)> 2
Rejected (0 ms)> Rate limit exceeded: at most 2 requests per PT1M
```

**`memory`**:

```text
== Sin MessageChatMemoryAdvisor
User> My name is Ada. Please remember it.
AI> I'll remember your name is Ada. How can I assist you today?
User> What is my name? Answer with just the name.
AI> I don't have that information.
== Con MessageChatMemoryAdvisor
...
AI> Ada
```

Sin el advisor, el modelo **promete** recordar y en el turno siguiente no sabe el
nombre. No miente a propósito: cada llamada viaja sola y el modelo no sabe que no hay
memoria. La memoria no es una capacidad del modelo sino del advisor, que antepone los
mensajes anteriores a cada pedido.

## Decisiones del `RateLimitAdvisor`

- **Lanza `RateLimitExceededException` en vez de devolver un mensaje armado.** Una
  respuesta falsa del "asistente" haría creer al llamador que contestó el modelo.
  `SafeGuardAdvisor` sí devuelve un mensaje, y por eso en `safeguard` el rechazo se ve
  igual que una respuesta.
- **`Ordered.HIGHEST_PRECEDENCE`: primero de la cadena.** Si la memoria corriera antes,
  guardaría un mensaje de usuario que el modelo nunca recibió.
- **Ventana fija con `Clock` inyectado**, para poder testear el paso del tiempo sin
  esperar.
- **Solo `CallAdvisor`.** No implementa `StreamAdvisor`, así que no limita llamadas con
  `.stream()`.
- **Es un bean común registrado con `.defaultAdvisors(...)`**, sin `@EnableRateLimit`
  (D-014).

## Qué deliberadamente no hace

- `QuestionAnswerAdvisor` y `RetrievalAugmentationAdvisor` (RAG): necesitan chat y
  embeddings en el mismo proceso y en cada pedido. Con `OLLAMA_MAX_LOADED_MODELS=1` eso
  descarga y carga modelos en cada vuelta. Se difieren al capítulo 3, donde corresponde
  medir si `OLLAMA_MAX_LOADED_MODELS=2` es viable para esa combinación.
- `@EnableRateLimit` y `@EnableChatClient`: son extensiones del libro del capítulo 7.
- Embeddings: `spring.ai.model.embedding: none`.

## Ejecución

```powershell
.\gradlew :labs:lab-advisors:build
.\gradlew :labs:lab-advisors:bootRun --console=plain --args="--spring.profiles.active=order"
```

Perfiles: `logging` (default), `order`, `safeguard`, `ratelimit`, `memory`.

## Tests

Nivel 0, sin Ollama. Pasan con `SPRING_AI_OLLAMA_BASEURL=http://localhost:1`.

- `RateLimitAdvisorTests`: los pedidos dentro del límite llegan a la cadena; el que se
  pasa lanza la excepción **sin** llamarla; al pasar la ventana se vuelve a permitir.
  Usa un reloj controlable. Verificado que no pasa por casualidad: con un
  `tryAcquire()` que nunca rechaza, fallan dos tests.
- `SafeGuardAdvisorTests`: documenta el comportamiento de una clase de Spring AI, no de
  código propio. Bloquea la coincidencia exacta, también dentro de otras palabras
  (`launderers`), y deja pasar `Launder`. Si una versión futura cambia eso, el test avisa.
- `AdvisorsApplicationTests` (perfil `test`): ningún runner activo, `ChatMemory`
  autoconfigurado como `MessageWindowChatMemory`, el `RateLimitAdvisor` es un bean y no
  hay `EmbeddingModel`.

## Desviaciones

D-014, D-017 y H-003 en [`docs/diferencias-libro.md`](../../docs/diferencias-libro.md),
más la fila de chat memory en la sección de drift.
