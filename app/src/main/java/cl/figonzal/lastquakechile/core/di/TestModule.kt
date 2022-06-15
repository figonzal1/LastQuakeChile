package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.utils.TestFragmentFactory
import cl.figonzal.lastquakechile.core.utils.provideLimitedList
import cl.figonzal.lastquakechile.core.utils.provideTestDatabase
import cl.figonzal.lastquakechile.quake_feature.data.repository.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Dependencies for instrumented Test
 */
val instrumentationTestModule = module {

    //Test database
    single { provideTestDatabase(get()) }

    /**
     * QUAKE DEPENDENCIES
     */
    //FakeQuakeRepository !!
    single<QuakeRepository> { FakeQuakeRepository(get(named("ioDispatcher"))) }

    //getQuakeUseCase
    factory { GetQuakesUseCase(get(), provideLimitedList(get())) }

    //viewModel
    viewModel { QuakeViewModel(get()) }

    //Adapter
    single { QuakeAdapter() }

    factory { TestFragmentFactory(get()) }
}
