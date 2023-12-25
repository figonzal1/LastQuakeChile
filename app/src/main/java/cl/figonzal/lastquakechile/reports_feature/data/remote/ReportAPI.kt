package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.core.data.remote.Embedded
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface used for retrofit
 */
fun interface ReportAPI {

    @GET("/api/v1/reports")
    suspend fun listReports(
        @Query(value = "page") page: Int
    ): ApiResponse<Embedded<ReportPayload>>
}