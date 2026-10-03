# spring-ai-lab

Laboratorio de aplicaciones Java con IA generativa. Tres aplicaciones independientes
—traducción multilingüe en tiempo real, identificación de canciones por huella de audio,
y dos modelos de lenguaje compitiendo en un juego de tablero— más una plataforma
transversal de observabilidad y evaluación que las tres comparten. Construido sobre
Spring AI, con modelos locales por defecto y proveedores en la nube solo donde la
capacidad lo exige.

El proyecto sigue el libro *Building Intelligent Applications with Spring AI*
(John Blum, Packt, 2026), pero no replica su estructura: donde el libro entrega
proyectos sueltos por capítulo, este repositorio extrae lo transversal a un módulo
propio y lo aplica hacia atrás sobre las tres aplicaciones.

## Stack tecnológico

| Tecnología | Versión | Rol en el sistema |
| --- | --- | --- |
| Java | Amazon Corretto 21.0.12 | Lenguaje y runtime de todos los módulos |
| Gradle | 8.14.3 (wrapper) | Build del monorepo multi-módulo |
| Spring Boot | 3.5.16 | Autoconfiguración, inyección de dependencias, servidor web |
| Spring AI | 1.0.8 | Abstracciones sobre modelos de IA: `ChatClient`, `EmbeddingModel`, `VectorStore`, advisors |
| Ollama | 0.35.0 | Runtime local de modelos, sin coste por token |
| `llama3.2:3b` | — | Modelo de chat por defecto. 2,6 GB residentes, contexto 4096 |
| `nomic-embed-text` | — | Modelo de embeddings de texto. Vectores de 768 dimensiones |
| PostgreSQL + PGVector | — | Almacén vectorial persistente (etapa 5, vía Docker) |
| OpenAI | — | Proveedor en la nube. Obligatorio para transcripción y síntesis de voz (etapa 4) y como segundo competidor (etapa 6) |

## Arquitectura

El diagrama de arquitectura vive en `docs/images/` y se construye de forma incremental:
cada etapa agrega el módulo que le corresponde y la prosa que recorre el flujo de una
petición de punta a punta nombrando clases reales.

Estado actual: dos módulos de consola sobre el starter de Ollama y `llama3.2:3b`.
`labs/lab-getting-started` inyecta `ChatModel` directamente; `labs/lab-chat-client`
construye un `ChatClient` sobre ese mismo `ChatModel` a partir del builder
autoconfigurado. La
estructura de carpetas (`labs/`, `apps/`, `platform/`, `agents/`) refleja la
organización prevista, documentada en la tabla de módulos.

## Módulos

| Módulo | Capa o rol | Responsabilidad | Estado |
| --- | --- | --- | --- |
| `labs/lab-getting-started` | Referencia | Ejemplo de control: `ChatModel` portable sobre Ollama, sin servidor web | Completado |
| `labs/lab-chat-client` | Referencia | `ChatClient` sobre `ChatModel`: builder autoconfigurado, `ChatClientCustomizer`, plantillas, converter propio, streaming | Completado |
| `labs/lab-advisors` | Referencia | La cadena de advisors: logging, safeguard, memoria de chat y un `RateLimitAdvisor` propio | Pendiente |
| `labs/lab-embeddings` | Referencia | `EmbeddingModel`, `SimpleVectorStore`, búsqueda por similitud y chunking | Pendiente |
| `labs/lab-advanced-api` | Referencia | Streaming, prompt templates, structured output, procesamiento de documentos, function calling | Pendiente |
| `apps/polyglot-chat` | Aplicación | Chat web con traducción en tiempo real, transcripción de voz y síntesis de voz | Pendiente |
| `apps/beat-shazam` | Aplicación | Identificación de canciones mediante huella de audio y búsqueda por similitud vectorial | Pendiente |
| `apps/connect4-arena` | Aplicación | Dos modelos de lenguaje compitiendo sobre un tablero compartido | Pendiente |
| `platform/ai-platform` | Plataforma transversal | Observabilidad, evaluadores, estimación de tokens, extensiones reutilizables de Spring AI | Pendiente |
| `agents/travel-agent` | Agente | Workflows agénticos con servidor y cliente MCP | Pendiente |

## Decisiones técnicas

- **Monorepo Gradle en lugar de proyectos Maven independientes.** El capítulo 7 del
  libro produce piezas transversales —observabilidad, evaluadores, divisores de texto—
  que quedan encerradas dentro de sus propios ejemplos. Un monorepo permite extraerlas a
  `platform/ai-platform` y que las tres aplicaciones dependan de ellas. Problema que
  evita: duplicar la capa transversal en tres copias que divergen.

- **Gradle `platform()` y version catalog en lugar del plugin
  `io.spring.dependency-management`.** Ese plugin resuelve un hueco que Gradle ya cubre
  de forma nativa con `platform()`. Las versiones viven en `gradle/libs.versions.toml`,
  en un solo lugar para los siete módulos. Problema que evita: un plugin de terceros más
  en la cadena de compatibilidad, y versiones de Spring AI divergiendo entre módulos.

- **Gradle 8.14.3 y no 9.x.** El soporte de Gradle 9 se incorporó como novedad de Spring
  Boot 4.0; la documentación del plugin de la línea 3.5 declara soporte para Gradle 7.x
  y 8.x. Como el plugin de Spring Boot solo valida una versión mínima de Gradle, una 9.x
  arranca sin error y falla más tarde en tareas concretas, que es el peor modo de falla
  posible. Problema que evita: fallos de build atribuibles a una incompatibilidad de
  herramienta y diagnosticados como bugs de la aplicación.

- **Spring Boot 3.5.16 pese al fin de soporte OSS de la línea 3.5 (30/06/2026).**
  Spring AI 1.0.8 exige la línea Spring Boot 3.x. La alternativa —Spring Boot 4 sobre
  Spring Framework 7— obliga a migrar a Spring AI 2.0, que reorganiza el tool calling,
  los advisors y MCP. Decisión consciente, con la migración documentada como trabajo
  futuro. Problema que evita: estudiar una API mientras se migra a otra distinta.

- **Java 21 en lugar del 17 que usa el libro.** Ambas son LTS y Spring Boot 3.5 soporta
  las dos. El pattern matching de `switch` y los patrones de registro simplifican el
  despacho de respuestas estructuradas de los capítulos 4 y 6. Problema que evita:
  cadenas de `if`/`instanceof` en código que es pieza de portfolio.

- **Ollama como proveedor por defecto, nube solo donde la capacidad lo exige.** El
  hardware de desarrollo tiene 8 GB de RAM y no tiene GPU. Los modelos locales cubren
  chat y embeddings de texto sin coste por token; la transcripción y la síntesis de voz
  no tienen equivalente local vía Spring AI y obligan a un proveedor en la nube en la
  etapa 4. Problema que evita: coste recurrente en las etapas donde no aporta nada, y
  descubrir en mitad de una etapa que la capacidad no existe localmente.

- **Política de un solo modelo cargado (`OLLAMA_MAX_LOADED_MODELS=1`).** Con 8 GB, dos
  modelos simultáneos en memoria llevan el sistema a paginar. La restricción está
  verificada: tras generar con `llama3.2:3b` y embeber con `nomic-embed-text`,
  `ollama ps` muestra una única entrada. Problema que evita: latencias de decenas de
  segundos por paginación, diagnosticadas como lentitud del modelo.

- **Autoría asistida por IA, declarada acá y no en cada commit.** El código y la
  documentación se escriben con asistencia de herramientas de IA (Claude Code). Las
  decisiones, la verificación y la responsabilidad sobre el resultado son del autor.
  Los commits no llevan trailer `Co-Authored-By`; esta declaración lo reemplaza.
  Problema que evita: atribución ambigua sin ensuciar el historial de Git.

## Ejecución

### Prerrequisitos

- Amazon Corretto 21 con `JAVA_HOME` configurado
- Ollama instalado, con `llama3.2:3b` y `nomic-embed-text` descargados
- Variables de entorno de usuario: `OLLAMA_MAX_LOADED_MODELS=1`, `OLLAMA_KEEP_ALIVE=5m`
- Docker Desktop (solo a partir de la etapa 5)

Los comandos están escritos para PowerShell en Windows.

### Nivel 0 — Verificar el entorno de build (sin coste, sin modelo)

```powershell
# Confirma la version de Gradle y que la JVM sea Corretto 21
.\gradlew --version

# Lista los modulos activos del monorepo
.\gradlew projects

# Compila y ejecuta los tests de todos los modulos
.\gradlew build --console=plain
```

### Nivel 1 — Verificar los modelos locales (sin coste)

```powershell
# El servidor de Ollama responde y los modelos estan descargados
Invoke-RestMethod http://localhost:11434/api/tags

# Generacion de texto
$gen = @{ model="llama3.2:3b"; prompt="hola"; stream=$false } | ConvertTo-Json
(Invoke-RestMethod http://localhost:11434/api/generate -Method Post -Body $gen -ContentType "application/json").response

# Dimension del vector de embeddings (debe ser 768)
$emb = @{ model="nomic-embed-text"; prompt="hola mundo" } | ConvertTo-Json
(Invoke-RestMethod http://localhost:11434/api/embeddings -Method Post -Body $emb -ContentType "application/json").embedding.Count

# Un solo modelo cargado a la vez
ollama ps
```

```powershell
# Ejemplo de control: una pregunta a ChatModel, respuesta por stdout, y el proceso termina
.\gradlew :labs:lab-getting-started:bootRun --console=plain
```

### Nivel 2 — Proveedores en la nube (con coste)

Disponible a partir de la etapa 4. Requiere claves de API y presupuesto explícito;
el detalle está en `docs/TESTING.md`.

## Estado del proyecto

| Etapa | Cap. | Módulo | Incremento | Estado |
| --- | --- | --- | --- | --- |
| 0 | — | raíz | Monorepo Gradle, convenciones, entorno Ollama medido | Completada |
| 1 | 1 | `labs` | Primera app Spring AI y cambio de proveedor en un paso | Completada |
| 2 | 2 | `lab-chat-client`, `lab-advisors`, `lab-embeddings` | `ChatClient`, advisors propios, embeddings, `SimpleVectorStore` | En curso |
| 3 | 3 | `lab-advanced-api` | Streaming, prompt templates, structured output, documentos, function calling | Pendiente |
| 4 | 4 | `polyglot-chat` | Traducción en tiempo real, transcripción y síntesis de voz en un chat web | Pendiente |
| 5 | 5 | `beat-shazam` | Java Sound API, `EmbeddingModel` propio por huella de audio, PGVector | Pendiente |
| 6 | 6 | `connect4-arena` | Dos modelos compitiendo sobre un tablero compartido | Pendiente |
| 7 | 7 | `ai-platform` | Chat options, Metadata API, observabilidad, evaluadores, extensiones | Pendiente |
| 7b | 7 | todas | Retrofit: la plataforma aplicada a las tres aplicaciones | Pendiente |
| 8 | 8 | `travel-agent` | Workflows agénticos, servidor y cliente MCP | Pendiente |
| 9 | 9 | `docs` | Ensayo de criterio: impacto, ética, límites. Sin código | Pendiente |
| 10 | — | raíz | Hardening: perfiles, CI, docker-compose, demo, README final | Pendiente |
