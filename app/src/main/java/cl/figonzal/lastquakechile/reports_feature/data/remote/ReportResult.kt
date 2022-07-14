package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Result object for retrofit call
 */
@JsonClass(generateAdapter = true)
data class ReportResult(

    @field:Json(name = "reportes")
    val reports: List<ReportDTO>
)