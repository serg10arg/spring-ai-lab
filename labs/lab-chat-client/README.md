# lab-chat-client

Capítulo 2: *ChatClient API*.

App de consola con tres runners, uno por perfil, que recorren `ChatClient`: el mismo
prompt del capítulo 1 con rol explícito, una plantilla con argumentos y conversión de
la salida a un tipo Java, y una respuesta en streaming.

## Qué demuestra

`ChatClient` no es azúcar sintáctico sobre `ChatModel`. Es el punto donde entran el rol
del mensaje, la plantilla, las opciones por defecto, los advisors y la conversión de la
salida.

| Pieza | Dónde | Qué muestra |
| --- | --- | --- |
| `ChatClient.Builder` autoconfigurado + bean `ChatClient` propio | `ChatClientApplication` | Spring AI autoconfigura el *builder* (prototype), no el cliente: una app puede necesitar varios clientes con defaults distintos sobre el mismo `ChatModel` |
| `ChatClientCustomizer` con `ChatOptions` | `ChatClientApplication` | Configuración programática que pisa la del `yml` propiedad por propiedad. Fija `temperature: 0.0`; el modelo sigue saliendo del `yml` |
| Perfil `simple` | `SimplePromptRunner` | `prompt().user(u -> u.text(...))`: el ejemplo del capítulo 1 con rol explícito |
| Perfil `template` | `TemplatePromptRunner` + `ZonedDateTimeOutputConverter` | Plantilla con `params(Map)` y un `StructuredOutputConverter<ZonedDateTime>` propio |
| Perfil `stream` | `StreamingRunner` | `.stream().content()` sobre `StreamingChatModel`, bloqueando hasta el último fragmento porque es un CLI |

### Por qué la plantilla no tiene `{format}`

En Spring AI 1.0, `.entity(converter)` agrega `converter.getFormat()` al final del
mensaje de usuario por su cuenta (`ChatModelCallAdvisor`). Por eso la plantilla no tiene
un `{format}`: inyectarlo además por `params` mandaría las instrucciones de formato dos
veces. El comportamiento es el mismo en 1.0.0 y en 1.0.8, así que no es drift de API sino
una redundancia del ejemplo del libro; está en la sección de erratas de
`docs/diferencias-libro.md`.

## Qué deliberadamente no hace

- Structured output "en serio" (`BeanOutputConverter`, JSON schema): es tema del
  capítulo 3. Acá hay un solo converter, propio y mínimo.
- Advisors: tienen su propio módulo, `lab-advisors`.
- Endpoints REST: son `ApplicationRunner`, igual que en el libro.
- Embeddings: `spring.ai.model.embedding: none` evita que exista el bean, así que este
  módulo nunca puede pedir `nomic-embed-text`.

## Ejecución

```powershell
.\gradlew :labs:lab-chat-client:build

# simple es el perfil por defecto
.\gradlew :labs:lab-chat-client:bootRun --console=plain
.\gradlew :labs:lab-chat-client:bootRun --console=plain --args="--spring.profiles.active=template"
.\gradlew :labs:lab-chat-client:bootRun --console=plain --args="--spring.profiles.active=stream"
```

En IntelliJ: una run configuration de Gradle por perfil, con el `--args` correspondiente.

Salidas medidas el 2026-10-03 (unos 21 s por corrida desde el jar, incluido el arranque):

```text
# simple
AI> 42.

# template
AI> Sat, 3 Oct 2026 04:00:00 +0900
Expected> Sat, 3 Oct 2026 23:00:00 +0900
Same instant> false
```

La respuesta de `template` está **mal y es así a propósito**: el runner no la rescata.
Ver H-002 en `docs/diferencias-libro.md`, con la comparación entre temperatura 0.0 y 0.8.

## Tests

Nivel 0, sin Ollama. Pasan con `SPRING_AI_OLLAMA_BASEURL=http://localhost:1`.

- `ChatClientApplicationTests` (perfil `test`, ningún runner activo): el `ChatClient`
  se construye y no existe ningún `EmbeddingModel`. Verificado que el segundo test no
  pasa por casualidad: falla con `SPRING_AI_MODEL_EMBEDDING=ollama`.
- `ZonedDateTimeOutputConverterTests`: parsea RFC-1123 con offset, y rechaza respuestas
  con texto extra y abreviaturas de zona como `JST`, que RFC-1123 no admite.

## Desviaciones

D-011 a D-013, H-002, la sección de drift de API y la errata del `{format}` en
[`docs/diferencias-libro.md`](../../docs/diferencias-libro.md).
