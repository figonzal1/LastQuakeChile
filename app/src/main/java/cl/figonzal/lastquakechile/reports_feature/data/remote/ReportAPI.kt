package cl.figonzal.lastquakechile.reports_feature.data.remote

import retrofit2.Response
import retrofit2.http.GET

/**
 * Interface used for retrofit
 */
interface ReportAPI {

    @GET("/lastquakechile/api/v1/reports")
    suspend fun listReports(): Response<ReportsResult>


    companion object {
        const val BASE_URL: String = "https://lastquakechile-server-prod.herokuapp.com"
    }
}