package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface used for retrofit
 */
interface ReportAPI {

    @GET("/api/v1/reports")
    suspend fun listReports(
        @Query(value = "sort") sort: String = "reportMonth,desc"
    ): Response<ReportResult>
}

@JsonClass(generateAdapter = true)
data class ReportResult(
    @Json(name = "_embedded")
    val embedded: ReportPayload
)

@JsonClass(generateAdapter = true)
data class ReportPayload(
    @Json(name = "reports")
    val reports: List<ReportDTO>
)