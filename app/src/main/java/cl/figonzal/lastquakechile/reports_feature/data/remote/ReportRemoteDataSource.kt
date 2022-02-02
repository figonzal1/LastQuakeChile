package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.utils.Resource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ReportRemoteDataSource {

    private var reportList: List<Report>? = null

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()


    suspend fun getReports(): Resource<List<Report>> {

        val service: ReportAPI = Retrofit.Builder()
            .baseUrl(ReportAPI.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .run {
                create(ReportAPI::class.java)
            }


        return try {
            val call = service.listReports()

            if (call.isSuccessful) {
                reportList = call.body()?.reportes?.map {
                    it.toDomainReport()
                }
            }
            Resource.Success(reportList)
        } catch (e: Exception) {
            Resource.Error("Error al pedir reportes: $e")
        }
    }
}
