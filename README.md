# Mariposa

Explorador de personajes de Rick and Morty construido con Kotlin y Jetpack Compose. El objetivo es demostrar una solución Android moderna, legible y testeable: lista paginada, búsqueda remota, detalle y favoritos persistentes.

## Requisitos funcionales

- Lista remota de personajes con paginación y búsqueda por nombre.
- Detalle navegable por `characterId`.
- Marcar y desmarcar favoritos persistentes.
- Pantalla de favoritos.
- Estados de carga, vacío, error y reintento donde aplican.

## Stack

- Kotlin, Coroutines, Flow y StateFlow.
- Jetpack Compose, Material 3, Dynamic Color y light/dark theme.
- Navigation Compose y Hilt.
- Retrofit, OkHttp y Kotlin Serialization.
- Room, Paging 3/Paging Compose y Coil Compose.
- JUnit, MockK, Turbine y Compose UI Testing.

El proyecto usa `minSdk 24`, Gradle Kotlin DSL y Version Catalog en `gradle/libs.versions.toml`.

## Arquitectura

Se usa un único módulo `:app` para mantener el alcance de la prueba técnica claro, rápido de construir y fácil de revisar. La separación por paquetes conserva límites que permiten modularizar después sin reescribir reglas de negocio:

```text
core/        componentes compartidos, theme y navegación
data/        API, DTOs, Room, mappers y repositorios
domain/      modelos, contratos de repositorio y casos de uso
feature/     list, detail y favorites; incluye adaptación a Paging Compose
di/          módulos Hilt
```

La dependencia conceptual es `presentation -> domain <- data`:

- `domain` no conoce Retrofit, Room, Compose ni Paging.
- DTOs y `FavoriteEntity` no escapan de `data`.
- Los mappers hacen DTO → dominio, Entity → dominio y dominio → Entity.
- Room es la fuente de verdad exclusiva de favoritos.

## Decisiones técnicas y trade-offs

### Un módulo ahora, módulos después

Separar desde el inicio en muchos módulos habría aumentado configuración Gradle, tiempos de build y complejidad de navegación sin aportar valor proporcional para tres features. Se preservan los límites internos (`domain` independiente y dependencias hacia adentro) para extraer `core`, `data`, `domain` o cada feature cuando crezcan el equipo, los tiempos de compilación o el aislamiento de ownership.

### Clean Architecture pragmática

`domain` contiene modelos Kotlin puros, contratos de repositorio y casos de uso. Los contratos viven ahí porque la capa que consume define lo que necesita; Retrofit, Room, DTOs y entidades permanecen en `data`. Los mappers explícitos evitan que detalles de transporte o persistencia contaminen la UI y hacen visible cada conversión.

No se introdujeron `BaseViewModel`, `BaseRepository`, `BaseUseCase`, un store MVI ni wrappers genéricos de resultados. Para este alcance, esas abstracciones ocultarían el flujo y crearían acoplamiento sin reutilización real.

### MVVM + UDF

Cada feature expone un `UiState` inmutable y recibe `UiAction`. Los ViewModels usan `StateFlow`; las Routes observan mediante `collectAsStateWithLifecycle()`. Las Screens reciben estado y callbacks, sin repositorios ni lógica de negocio. Los eventos puntuales —como mostrar “Deshacer”— se emiten separadamente para no convertir una notificación efímera en estado persistente.

Route conecta ViewModel, navegación y efectos; Screen es una función de estado y callbacks. Esta división reduce el acoplamiento a Navigation/Hilt y permite probar estados visuales de forma aislada.

### Estado restaurable

La búsqueda y la posición de la lista se guardan como valores primitivos en `SavedStateHandle`, no como tipos Compose. Al volver de detalle, o si Android recrea la pantalla, se recupera el contexto de exploración. `LazyListState` sigue siendo una preocupación de UI y únicamente traduce su posición a una acción del ViewModel.

### Paging y búsqueda

`CharacterRepository` expone páginas de dominio y convierte el HTTP 404 de una búsqueda en lista vacía. `CharacterPagingSource` y `Pager` pertenecen a la feature de lista, donde se adaptan esas páginas a Paging 3; así `domain` no depende de Paging.

`CharacterListViewModel` usa `debounce`, `distinctUntilChanged`, `flatMapLatest` y `cachedIn(viewModelScope)`: evita una solicitud por tecla, cancela el query anterior y comparte el flujo paginado durante la vida del ViewModel. Un error al cargar la siguiente página conserva los personajes ya visibles y ofrece reintento.

### Red, detalle e imágenes

Retrofit, OkHttp y Kotlin Serialization resuelven el contrato HTTP sin una capa de red genérica. Los fallos transitorios reciben reintentos acotados en el repositorio; no se envuelve cada llamada en `try/catch`. La lista conserva en memoria los personajes recibidos para abrir detalle sin otra petición durante scroll rápido; si no existe en caché, el detalle consulta la API.

La UI distingue `IOException` de un fallo genérico para comunicar falta de conexión sin afirmar que el servidor está caído. Coil intenta memoria y disco antes de red: sin conectividad conserva una imagen previamente cacheada y, si no existe, muestra un placeholder neutral en lugar de una imagen rota.

### Favoritos

Room es la única fuente de verdad de favoritos. `FavoriteDao` publica `Flow` de favoritos e IDs favoritos; el toggle se ejecuta mediante caso de uso y repositorio, y la UI reacciona al estado emitido por Room. Si la escritura falla, se ofrece reintento. Una operación exitosa muestra una confirmación breve con “Deshacer”, que reutiliza el mismo toggle sin duplicar reglas de persistencia.

No se guarda la lista remota completa en Room: el requerimiento exige persistir favoritos, no un modo offline total. Esto reduce complejidad y deja como mejora futura el cache remoto si el producto lo necesita.

### Navegación

Navigation Compose recibe únicamente `characterId`; no se serializan objetos de dominio entre destinos. `CharacterDetailViewModel` obtiene ese ID desde `SavedStateHandle`, lo que mantiene el detalle recuperable y testeable.

### UI

La app usa Material 3, Dynamic Color cuando Android lo permite y tema claro/oscuro. Coil carga imágenes con caché y placeholder. Las animaciones nativas se limitan al contexto de búsqueda y favorito para aportar feedback sin distraer. Los textos propios están centralizados en recursos, los valores de la API conservan su idioma original y las acciones principales incluyen semántica accesible.

No se añadió un observador global de conectividad: para esta prueba es suficiente clasificar el error de la operación que falló. Un monitor global agregaría estado y permisos/edge cases sin resolver por sí mismo una solicitud fallida.

## Ejecución

Requiere Android Studio con JDK 17 y dispositivo o emulador Android API 24+.

```bash
./gradlew assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/`.

## Testing y calidad

```bash
# Tests unitarios
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug

# Reporte HTML/XML de cobertura de tests unitarios
./gradlew :app:createDebugUnitTestCoverageReport

# Tests instrumentados / Compose con dispositivo o emulador conectado
./gradlew connectedDebugAndroidTest
```

El reporte de cobertura se genera en `app/build/reports/coverage/test/debug/`. No se aplica todavía un umbral: primero se busca visibilidad y una línea base útil, evitando que una métrica global incentive tests sin valor.

Cobertura incluida:

- `CharacterListViewModel`: debounce, último query y restauración de búsqueda/posición.
- `CharacterPagingSource`: éxito, error de red y error de deserialización.
- `CharacterDetailViewModel`: `characterId` desde `SavedStateHandle`, estado offline y feedback de favorito.
- `ToggleFavoriteUseCase`: delegación al repositorio.
- `CharacterRepositoryImpl`: mapeo antes de persistir el favorito.
- `FavoritesViewModel`: conserva el favorito fallido para reintentar y emite la acción de deshacer.
- `FavoritesScreenTest`: estado vacío de Compose.

La validación local del código confirma `testDebugUnitTest`, `assembleDebug` y `lintDebug`. El test instrumentado requiere un dispositivo o emulador desbloqueado; se ejecutó correctamente en el emulador `Medium_Phone_API_36.1`.

## Integración continua

El workflow [Android CI](.github/workflows/android-ci.yml) se ejecuta en cada push y pull request hacia `main`; también puede iniciarse manualmente. Con JDK 17 ejecuta tests unitarios, lint, `assembleDebug`, `assembleDebugAndroidTest` y el reporte nativo JaCoCo de cobertura. El reporte de cobertura se conserva 14 días como artefacto del run.

Las pruebas instrumentadas se mantienen fuera del CI base porque requieren un emulador gestionado; incorporarlo es una mejora razonable si el proyecto necesita esa señal en cada pull request.

## Guía de entrevista

Las preguntas, respuestas y alternativas discutibles de esta solución están en [docs/interview-questions.md](docs/interview-questions.md). Sirve para defender decisiones con sus límites, no para afirmar que existe una única arquitectura correcta.

## Trade-offs y mejoras futuras

- Modularizar `core`, `data`, `domain` y cada feature cuando el proyecto crezca.
- Añadir pruebas Room con base de datos en memoria y pruebas de navegación end-to-end.
- Añadir caché remoto si el producto requiere navegación offline más allá de favoritos.
- Ampliar recursos de idioma, revisar el nivel de logging de OkHttp antes de producción y evaluar un emulador gestionado para CI.
