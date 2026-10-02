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

## Erratas detectadas en el libro

| Ubicación            | Dice                            | Debería decir                     | Nota                         |
|----------------------|---------------------------------|-----------------------------------|------------------------------|
| Cap. 1, página 13    | `spring-ai-starter-model-llama` | `spring-ai-starter-model-ollama`  | El artefacto `-llama` no existe en Maven Central. |
