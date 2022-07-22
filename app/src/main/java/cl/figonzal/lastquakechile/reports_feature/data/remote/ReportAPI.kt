package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.core.data.remote.Embedded
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
    ): Response<Embedded<ReportPayload>>
}