package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.core.data.remote.mapSuccess
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import com.skydoves.sandwich.ApiResponse

class ReportRemoteDataSource(private val reportAPI: ReportAPI) {
    suspend fun getReports(pageIndex: Int): ApiResponse<List<ReportDTO>> =
        reportAPI.listReports(pageIndex).mapSuccess { embedded?.reports.orEmpty() }
}
