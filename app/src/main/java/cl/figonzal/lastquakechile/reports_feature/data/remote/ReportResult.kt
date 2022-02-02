package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import com.google.gson.annotations.SerializedName

/**
 * Result object for retrofit call
 */
data class ReportResult(
    @SerializedName("reportes")
    val reportes: List<ReportDTO>
)