package cl.figonzal.lastquakechile.newcode.data.remote

import cl.figonzal.lastquakechile.model.ReportModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ReportsRemoteDataSource {

    private var reportList: List<ReportModel>? = null

    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()


    suspend fun getReports(): List<ReportModel> {

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
            call.body()?.reportes
        } else {
            arrayListOf()
        }

        return reportList as List<ReportModel>
    }

}
