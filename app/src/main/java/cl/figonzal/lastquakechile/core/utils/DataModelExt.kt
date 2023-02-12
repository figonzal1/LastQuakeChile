package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.Intent
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import timber.log.Timber

/**
 * Function that map dto list to full quake entity
 */
fun List<QuakeDTO>.toQuakeListEntity() = map {
    val quakeEntity = it.toEntity()
    val coordinateEntity = it.coordinate.toEntity()

    QuakeAndCoordinate(
        quakeEntity,
        coordinateEntity
    )
}

/**
 * Function that map full quake entity to domain model
 */
fun List<QuakeAndCoordinate>.toQuakeDomain() = map { it.toDomain() }

/**
 * Function that map report dto to entity
 */
fun List<ReportDTO>.toReportListEntity() = map { reportDTO ->
    val reportEntity = reportDTO.toEntity()
    val topCities = reportDTO.cityQuakes.map { it.toEntity() }

    ReportWithCityQuakes(
        report = reportEntity,
        cityQuakes = topCities
    )
}

/**
 * Function that map cityQuakeEntity to domain
 */
fun List<CityQuakesEntity>.toCityQuakesListDomain() = map { it.toDomain() }

/**
 * Function that map reportWithCityQuakes to domain
 */
fun List<ReportWithCityQuakes>.toReportListDomain() = map { it.toDomain() }

fun Context.openQuakeDetails(quake: Quake, isSnapshotRequestInBottomSheet: Boolean = false) {
    Intent(this, QuakeDetailsActivity::class.java).apply {
        putExtra(QUAKE, quake)

        if (isSnapshotRequestInBottomSheet) {
            putExtra(IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET, true)
        }

        Timber.d("QuakeDetail intent")
        startActivity(this)
    }
}