package cl.figonzal.lastquakechile.reports_feature.ui.compose

import android.content.res.Configuration
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.reports_feature.domain.model.CityQuakes
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.ui.ReportState
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

// Tamaño de página de reportes (igual que el QUERY_PAGE_SIZE del antiguo ReportsFragment).
private const val QUERY_PAGE_SIZE = 5

// ─── ViewModel-backed entry point ────────────────────────────────────────────

@Composable
fun ReportsScreen(viewModel: ReportViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val noConnectionMsg = stringResource(R.string.io_error)
    val serviceErrorMsg = stringResource(R.string.service_error)
    val noMoreDataMsg = stringResource(R.string.no_more_data)
    val httpErrorMsg = stringResource(R.string.http_error)

    LaunchedEffect(Unit) {
        viewModel.getFirstPageReports()
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

    ReportsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.getFirstPageReports() },
        onLoadMore = { viewModel.getNextPageReports() }
    )
}

// ─── Stateless content (previewable) ─────────────────────────────────────────

@Composable
internal fun ReportsContent(
    state: ReportState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    // background (#28282C dark / #EFEFEF light) coincide con el Scaffold del detalle,
    // asegurando que los cards (surface = #323236) sean más claros que el fondo en dark mode.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.reports.isEmpty() && state.domainError != null && state.domainError != DomainError.NoMoreData -> {
                    ErrorContent(
                        error = state.domainError,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ReportsList(
                        reports = state.reports,
                        isLoading = state.isLoading,
                        isLastPage = state.isLastPage,
                        onLoadMore = onLoadMore
                    )
                }
            }

            // Barra horizontal en la parte superior, igual que progressBarStyleHorizontal del XML.
            // secondary (cyan) es visible en light y dark. trackColor transparent elimina el
            // fondo gris de M3 que no existía en el M2 ProgressBar.
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
        } // Box
    } // Surface
}

// ─── Internal composables ─────────────────────────────────────────────────────

@Composable
private fun ReportsList(
    reports: List<Report>,
    isLoading: Boolean,
    isLastPage: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { info ->
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@map false
                val total = info.totalItemsCount
                // total >= QUERY_PAGE_SIZE replica el guard del RecyclerView original:
                // no paginar cuando hay menos de una página completa (no hay siguiente página),
                // evitando además recargar datos duplicados.
                !isLoading && !isLastPage && total >= QUERY_PAGE_SIZE && lastVisible >= total - 1
            }
            .distinctUntilChanged()
            .collect { shouldLoad -> if (shouldLoad) onLoadMore() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        items(reports, key = { it.reportMonth }) { report ->
            ReportCard(
                report = report,
                modifier = Modifier.padding(vertical = 4.dp)
            )
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

// ─── Previews ─────────────────────────────────────────────────────────────────

private val sampleReports = List(5) { i ->
    Report(
        reportMonth = "2024-0${i + 1}",
        nSensitive = i * 3,
        nQuakes = 100 + i * 20,
        promMagnitude = 3.1 + i * 0.2,
        promDepth = 45.0 + i * 5,
        maxMagnitude = 5.5 + i * 0.1,
        minDepth = 10.0,
        cityQuakes = listOf(
            CityQuakes("Santiago", 30 + i),
            CityQuakes("Valparaíso", 15 + i),
        )
    )
}

@Preview(showBackground = true, name = "ReportsScreen — List")
@Composable
private fun ReportsScreenListPreview() {
    LastQuakeChileTheme {
        ReportsContent(state = ReportState(reports = sampleReports))
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ReportsScreen — List Dark"
)
@Composable
private fun ReportsScreenListDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        ReportsContent(state = ReportState(reports = sampleReports))
    }
}

@Preview(showBackground = true, name = "ReportsScreen — Loading")
@Composable
private fun ReportsScreenLoadingPreview() {
    LastQuakeChileTheme {
        ReportsContent(state = ReportState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "ReportsScreen — Error (no connection)")
@Composable
private fun ReportsScreenErrorPreview() {
    LastQuakeChileTheme {
        ReportsContent(state = ReportState(domainError = DomainError.NoConnection))
    }
}

@Preview(showBackground = true, name = "ReportsScreen — Error (empty list)")
@Composable
private fun ReportsScreenEmptyPreview() {
    LastQuakeChileTheme {
        ReportsContent(state = ReportState(domainError = DomainError.EmptyList))
    }
}

@Preview(showBackground = true, name = "ReportsScreen — Loading next page (barra top)")
@Composable
private fun ReportsScreenLoadingNextPagePreview() {
    LastQuakeChileTheme {
        // isLoading=true con items → barra horizontal arriba, lista visible debajo
        ReportsContent(state = ReportState(reports = sampleReports, isLoading = true))
    }
}
