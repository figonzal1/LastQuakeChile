@file:Suppress("unused")

package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.utils.TestFragmentFactory
import cl.figonzal.lastquakechile.core.utils.provideTestDatabase
import cl.figonzal.lastquakechile.quake_feature.data.repository.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.reports_feature.data.repository.FakeReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val testQuakeModule = module {
    //FakeQuakeRepository !!
    factory<QuakeRepository> { FakeQuakeRepository(get(named("ioDispatcher"))) }

    //getQuakeUseCase
    factory { GetQuakesUseCase(get()) }

    //viewModel
    viewModel { QuakeViewModel(get()) }

    //Adapter
    factory { QuakeAdapter() }
}

val testReportModule = module {
    //FakeQuakeRepository !!
    factory<ReportRepository> { FakeReportRepository(get(named("ioDispatcher"))) }

    //getQuakeUseCase
    factory { GetReportsUseCase(get()) }

    //viewModel
    viewModel { ReportViewModel(get()) }

    //Adapter
    factory { ReportAdapter() }
}

/**
 * Dependencies for instrumented Test
 */
val instrumentationTestModule = module {

    //Test database
    factory { provideTestDatabase(get()) }

    includes(testQuakeModule, testReportModule)

    //Test factory depends on submodules above
    factory { TestFragmentFactory(get(), get()) }
}
