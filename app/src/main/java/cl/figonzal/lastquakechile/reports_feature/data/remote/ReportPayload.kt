package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportPayload(
    @Json(name = "reports")
    val reports: List<ReportDTO>
)
