package cl.figonzal.lastquakechile.reports_feature.domain.model

data class Report(
    val reportMonth: String,
    val nSensitive: Int,
    val nQuakes: Int,
    val promMagnitude: Double,
    val promDepth: Double,
    val maxMagnitude: Double,
    val minDepth: Double,
    val cityQuakes: List<CityQuakes>
)