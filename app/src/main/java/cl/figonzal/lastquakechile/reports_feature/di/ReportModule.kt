package cl.figonzal.lastquakechile.reports_feature.di

import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.provideReportAPI
import cl.figonzal.lastquakechile.core.utils.provideReportDao
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.data.repository.ReportRepositoryImpl
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val reportModule = module {

    //Local DataSources Dependency
    single { provideReportDao(get(named("database"))) }
    single { ReportLocalDataSource(get()) }

    //Remote DataSources Dependency
    single { provideReportAPI(get(named("apiService"))) }
    single { ReportRemoteDataSource(get()) }

    //SharedPrefUtils
    single { SharedPrefUtil(get()) }

    //Repository
    single<ReportRepository> {
        ReportRepositoryImpl(
            get(),
            get(),
            get(named("ioDispatcher")),
            get(),
            get()
        )
    }

    //getReportsUseCase
    single { GetReportsUseCase(get()) }

    //viewModel
    viewModel { ReportViewModel(get()) }
}