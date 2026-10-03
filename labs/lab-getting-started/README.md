# lab-getting-started

Capítulo 1: *Getting Started with Spring AI*.

El módulo más pequeño del repo: una app Spring Boot sin servidor web que le hace una
pregunta a un modelo de chat, escribe la respuesta por stdout y termina.

Es mínimo a propósito. Su valor es ser el **ejemplo de control** del repositorio: cuando
algo falla en otro módulo, correr este separa "el problema es mi código" de "el problema
es Ollama". También es el primer candidato para instrumentar en el retrofit de
observabilidad (etapa 7b).

## Qué demuestra

`ChatModel` como abstracción portable. `GettingStartedApplication` pide un `ChatModel`
por inyección y llama `chatModel.call(prompt)`; en ningún lado nombra al proveedor.
Quién responde lo decide el starter del classpath y una property:

| Pieza | Dónde | Qué fija |
| --- | --- | --- |
| `spring-ai-starter-model-ollama` | `build.gradle.kts` (la única dependencia) | El proveedor |
| `spring.ai.ollama.chat.options.model` | `application.yml` | El modelo dentro del proveedor |

Es el mismo movimiento que Spring hizo con `DataSource` frente a los drivers JDBC: la
interfaz de dominio arriba, la autoconfiguración abajo y el proveedor como un detalle de
despliegue.

## Qué deliberadamente no hace

No usa `ChatClient`, advisors, prompt templates ni structured output. Todo eso llega en
los capítulos 2 y 3, y mezclarlo acá taparía la tesis. El prompt es un `String` literal
y la respuesta es otro `String`.

## Ejecución

```powershell
# El nombre del modelo debe coincidir exactamente con application.yml
ollama list

.\gradlew :labs:lab-getting-started:build
.\gradlew :labs:lab-getting-started:bootRun --console=plain

# Una sola entrada
ollama ps
```

Salida esperada (el proceso termina solo):

```text
User> In a single word, 'what is the answer to life, the universe, and everything?'

AI> 42.
```

Medido el 2026-10-03: la app arranca en unos 3 s y el `bootRun` completo tarda unos 30 s
(la mayor parte es Gradle). `ollama ps` muestra solo `llama3.2:3b` (2,6 GB, 100 % CPU).

## Modos de falla

| Síntoma | Causa | Fix |
| --- | --- | --- |
| `HTTP 404 - model 'llama3.2' not found` | Ollama resuelve por nombre exacto, no por prefijo | Copiar el nombre tal cual de `ollama list` |
| `Unable to start reactive web server ... no ReactiveWebServerFactory bean` | Se quitó `.web(WebApplicationType.NONE)`: el starter arrastra `spring-webflux`, Boot deduce una app reactiva y no encuentra servidor | Mantener `WebApplicationType.NONE`; es un CLI |
| La app responde pero el proceso **no termina** | Se quitó `NONE` *y además* hay `spring-boot-starter-web` en el classpath: Boot deduce SERVLET y Tomcat mantiene viva la JVM | Mismo fix |
| La respuesta es un párrafo y no `42` | `llama3.2:3b` no siempre respeta "in a single word" | No es un bug: es una diferencia de calidad de modelo (ver `docs/diferencias-libro.md`) |

El síntoma de quitar `NONE` depende del classpath. Con el classpath actual (solo
`spring-webflux`), falla al arrancar, verificado el 2026-10-03; es el mejor caso porque
no hay forma de no verlo. Si alguna vez se agrega `spring-boot-starter-web`, el síntoma
pasa a ser el cuelgue, que es más difícil de diagnosticar.

Con `pull-model-strategy: never`, un nombre de modelo equivocado falla rápido con 404 en
lugar de disparar una descarga de varios GB. Esto está verificado: se probó con
`llama3.2` y no se descargó nada.

## Test

`GettingStartedApplicationTests` levanta el contexto y verifica que la
autoconfiguración resuelva un `OllamaChatModel` con el modelo que dice `application.yml`.
No llama a Ollama: el runner se reemplaza por un mock (`@SpringBootTest` ejecuta los
`ApplicationRunner`), y construir el `OllamaChatModel` no usa la red. Para verificarlo,
el test pasó con `SPRING_AI_OLLAMA_BASEURL=http://localhost:1`.

## El recorrido del libro: de OpenAI a Ollama

El libro empieza con OpenAI (`gpt-4o-mini`) y al final del capítulo cambia a Ollama.
Acá ese camino se documenta pero no se ejecuta, por costo (D-008). Volver a OpenAI sería:

1. En `build.gradle.kts`, reemplazar `libs.spring.ai.starter.model.ollama` por
   `libs.spring.ai.starter.model.openai`.
2. En `application.yml`, reemplazar el bloque `spring.ai.ollama` por
   `spring.ai.openai.api-key: ${OPENAI_API_KEY}` y
   `spring.ai.openai.chat.options.model: gpt-4o-mini`.
3. Java: ningún cambio.

### Qué mitad del argumento queda probada

- **El modelo es configuración: probado.** Pasar
  `--spring.ai.ollama.chat.options.model=llama3.2` cambia el modelo pedido sin tocar
  Java, y Ollama responde con un 404 porque ese nombre no existe localmente. La
  property llega al proveedor tal cual.
- **El proveedor es una dependencia: documentado, no ejecutado.** Hace falta un segundo
  proveedor u otro modelo de chat. No se baja ahora para usarlo una sola vez: OpenAI
  entra por necesidad en la etapa 4, y la etapa 6 necesita dos modelos compitiendo.
  Ahí se ejercita esta mitad.

## Desviaciones

D-005 a D-010 en [`docs/diferencias-libro.md`](../../docs/diferencias-libro.md).
