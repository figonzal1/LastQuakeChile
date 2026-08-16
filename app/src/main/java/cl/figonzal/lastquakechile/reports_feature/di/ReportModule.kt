package cl.figonzal.lastquakechile.reports_feature.di

import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportAPI
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.data.repository.ReportRepositoryImpl
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import cl.figonzal.lastquakechile.reports_feature.ui.ReportsFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Koin sub module for report
 */
val reportModule = module {

    //Local DataSources Dependency
    single { get<AppDatabase>(named("database")).reportDao() }
    single { ReportLocalDataSource(get()) }

    //Remote DataSources Dependency
    single { get<Retrofit>(named("apiService")).create(ReportAPI::class.java) }
    single { ReportRemoteDataSource(get()) }

    //SharedPrefUtils
    single { SharedPrefUtil(get()) }

    //Repository
    single<ReportRepository> {
        ReportRepositoryImpl(get(), get(), get(named("ioDispatcher")))
    }

    //viewModel
    viewModel { ReportViewModel(get()) }

    //ReportAdapter
    factory { ReportAdapter() }

    //Report Fragment
    fragment { ReportsFragment() }
}