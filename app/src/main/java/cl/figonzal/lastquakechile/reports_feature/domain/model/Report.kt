package cl.figonzal.lastquakechile.reports_feature.domain.model

import cl.figonzal.lastquakechile.model.QuakesCity

data class Report(
    val reportMonth: String,
    val nSensitive: Int,
    val nQuakes: Int,
    val promMagnitud: Double,
    val promDepth: Double,
    val maxMagnitude: Double,
    val minDepth: Double,
    val topCities: List<QuakesCity>
)