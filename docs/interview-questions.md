# Guía de entrevista técnica

Este documento explica las decisiones de Mariposa con el razonamiento que se esperaría en una conversación técnica. No pretende convertir preferencias en reglas absolutas: cada respuesta indica cuándo podría cambiar.

## Arquitectura

### ¿Por qué iniciar con un solo módulo Gradle?

Porque el producto tiene tres features y un único equipo/ownership implícito. Varios módulos prematuros añaden configuración, APIs internas, ciclos de build y navegación más costosa de revisar. La separación `data` / `domain` / `feature` dentro de `:app` conserva los límites para extraer módulos cuando haya compilaciones lentas, equipos independientes o reutilización real.

### ¿Por qué el contrato de repositorio vive en `domain`?

La capa consumidora define las operaciones que necesita. `domain` declara `CharacterRepository`; `data` decide si la implementación usa Retrofit, Room, caché u otra fuente. Así la regla de negocio no depende del mecanismo de obtención de datos y puede probarse con un fake o MockK.

### ¿Por qué usar mappers explícitos en vez de reutilizar DTOs o entidades?

DTOs reflejan un contrato remoto cambiante y entidades reflejan almacenamiento. El modelo de dominio debe describir la app. Los mappers hacen visible la conversión y evitan que anotaciones de Serialization/Room lleguen a ViewModels o Composables. Para un prototipo de una sola capa podrían omitirse, pero no para una prueba que evalúa límites de arquitectura.

### ¿Por qué no hay BaseViewModel, BaseRepository o framework MVI?

No existe comportamiento compartido suficiente para justificar herencia o un reducer/store. Cada clase es pequeña y su flujo se entiende localmente. Una abstracción solo se incorporaría tras detectar duplicación estable en varias features, no antes.

## Estado y UI

### ¿Por qué MVVM con UDF y no MVI completo?

MVVM con `UiState` inmutable y `UiAction` ya proporciona un flujo unidireccional claro: Compose emite intención, el ViewModel coordina casos de uso y publica estado. Un MVI con reducer único aporta valor en máquinas de estado muy complejas; aquí agregaría ceremonía sin mejorar la legibilidad.

### ¿Por qué separar Route y Screen?

Route conoce Hilt, navegación y colecta efectos; Screen recibe datos y callbacks. Esto mantiene la UI reutilizable y permite probar estados sin instanciar un ViewModel o un `NavController`. La separación se usa donde aporta valor; no se crea una capa adicional para componentes triviales.

### ¿Por qué StateFlow y collectAsStateWithLifecycle en lugar de LiveData?

El stack usa Coroutines y Flow de extremo a extremo: Room publica Flow, los casos de uso lo preservan y StateFlow representa el estado actual. `collectAsStateWithLifecycle` suspende la colección fuera del estado visible apropiado. LiveData no aporta una ventaja en esta arquitectura y mezclar ambos modelos complica el flujo.

### ¿Por qué guardar query y scroll en SavedStateHandle?

La búsqueda y la posición forman parte del contexto que el usuario espera recuperar al volver de detalle o tras recreación. Se almacenan como `String` e `Int`, no como `LazyListState`, para no filtrar tipos Compose hacia el ViewModel. Para una pantalla sin navegación de ida y vuelta, `rememberSaveable` podría ser suficiente.

### ¿Por qué Snackbar con “Deshacer” usa el mismo toggle?

El repositorio encapsula si debe insertar o eliminar. Al deshacer, se invoca el mismo caso de uso con el personaje original, evitando duplicar operaciones opuestas en la UI. El mensaje es un evento efímero; no pertenece al `UiState` persistente.

## Datos, red y Paging

### ¿Por qué PagingSource está en la feature y no en domain?

Paging 3 es un mecanismo de presentación. El repositorio expone `CharacterPage`, un modelo de dominio simple; la feature lo adapta a `PagingSource` y `Pager`. Esto permite reemplazar la UI paginada por otra estrategia sin hacer que domain dependa de AndroidX Paging.

### ¿Por qué debounce, distinctUntilChanged y flatMapLatest para búsqueda?

`debounce` evita solicitar por cada pulsación, `distinctUntilChanged` evita repetir el mismo query y `flatMapLatest` cancela la búsqueda anterior cuando llega una nueva. Es importante que la cancelación ocurra en el flujo y no mediante flags manuales susceptibles a carreras.

### ¿Por qué un HTTP 404 de búsqueda se convierte en lista vacía?

La API usa 404 para comunicar que no hubo coincidencias. Para el usuario, eso no es una caída del sistema sino un estado vacío. Otros 4xx/5xx y errores de transporte sí se muestran como error/reintento.

### ¿Por qué hay reintentos limitados en el repositorio?

Fallos transitorios como `IOException`, 429 y 5xx pueden resolverse solos. Se aplica backoff acotado y se respeta `Retry-After` cuando existe; no se reintenta un 4xx de negocio ni se bloquea indefinidamente. El botón de reintento mantiene la decisión final en el usuario.

### ¿Por qué detalle puede abrirse sin otra llamada de red tras scroll rápido?

El repositorio conserva en memoria los personajes recibidos por Paging. Si el usuario ya vio una card, el detalle puede usar ese objeto y evitar un fallo o latencia extra. Si el personaje no está en memoria, se usa el endpoint de detalle. Es una caché de conveniencia, no una segunda fuente de verdad para favoritos.

### ¿Por qué no guardar la lista remota completa en Room?

El requerimiento pide persistir favoritos. Persistir también el catálogo exigiría política de expiración, invalidación, migraciones y definición de datos obsoletos. Se dejó como mejora futura porque no hay requisito de modo offline completo.

## UX y resiliencia

### ¿Por qué no hay un monitor global de conectividad?

El estado de red puede cambiar entre una comprobación y la petición. La señal confiable es el resultado de la operación: un `IOException` se comunica como falta de conexión y se ofrece reintento. Un monitor global se justificaría si varias pantallas requieren una experiencia offline continua o sincronización en segundo plano.

### ¿Qué ocurre con imágenes sin conexión?

Coil consulta caché de memoria y disco antes de red. Una imagen cacheada sigue visible; una no disponible muestra un placeholder de personaje, no una imagen rota ni un spinner infinito. El placeholder también cubre URLs inválidas, por lo que no se afirma que la causa exacta sea la red.

### ¿Por qué usar Material 3 y Dynamic Color?

Material 3 ofrece componentes accesibles y consistentes; Dynamic Color integra la app visualmente con Android 12+ cuando está disponible. El tema claro/oscuro mantiene una alternativa determinista para versiones o dispositivos sin color dinámico.

## Testing y entrega

### ¿Qué valida cada tipo de prueba?

Las unitarias validan ViewModels, PagingSource, casos de uso y repositorio sin dispositivo. MockK aísla dependencias y Turbine verifica emisiones Flow/eventos. El test Compose valida una semántica visible. Los tests instrumentados deben ejecutarse en dispositivo o emulador desbloqueado; Room en memoria y navegación end-to-end son las siguientes pruebas de mayor valor.

### ¿Por qué el CI no ejecuta pruebas instrumentadas?

El CI base prioriza feedback rápido y estable: tests unitarios, lint y ensamblado. Ejecutar instrumentados requiere configurar un emulador gestionado, aumenta tiempo/costo y puede incorporar inestabilidad. Si la app crece o la señal visual es crítica, se añadiría un job dedicado con emulador gestionado.

### ¿Qué mejorarías antes de producción?

Separaría módulos cuando exista una razón medible, agregaría caché remoto y política de expiración si se necesita offline, pruebas Room/navegación, observabilidad/analytics, internacionalización completa y una política de logging/seguridad de red. También evaluaría un job instrumentado en CI y pruebas de accesibilidad más amplias.
