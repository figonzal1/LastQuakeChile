package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ReportRemoteDataSource {

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()


    suspend fun getReports(): List<ReportWithQuakeCity> {

        val service: ReportAPI = Retrofit.Builder()
            .baseUrl(ReportAPI.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .run {
                create(ReportAPI::class.java)
            }


        val call = service.listReports()

        return call.body()?.reportes?.map { it ->

            val reportEntity = it.toReportEntity()
            val topCities = it.top_ciudades.map { it.toQuakeCityEntity() }

            ReportWithQuakeCity(
                report = reportEntity,
                topCities = topCities
            )
        }!!
    }
}
