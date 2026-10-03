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

## Erratas detectadas en el libro

| Ubicación            | Dice                            | Debería decir                     | Nota                         |
|----------------------|---------------------------------|-----------------------------------|------------------------------|
| Cap. 1, página 13    | `spring-ai-starter-model-llama` | `spring-ai-starter-model-ollama`  | El artefacto `-llama` no existe en Maven Central. |
