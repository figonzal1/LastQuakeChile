# Migración XML → Jetpack Compose

Estrategia incremental "bottom-up" con interop siguiendo la guía oficial de Google.
Cada fase compila + tests pasan + validación manual antes de continuar con la siguiente.

## Estado de las fases

| Fase | Descripción                                              | Estado       |
|------|----------------------------------------------------------|--------------|
| 0    | Infraestructura Compose + Theme Material 3               | ✅ Completa  |
| 1    | Lista de Reportes (ComposeView interop)                  | ✅ Completa  |
| 2    | Lista de Sismos + permiso notificaciones                 | ⏳ Pendiente |
| 3    | Detalle de sismo (mapa + ads con AndroidView)            | ⏳ Pendiente |
| 4    | Ajustes (SharedPrefUtil → Compose)                       | ⏳ Pendiente |
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

**Archivos pendientes de borrar** (una vez validado en dispositivo):
- `reports_feature/ui/ReportAdapter.kt`
- `res/layout/card_view_reports.xml`
- `res/layout/fragment_reports.xml`

**Patrón de paginación en Compose:**
```kotlin
LaunchedEffect(listState) {
    snapshotFlow { listState.layoutInfo }
        .map { info ->
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@map false
            val total = info.totalItemsCount
            !isLoading && !isLastPage && total > 0 && lastVisible >= total - 1
        }
        .distinctUntilChanged()
        .collect { shouldLoad -> if (shouldLoad) onLoadMore() }
}
```

---

## Fase 2 — Lista de Sismos (próxima)

Misma estructura que Fase 1. Diferencias a considerar:

1. `QuakeCard` — portar `card_view_quake.xml`:
   - Color de magnitud: usar `getMagnitudeColor(quake.magnitude, false)` del `ViewsExt.kt` existente
     → mapear el `@ColorRes Int` a `Color` de Compose con `colorResource(id)`.
   - `timeToText` es una extension de `TextView` — hay que crear una función pura equivalente
     o llamar a la lógica interna (`localDateToDHMS`) directamente desde el composable.
   - Íconos de verificado/sensible con visibilidad condicional → `if (quake.isVerified) Icon(...)`.

2. Tarjeta de permiso de notificaciones (Android 13+):
   - Reemplaza `handleCvAlertPermission` → `rememberLauncherForActivityResult` + `if` en composable.

3. Navegación a detalle: `openQuakeDetails(quake)` es extension de `Context` → se puede llamar
   directamente desde el composable con `LocalContext.current.openQuakeDetails(quake)`.

**Archivos a crear:**
- `quake_feature/ui/compose/QuakeCard.kt`
- `quake_feature/ui/compose/QuakeScreen.kt`

**Archivos a modificar:**
- `quake_feature/ui/QuakeFragment.kt` → ComposeView
- `quake_feature/di/QuakeModule.kt` → eliminar `QuakeAdapter`

---

## Fase 3 — Detalle de sismo

- Tarjeta detalle → Compose puro (Column con los campos).
- **Mapa**: `MapView` envuelto con `AndroidView` (mantener ciclo de vida completo).
  La animación de los círculos y `ValueAnimator` quedan intactos dentro del callback de `onMapReady`.
- **Anuncio nativo**: `NativeAdView` envuelto con `AndroidView`.
- `Scaffold` + `TopAppBar` para el toolbar con el botón de capas y compartir.

---

## Fase 4 — Ajustes

- `PreferenceFragmentCompat` → pantalla Compose con lista de opciones.
- Leer/escribir usando el mismo `SharedPrefUtil` para no cambiar el almacenamiento.

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
