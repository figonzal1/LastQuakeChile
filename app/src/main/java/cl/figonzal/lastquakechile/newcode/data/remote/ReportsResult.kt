package cl.figonzal.lastquakechile.newcode.data.remote

import cl.figonzal.lastquakechile.model.ReportModel
import com.google.gson.annotations.SerializedName

/**
 * Result object for retrofit call
 */
data class ReportsResult(
    @SerializedName("reportes")
    val reportes: List<ReportModel>
)