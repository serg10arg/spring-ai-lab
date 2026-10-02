# Testing

Cómo ejercitar `spring-ai-lab`, organizado en niveles de coste creciente. Empezá
siempre por el nivel más bajo que pueda detectar el problema que buscás.

## Nivel 0 — Build y tests unitarios

Sin modelo, sin red, sin coste.

```powershell
.\gradlew build --console=plain
```

Cubre: compilación, configuración de Spring, tests unitarios con dobles de prueba.
No cubre: nada que dependa de la respuesta de un modelo.

## Nivel 1 — Modelos locales con Ollama

Sin coste por token. Coste real: RAM y tiempo de CPU.

Prerrequisitos: Ollama corriendo, `llama3.2:3b` y `nomic-embed-text` descargados,
`OLLAMA_MAX_LOADED_MODELS=1` activo.

```powershell
Invoke-RestMethod http://localhost:11434/api/tags
ollama ps
```

### Línea de base medida

Hardware: Intel i7-10510U, 8 GB RAM, gráficos integrados, sin GPU. Windows nativo.

| Métrica | Valor | Fecha |
| --- | --- | --- |
| Generación en frío, `llama3.2:3b` (prompt de una palabra) | 2,7 s | 2026-10-02 |
| Generación en caliente, `llama3.2:3b` (prompt de una palabra) | 0,44 s | 2026-10-02 |
| RAM residente `llama3.2:3b` (contexto 4096, 100% CPU) | 2,6 GB | 2026-10-02 |
| RAM residente `nomic-embed-text` (contexto 2048, 100% CPU) | 376 MB | 2026-10-02 |
| Dimensión del vector de `nomic-embed-text` | 768 | 2026-10-02 |

Notas de interpretación:

- La diferencia entre frío y caliente es el coste de cargar el modelo a RAM. Mientras
  `OLLAMA_KEEP_ALIVE=5m` esté activo, una segunda llamada dentro de la ventana paga
  solo el tiempo de inferencia.
- Los 2,6 GB residentes superan los 2,0 GB del archivo en disco: la diferencia es la
  caché de clave-valor del contexto, y crece si se eleva `num_ctx`.
- `ollama ps` debe mostrar **una sola** entrada en todo momento. Dos entradas indican
  que la política de un solo modelo no está activa.
- La dimensión 768 define el tipo de la columna vectorial en PGVector (etapa 5).

Si una respuesta del modelo es mala pero el código es correcto, el problema es de
calidad de modelo, no de API: verificá primero que la petición enviada sea la que
esperás antes de tocar código.

## Nivel 2 — Proveedores en la nube

Con coste por token. Disponible a partir de la etapa 4.

Antes de ejecutar cualquier prueba de este nivel, revisá el presupuesto estimado de la
etapa correspondiente. La transcripción y la síntesis de voz se facturan por duración
de audio, no por tokens, y se consumen rápido durante el desarrollo iterativo.
