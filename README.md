# Mariposa

Explorador de personajes de Rick and Morty construido con Kotlin y Jetpack Compose. Incluye lista paginada, búsqueda remota, detalle y favoritos persistentes.

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

Se usa un único módulo `:app` para mantener el alcance de la prueba técnica claro y rápido de construir, con límites internos listos para modularizar:

```text
core/        theme y navegación
data/        API, DTOs, PagingSource, Room, mappers y repositorios
domain/      modelos, contratos de repositorio y casos de uso
feature/     list, detail y favorites
di/          módulos Hilt
```

La dependencia conceptual es `presentation -> domain <- data`:

- `domain` no conoce Retrofit, Room, Compose ni Paging.
- DTOs y `FavoriteEntity` no escapan de `data`.
- Los mappers hacen DTO → dominio, Entity → dominio y dominio → Entity.
- Room es la fuente de verdad exclusiva de favoritos.

## Decisiones técnicas

### MVVM + UDF

Cada feature expone un `UiState` inmutable y recibe `UiAction`. Los ViewModels usan `StateFlow`; las Routes observan mediante `collectAsStateWithLifecycle()`. Las Screens reciben estado y callbacks, sin repositorios ni lógica de negocio.

### Paging y búsqueda

`CharacterRepository` expone páginas de dominio y convierte el HTTP 404 de una búsqueda en lista vacía. `CharacterPagingSource` y `Pager` pertenecen a la feature de lista, donde se adaptan esas páginas a Paging 3. `CharacterListViewModel` usa `debounce`, `distinctUntilChanged`, `flatMapLatest` y `cachedIn(viewModelScope)`, cancelando la búsqueda anterior al cambiar el query.

### Favoritos

`FavoriteDao` publica `Flow` de favoritos e IDs favoritos. El toggle se ejecuta mediante caso de uso y repositorio; la UI solo reacciona al estado emitido por Room. Si la escritura falla, la UI muestra un mensaje con opción de reintento.

### UI

La app usa Material 3, Coil para imágenes y animaciones Compose para el contexto de búsqueda y la transición Favorite/Unfavorite. Los textos visibles están centralizados en recursos y los estados/acciones principales incluyen semántica accesible.

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

# Tests instrumentados / Compose con dispositivo o emulador conectado
./gradlew connectedDebugAndroidTest
```

Cobertura incluida:

- `CharacterListViewModel`: debounce y último query.
- `CharacterPagingSource`: éxito, error de red y error de deserialización.
- `CharacterDetailViewModel`: `characterId` desde `SavedStateHandle` y fallo al actualizar favorito.
- `ToggleFavoriteUseCase`: delegación al repositorio.
- `CharacterRepositoryImpl`: mapeo antes de persistir el favorito.
- `FavoritesViewModel`: conserva el favorito fallido para reintentar.
- `FavoritesScreenTest`: estado vacío de Compose.

La última validación confirmó `testDebugUnitTest`, `assembleDebug`, `lintDebug` y `connectedDebugAndroidTest`. El test instrumentado se ejecutó correctamente en el emulador `Medium_Phone_API_36.1`.

## Integración continua

El workflow [Android CI](.github/workflows/android-ci.yml) se ejecuta en cada push y pull request hacia `main`; también puede iniciarse manualmente. Ejecuta `testDebugUnitTest`, `lintDebug` y `assembleDebug` con JDK 17. Las pruebas instrumentadas se mantienen fuera del CI base porque requieren un emulador gestionado.

## Trade-offs y mejoras futuras

- Modularizar `core`, `data`, `domain` y cada feature cuando el proyecto crezca.
- Añadir pruebas Room con base de datos en memoria y pruebas de navegación end-to-end.
- Ampliar recursos de idioma y revisar el nivel de logging de OkHttp antes de producción.
