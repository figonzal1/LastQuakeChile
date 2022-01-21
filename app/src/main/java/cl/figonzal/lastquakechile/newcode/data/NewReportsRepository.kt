package cl.figonzal.lastquakechile.newcode.data

import cl.figonzal.lastquakechile.model.ReportModel
import cl.figonzal.lastquakechile.newcode.data.remote.ReportsRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NewReportsRepository(private val remoteDataSource: ReportsRemoteDataSource) {

    suspend fun getReports(): Flow<List<ReportModel>> = flow {
        emit(remoteDataSource.getReports())
    }

}