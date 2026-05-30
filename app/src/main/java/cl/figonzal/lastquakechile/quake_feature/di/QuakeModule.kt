package cl.figonzal.lastquakechile.quake_feature.di

import cl.figonzal.lastquakechile.core.utils.provideQuakeAPI
import cl.figonzal.lastquakechile.core.utils.provideQuakeDao
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.data.repository.QuakeRepositoryImpl
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.use_case.GetQuakesUseCase
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.quake_feature.ui.map.MapsFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin sub module for quakes
 */
val quakeModule = module {

    //Local DataSources Dependency
    single { provideQuakeDao(get(named("database"))) }
    single { QuakeLocalDataSource(get()) }

    //Remote DataSources Dependency
    single { provideQuakeAPI(get(named("apiService"))) }
    single { QuakeRemoteDataSource(get()) }

    //Repository
    single<QuakeRepository> {
        QuakeRepositoryImpl(
            get(),
            get(),
            get(named("ioDispatcher"))
        )
    }

    //getQuakeUseCase
    factory { GetQuakesUseCase(get()) }

    //viewModel
    viewModel { QuakeViewModel(get()) }

    //QuakeFragment
    fragment { QuakeFragment() }

    //Map Fragment
    fragment { MapsFragment() }
}