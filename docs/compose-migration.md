# Migración XML → Jetpack Compose

Estrategia incremental "bottom-up" con interop siguiendo la guía oficial de Google.
Cada fase compila + tests pasan + validación manual antes de continuar con la siguiente.

## Estado de las fases

| Fase | Descripción                                              | Estado       |
|------|----------------------------------------------------------|--------------|
| 0    | Infraestructura Compose + Theme Material 3               | ✅ Completa  |
| 1    | Lista de Reportes (ComposeView interop) + cleanup + test | ✅ Completa  |
| 2    | Lista de Sismos + permiso notificaciones                 | ✅ Completa  |
| 3    | Detalle de sismo (mapa + ads con AndroidView)            | ✅ Completa  |
| 4    | Ajustes (SharedPrefUtil → Compose)                       | ✅ Completa  |
| 5    | Single-activity con Navigation Compose                   | ⏳ Pendiente |
| 6    | Limpieza final (ViewBinding, XMLs, adapters huérfanos)   | ⏳ Pendiente |

---

## Fase 0 — Infraestructura (completada)

**Qué se hizo:**
- `gradle/libs.versions.toml`: agregadas versiones `compose-bom`, `kotlin`, plugin `kotlin-compose`,
  librerías `androidx-compose-*`, `koin-androidx-compose`, `coil-compose`.
- `build.gradle.kts` (raíz): declarado `kotlin.compose` plugin con `apply false`.
- `app/build.gradle.kts`: aplicado `kotlin.compose`, habilitado `compose = true` en `buildFeatures`,
  agregadas dependencias Compose BOM.
- Creado paquete `core/ui/theme/` con `Color.kt`, `Type.kt`, `Theme.kt` (Material 3).
  - `Color.kt` porta la paleta completa de `values/colors.xml` incluyendo escala de magnitudes.
  - `Theme.kt` soporta modo claro y oscuro alineado con `values-night/colors.xml`.

**Nota sobre AGP 9.x:** el plugin `kotlin.android` NO es necesario (AGP 9+ lo incluye internamente).
Solo se necesita `kotlin.compose`.

---

## Fase 1 — Lista de Reportes (completada)

**Estrategia:** `ReportsFragment` se mantiene como contenedor para el ViewPager2 existente.
Su `onCreateView` ahora devuelve un `ComposeView` en lugar de inflar el XML.
ViewModel y toda la capa de datos/dominio **no cambiaron**.

**Archivos nuevos:**
- `reports_feature/ui/compose/ReportCard.kt` — card M3 portando `card_view_reports.xml`
- `reports_feature/ui/compose/ReportsScreen.kt` — pantalla completa con estados loading/error/lista

**Archivos modificados:**
- `reports_feature/ui/ReportsFragment.kt` — reemplazado ViewBinding por ComposeView
- `reports_feature/di/ReportModule.kt` — eliminado `ReportAdapter` (ya no se usa)

**Archivos borrados (cleanup completado):**
- `reports_feature/ui/ReportAdapter.kt`
- `res/layout/card_view_reports.xml`
- `res/layout/fragment_reports.xml`

**Test instrumentado migrado:** `ReportsFragmentTest` pasó de Espresso (RecyclerView/IDs)
a Compose testing (`createEmptyComposeRule` + `onNodeWithText`). Requiere
`androidx-compose-ui-test-junit4` (androidTest) y `ui-test-manifest` (debug).
Ambos tests verdes en dispositivo.

**Patrón de paginación en Compose** (con guard de tamaño de página):
```kotlin
LaunchedEffect(listState) {
    snapshotFlow { listState.layoutInfo }
        .map { info ->
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@map false
            val total = info.totalItemsCount
            // total >= QUERY_PAGE_SIZE replica el guard del RecyclerView original:
            // sin él, la lista auto-pagina apenas el fondo es visible (sin scroll del
            // usuario), recargando datos y, con keys duplicadas, crasheando el LazyColumn.
            !isLoading && !isLastPage && total >= QUERY_PAGE_SIZE && lastVisible >= total - 1
        }
        .distinctUntilChanged()
        .collect { shouldLoad -> if (shouldLoad) onLoadMore() }
}
```

> ⚠️ **Lección para Fase 2+:** el `RecyclerView.OnScrollListener` original tenía dos guards
> que hay que replicar: `totalItemCount >= QUERY_PAGE_SIZE` y el flag `isScrolling`. Omitirlos
> hace que la lista auto-pagine en el primer layout. El guard de página es el mínimo imprescindible.

---

## Fase 2 — Lista de Sismos (completada)

Misma estrategia que Fase 1: `QuakeFragment.onCreateView` devuelve un `ComposeView`
(`DisposeOnViewTreeLifecycleDestroyed`). ViewModel y capa de datos sin cambios.

**Archivos nuevos:**
- `quake_feature/ui/compose/QuakeCard.kt` — card M3 portando `card_view_quake.xml`
- `quake_feature/ui/compose/QuakeScreen.kt` — pantalla con estados + `NotificationPermissionCard`

**Archivos modificados:**
- `quake_feature/ui/QuakeFragment.kt` — ComposeView (menú vía `configOptionsMenu(fragmentIndex = 1)`)
- `quake_feature/di/QuakeModule.kt` — eliminado `QuakeAdapter`

**Archivos borrados:** `QuakeAdapter.kt`, `card_view_quake.xml`, `fragment_quake.xml`.

**Decisiones clave (reutilizables en Fase 3):**
- **Función pura para Compose:** se creó `quakeTimeText(context, quake, isShortVersion)` en
  `ViewsExt.kt` como equivalente puro de la extension `TextView.timeToText`. Las composables NO
  deben llamar extensions de `View` — se extrae la lógica a una función que devuelve `String`.
- **Color de magnitud:** `colorResource(getMagnitudeColor(quake.magnitude, false))`.
- **Círculo de magnitud:** `background_magnitude_circle_shape` (rect con esquinas) escalado a 50dp →
  `RoundedCornerShape(12.dp)`, no `CircleShape`. El badge verificado (layer-list) se compone a mano
  (óvalo blanco + ícono) porque `painterResource` no soporta layer-list.
- **Permiso notificaciones (Android 13+):** `NotificationPermissionCard` con
  `rememberLauncherForActivityResult(RequestPermission())` + re-chequeo en `ON_RESUME` vía
  `LifecycleEventObserver` (la tarjeta desaparece al volver de Ajustes del sistema).

**Test instrumentado:** `QuakeFragmentTest` migrado a Compose testing (mismo patrón que Fase 1).

---

## Fase 3 — Detalle de sismo (próxima)

`QuakeDetailsActivity` es la pantalla más compleja: combina **3 vistas Android que NO se migran a
Compose puro** (mapa con animaciones, ad nativo) más una tarjeta de datos que sí se porta. El objetivo
es dejar la `Activity` como host delgado (`setContent { }`) sin tocar todavía la navegación (eso es
Fase 5). El sismo sigue llegando por `intent extra` (`Quake` parcelable) — no se introduce ViewModel
nuevo.

### Estructura objetivo

```
QuakeDetailsActivity (setContent)
└─ LastQuakeChileTheme
   └─ QuakeDetailScreen(quake, isSnapshotRequest, onBack, onShare)
      └─ Scaffold(topBar = TopAppBar)            // título = ciudad, back, menú capas
         └─ Column (verticalScroll)              // reemplaza NestedScrollView
            ├─ QuakeMap(quake)                   // AndroidView(MapView) — interop
            ├─ NativeAdCard()                    // AndroidView(NativeAdView) — interop
            └─ QuakeDetailCard(quake)            // Compose puro
         + FloatingActionButton(onShare)         // botón compartir (snapshot)
```

### 1. Tarjeta de detalle → Compose puro (`QuakeDetailCard`)

Porta `card_view_quake_detail.xml`. Reutiliza lo de Fase 2:
- **Encabezado** (círculo magnitud + badge verificado + ciudad + hora + referencia + ícono sensible)
  es idéntico al de `QuakeCard`. → **Extraer un composable compartido** `MagnitudeHeader` /
  `MagnitudeCircle` a `core/ui/components/` y consumirlo desde `QuakeCard` y `QuakeDetailCard`
  (DRY; evita duplicar la lógica del badge layer-list).
- **Grid inferior** (fecha/hora, coordenadas DMS, profundidad, escala) → `Row` con dos `Column`
  separadas por el guideline al 50%. Cada celda: ícono + título + valor.
- **Funciones puras pendientes de crear en `ViewsExt.kt`** (mismo patrón que `quakeTimeText`):
  - `quakeScaleText(context, scale)` ← equivalente de `TextView.setScale`
  - `coordinateToDMS(context, coordinate)` ← equivalente de `TextView.formatDMS`
  - Fecha/hora completa: `it.localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))`
    se puede calcular directo en la composable.

### 2. Mapa → `AndroidView(MapView)` con ciclo de vida (interop obligatorio)

**Buena práctica:** crear un helper reutilizable `rememberMapViewWithLifecycle()` en
`core/ui/map/` que:
- `remember { MapView(context).apply { onCreate(null) } }`
- observa el `LifecycleOwner` con un `DisposableEffect` y reenvía
  `onStart/onResume/onPause/onStop/onDestroy` + `onLowMemory` al `MapView`.
- en `onDispose` llama `onDestroy` y **cancela los `ValueAnimator`** de los círculos.

Este helper se reutiliza tal cual en la migración del mapa de Fase 5.

La lógica de `onMapReady` (configuración de gestos, `setNightMode`, `configMapType`, los 4 `addCircle`
y los dos `ValueAnimator` animados) se mueve **sin cambios** dentro de `getMapAsync { }`, llamado desde
el `factory`/`LaunchedEffect`. Los animadores se guardan en `remember` para poder cancelarlos en
`onDispose`.

> ⚠️ `onSaveInstanceState`: en interop con `setContent` no hay el `Bundle` del `MapView` que tenía la
> Activity XML. Para Fase 3 se acepta recrear el mapa en cambios de configuración (el sismo viene del
> intent, no hay estado que perder). Documentar esta diferencia respecto al XML original.

### 3. Anuncio nativo → `AndroidView(NativeAdView)` (interop obligatorio)

- Mantener `loadNativeAd()` / `populateNativeAdView()` prácticamente igual, pero disparados desde la
  composable. El `NativeAdView` se infla de `R.layout.ad_small_template` dentro del `factory`.
- **Ciclo de vida del `NativeAd`:** `DisposableEffect(Unit) { onDispose { nativeAd?.destroy() } }`
  para no filtrar el ad (hoy lo hace `onDestroy` de la Activity).
- Estado de visibilidad (`hideAdBanner`) → `var adLoaded by remember { mutableStateOf(false) }`;
  la tarjeta solo se compone si el ad cargó (reemplaza `cv_native_ad.visibility`).
- `MobileAds.initialize` sigue en un coroutine (`LaunchedEffect`).

### 4. Toolbar + compartir + menú de capas

- `Scaffold` + `TopAppBar` M3: título = `quake.city`, navegación back (`onBack { finish() }`),
  acción de menú "capas".
- **FAB compartir:** `makeSnapshot(googleMap, quake)` necesita la referencia al `GoogleMap` → guardarla
  en un `remember { mutableStateOf<GoogleMap?>(null) }` seteado en `onMapReady`. El `onClick` del FAB
  invoca `context.makeSnapshot(map, quake)`. Replicar también el `isSnapshotRequest` (delay 1s →
  snapshot automático) con un `LaunchedEffect(mapReady)`.
- **Diálogo de capas (`MapTerrainDialogFragment`):** dos opciones —
  (a) mantenerlo como `DialogFragment` vía `supportFragmentManager` (interop, menos trabajo), o
  (b) migrarlo a un `AlertDialog`/`ModalBottomSheet` de Compose con estado `rememberSaveable`.
  **Recomendado:** (b) por coherencia, pero es aislable en su propio commit; si se prioriza tamaño de
  PR, dejar (a) y migrar el diálogo en Fase 4/5.

### Archivos a crear

- `quake_feature/ui/compose/QuakeDetailScreen.kt` — Scaffold + Column scrollable + FAB
- `quake_feature/ui/compose/QuakeDetailCard.kt` — tarjeta de datos (Compose puro)
- `quake_feature/ui/compose/QuakeMap.kt` — `AndroidView` del mapa + lógica de círculos
- `quake_feature/ui/compose/NativeAdCard.kt` — `AndroidView` del ad nativo
- `core/ui/map/MapViewLifecycle.kt` — `rememberMapViewWithLifecycle()` (reutilizable Fase 5)
- `core/ui/components/MagnitudeHeader.kt` — composable compartido extraído de `QuakeCard`
- (opcional) `quake_feature/ui/compose/MapTerrainDialog.kt` — diálogo de capas en Compose

### Archivos a modificar

- `quake_feature/ui/QuakeDetailsActivity.kt` → host delgado con `setContent { }`; se eliminan
  ViewBinding, overrides de ciclo de vida del `MapView` y `MenuProvider` (migran a Compose)
- `core/utils/views/ViewsExt.kt` → agregar `quakeScaleText` y `coordinateToDMS` (funciones puras)
- `QuakeCard.kt` → consumir `MagnitudeHeader` compartido

### Archivos a borrar (cleanup al cerrar la fase)

- `res/layout/activity_quake_details.xml`
- `res/layout/card_view_quake_detail.xml`
- `res/layout/card_view_mapview.xml`
- `res/menu/menu_quake_details.xml` (si se migra el menú a Compose)

> ⚠️ **No se borran** `ad_small_template.xml` (se sigue inflando vía `AndroidView`) ni los layouts del
> diálogo de capas si se elige la opción (a).

### Buenas prácticas transversales de la fase

- **Composables stateless + state hoisting:** `QuakeDetailScreen` recibe `quake`, `onBack`, `onShare`
  como parámetros; nada de lógica de Activity dentro de las composables de UI.
- **`@Preview`** para `QuakeDetailCard` (claro/oscuro, verificado/sensible) — el mapa y el ad no se
  previsualizan (dependen de servicios), se aíslan en sus propios composables interop.
- **Interop limpio:** cada `AndroidView` en su propio composable, con `factory` + `update` separados y
  liberación de recursos en `DisposableEffect`.
- **Verificación de la fase:** `assembleDevDebug` + `testDevDebugUnitTest` + validación manual del
  detalle (animación de círculos, modo noche del mapa, carga/ocultar ad, compartir snapshot, cambio de
  tipo de mapa). Migrar/crear test instrumentado de la pantalla de detalle si existía cobertura previa.

---

## Fase 4 — Ajustes (completada)

`SettingsActivity` queda como host delgado (`setContent`). `PreferenceFragmentCompat` y su
`OnSharedPreferenceChangeListener` se eliminan. La pantalla `SettingsScreen` lee/escribe
directamente en `SharedPrefUtil`, fuente única de verdad compartida con el sistema de
notificaciones.

**Archivos nuevos:**
- `core/ui/compose/SettingsScreen.kt` — pantalla Compose con secciones notificaciones,
  modo noche (API < Q), ads consent (condicional), y acerca de.

**Archivos modificados:**
- `core/ui/SettingsActivity.kt` → host delgado con `setContent`; lógica de modo noche y
  consent form como callbacks.
- `core/services/NightModeService.kt` → lee de `SharedPrefUtil` en vez de default prefs,
  con fallback de migración via `readBoolMigrating`.
- `core/utils/SharedPrefUtil.kt` → añadidos `readBoolMigrating` y `readStringMigrating`
  para migración transparente de valores persistidos por el antiguo `PreferenceFragmentCompat`.
- `core/utils/DataModelExt.kt` → añadidos `openPrivacyPolicy()` y `sendContactEmail()`
  como extensiones de `Context`, moviendo los intents de la Activity a funciones puras.
- `core/utils/LifecycleExt.kt` → propaga `sharedPrefUtil` a `NightModeService`.
- `core/ui/MainActivity.kt` → eliminada llamada a `PreferenceManager.setDefaultValues`
  (ya no hay `PreferenceFragment`; defaults son explícitos en cada llamada).

**Archivos borrados:**
- `res/layout/settings_activity.xml`
- `res/xml/root_preferences.xml`

**Decisiones clave:**
- **Fuente única de almacenamiento:** Todas las lecturas y escrituras van a `SharedPrefUtil`
  (`"lastquakechile"`). El antiguo flujo tenía doble archivo: el `PreferenceFragmentCompat`
  escribía en default-prefs y un listener espejaba manualmente a `SharedPrefUtil`. Compose
  lo unifica.
- **Migración transparente:** `readBoolMigrating`/`readStringMigrating` leen de
  `SharedPrefUtil` con fallback a default-prefs, por lo que usuarios con valores persistidos
  antes de la migración no pierden sus preferencias.
- **Bug suscripción corregido:** El switch de alertas persiste en `SharedPrefUtil` bajo la
  clave `pref_suscrito_quake`, donde `setUpNotificationService` la lee en cada arranque.
  Antes, el switch escribía solo en default-prefs y un OFF se perdía al reiniciar.
- **Diálogo magnitud mínima:** Reemplaza el `EditTextPreference` por un `AlertDialog` de
  Compose con `OutlinedTextField` y validación `toDoubleOrNull()` antes de persistir.
- **Modo noche:** Solo visible en API < Q (igual que en el XML). El callback
  `onNightModeChanged` llama a `setDefaultNightMode` + `recreate()` en la Activity.

---

## Fase 5 — Single-activity (destino final)

- `MainActivity` pasa a `setContent { }`.
- Reemplazar `ViewPager2 + TabLayout + CollapsingToolbar` por:
  - `Scaffold` + `TopAppBar` M3
  - `HorizontalPager` + `TabRow` de Compose
- `NavHost` con rutas: lista → detalle, lista reportes, mapa, ajustes.
- `QuakeDetailsActivity` se convierte en destino composable (sin `Activity` ni `Intent` extra).
- Eliminar `MainFragmentStateAdapter`, `setupKoinFragmentFactory`, `koin-androidx-fragment`.

---

## Principios de la migración

- **ViewModels sin cambios**: `StateFlow`/`SharedFlow` son compatibles directamente con
  `collectAsStateWithLifecycle()`.
- **Interop obligatorio** (no se migra a Compose puro): Google Maps (`MapView`/`ClusterManager`)
  y AdMob (`NativeAdView`, `AdView`) → siempre `AndroidView`.
- **Un PR por fase** para poder revertir aislado.
- **Verificar cada fase**: `assembleDevDebug` + `testDevDebugUnitTest` + validación manual.
