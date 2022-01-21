package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ReportsRemoteDataSource {

    private var reportList: List<Report>? = null

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()


    suspend fun getReports(): List<Report> {

        val service: ReportAPI = Retrofit.Builder()
            .baseUrl(ReportAPI.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .run {
                create(ReportAPI::class.java)
            }


        val call = service.listReports()

        reportList = if (call.isSuccessful) {
            call.body()?.reportes?.map {
                it.toDomainReport()
            }
        } else {
            arrayListOf()
        }

        return reportList as List<Report>
    }

}
