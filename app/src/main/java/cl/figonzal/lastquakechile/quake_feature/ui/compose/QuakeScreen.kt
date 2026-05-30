package cl.figonzal.lastquakechile.quake_feature.ui.compose

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.services.notifications.utils.SHARED_PREF_PERMISSION_ASKED_ONCE
import cl.figonzal.lastquakechile.core.services.notifications.utils.SHARED_PREF_PERMISSION_ALERT_ANDROID_13
import cl.figonzal.lastquakechile.core.services.notifications.utils.ROOT_PREF_SUBSCRIPTION
import cl.figonzal.lastquakechile.core.services.notifications.utils.subscribedToQuakes
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.openQuakeDetails
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeState
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val QUERY_PAGE_SIZE = 20

// ─── ViewModel-backed entry point ────────────────────────────────────────────

@Composable
fun QuakeScreen(viewModel: QuakeViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val noConnectionMsg = stringResource(R.string.io_error)
    val serviceErrorMsg = stringResource(R.string.service_error)
    val noMoreDataMsg = stringResource(R.string.no_more_data)
    val httpErrorMsg = stringResource(R.string.http_error)

    LaunchedEffect(Unit) {
        viewModel.getFirstPageQuakes()
    }

    LaunchedEffect(Unit) {
        viewModel.errorState.collect { error ->
            val message = when (error) {
                DomainError.NoConnection -> noConnectionMsg
                DomainError.ServerError, DomainError.Timeout -> serviceErrorMsg
                DomainError.NoMoreData -> noMoreDataMsg
                else -> httpErrorMsg
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    // Transparente para dejar ver el fondo del MaterialCardView contenedor
    // (toolbar_layout.xml, cardElevation=4dp). En dark mode M2 ese card recibe un
    // elevation overlay que lo aclara (~#454549); pintar surface plano (#323236) lo
    // dejaría más oscuro que el AdFragment vecino y crearía un seam al hacer swipe.
    // contentColor se fija explícito porque contentColorFor(Transparent) = Unspecified.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        QuakeContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onRetry = { viewModel.getFirstPageQuakes() },
            onLoadMore = { viewModel.getNextPageQuakes() },
            onQuakeClick = { quake -> context.openQuakeDetails(quake) }
        )
    }
}

// ─── Stateless content (previewable) ─────────────────────────────────────────

@Composable
internal fun QuakeContent(
    state: QuakeState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onQuakeClick: (Quake) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Notification permission card — Android 13+ only
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationPermissionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }

            // Cache copy banner when showing stale data after an error
            if (state.quakes.isNotEmpty() &&
                state.domainError != null &&
                state.domainError != DomainError.NoMoreData
            ) {
                Text(
                    text = stringResource(R.string.local_copy),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            when {
                state.quakes.isEmpty() &&
                        state.domainError != null &&
                        state.domainError != DomainError.NoMoreData -> {
                    ErrorContent(
                        error = state.domainError,
                        onRetry = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                else -> {
                    QuakeList(
                        quakes = state.quakes,
                        isLoading = state.isLoading,
                        isLastPage = state.isLastPage,
                        onLoadMore = onLoadMore,
                        onQuakeClick = onQuakeClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Horizontal progress bar at top — matches progressBarStyleHorizontal from XML
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = Color.Transparent
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Internal composables ─────────────────────────────────────────────────────

@Composable
private fun QuakeList(
    quakes: List<Quake>,
    isLoading: Boolean,
    isLastPage: Boolean,
    onLoadMore: () -> Unit,
    onQuakeClick: (Quake) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { info ->
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@map false
                val total = info.totalItemsCount
                // total >= QUERY_PAGE_SIZE replicates the RecyclerView scroll listener guard:
                // avoids eager auto-load when the full list fits on screen without user scrolling.
                !isLoading && !isLastPage && total >= QUERY_PAGE_SIZE && lastVisible >= total - 1
            }
            .distinctUntilChanged()
            .collect { shouldLoad -> if (shouldLoad) onLoadMore() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(quakes, key = { it.quakeCode }) { quake ->
            QuakeCard(quake = quake, onClick = onQuakeClick)
        }
    }
}

@Composable
private fun ErrorContent(
    error: DomainError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = when (error) {
        DomainError.NoConnection -> stringResource(R.string.io_error)
        DomainError.ServerError, DomainError.Timeout -> stringResource(R.string.service_error)
        DomainError.EmptyList -> stringResource(R.string.empty_list)
        else -> stringResource(R.string.http_error)
    }
    val showRetry = error != DomainError.EmptyList

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        if (showRetry) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val sharedPrefUtil = remember { SharedPrefUtil(context) }

    var isGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ASKED_ONCE, true)
        if (granted) {
            isGranted = true
            sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, true)
            if (sharedPrefUtil.getData(ROOT_PREF_SUBSCRIPTION, true)) {
                subscribedToQuakes(true)
            }
        } else {
            sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, false)
        }
    }

    // Re-check permission on resume so the card disappears after granting from system Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isGranted) return

    val wasAskedBefore = sharedPrefUtil.getData(SHARED_PREF_PERMISSION_ASKED_ONCE, false)
    val activity = context as? android.app.Activity
    val permanentlyDenied = wasAskedBefore && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.POST_NOTIFICATIONS
            )

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.alert_permission_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.alert_permission_description),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = {
                    if (permanentlyDenied) {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        )
                    } else {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text(
                    stringResource(
                        if (permanentlyDenied) R.string.open_settings_button
                        else R.string.activate_button
                    )
                )
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun fakeQuake(
    code: Int,
    city: String,
    reference: String,
    magnitude: Double,
    isSensitive: Boolean = false,
    isVerified: Boolean = true
) = Quake(
    quakeCode = code,
    localDate = LocalDateTime.now().minusMinutes(code.toLong())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
    city = city,
    reference = reference,
    magnitude = magnitude,
    depth = 34.8,
    scale = "ml",
    isSensitive = isSensitive,
    isVerified = isVerified,
    coordinate = Coordinate(-30.0, -71.0)
)

private val sampleQuakes = listOf(
    fakeQuake(1, "La Serena", "45km al OS de La Serena", 3.6),
    fakeQuake(2, "Concepción", "14km al OS de Concepción", 6.6, isSensitive = true),
    fakeQuake(3, "Santiago", "14km al OS de Santiago", 2.8),
    fakeQuake(4, "Arica", "67km al SE de Arica", 3.2),
    fakeQuake(5, "Iquique", "21km al SE de Iquique", 3.8, isSensitive = true),
)

@Preview(showBackground = true, name = "QuakeScreen — List")
@Composable
private fun QuakeScreenListPreview() {
    LastQuakeChileTheme {
        QuakeContent(state = QuakeState(quakes = sampleQuakes))
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "QuakeScreen — List Dark"
)
@Composable
private fun QuakeScreenListDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        QuakeContent(state = QuakeState(quakes = sampleQuakes))
    }
}

@Preview(showBackground = true, name = "QuakeScreen — Loading")
@Composable
private fun QuakeScreenLoadingPreview() {
    LastQuakeChileTheme {
        QuakeContent(state = QuakeState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "QuakeScreen — Error (no connection)")
@Composable
private fun QuakeScreenErrorPreview() {
    LastQuakeChileTheme {
        QuakeContent(state = QuakeState(domainError = DomainError.NoConnection))
    }
}

@Preview(showBackground = true, name = "QuakeScreen — Cache copy banner")
@Composable
private fun QuakeScreenCacheBannerPreview() {
    LastQuakeChileTheme {
        QuakeContent(
            state = QuakeState(quakes = sampleQuakes, domainError = DomainError.NoConnection)
        )
    }
}

@Preview(showBackground = true, name = "QuakeScreen — Loading next page")
@Composable
private fun QuakeScreenLoadingNextPagePreview() {
    LastQuakeChileTheme {
        QuakeContent(state = QuakeState(quakes = sampleQuakes, isLoading = true))
    }
}
