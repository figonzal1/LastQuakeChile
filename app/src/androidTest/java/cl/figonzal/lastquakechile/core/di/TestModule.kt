@file:Suppress("unused")

package cl.figonzal.lastquakechile.core.di

import android.app.Application
import androidx.room.Room
import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.quake_feature.data.repository.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.reports_feature.data.repository.FakeReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val testQuakeModule = module {
    //FakeQuakeRepository !!
    factory<QuakeRepository> { FakeQuakeRepository(get(named("ioDispatcher"))) }

    //viewModel
    viewModel { QuakeViewModel(get()) }

    //Adapter
    factory { QuakeAdapter() }
}

val testReportModule = module {
    //FakeQuakeRepository !!
    factory<ReportRepository> { FakeReportRepository(get(named("ioDispatcher"))) }

    //viewModel
    viewModel { ReportViewModel(get()) }

    //Adapter
    factory { ReportAdapter() }
}

/**
 * Provide in memory database for injection test
 */
private fun provideTestDatabase(application: Application): AppDatabase = Room.inMemoryDatabaseBuilder(
    application,
    AppDatabase::class.java
).allowMainThreadQueries().build()

/**
 * Dependencies for instrumented Test
 */
val instrumentationTestModule = module {

    //Test database
    factory { provideTestDatabase(get()) }

    includes(testQuakeModule, testReportModule)

}
