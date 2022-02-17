package cl.figonzal.lastquakechile.quake_feature.di

import cl.figonzal.lastquakechile.core.utils.provideQuakeAPI
import cl.figonzal.lastquakechile.core.utils.provideQuakeDao
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.data.repository.QuakeRepositoryImpl
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val quakeModule = module {

    //Local DataSources Dependency
    single { provideQuakeDao(get(named("database"))) }
    single { QuakeLocalDataSource(get()) }

    //Remote DataSources Dependency
    single { provideQuakeAPI(get(named("apiService"))) }
    single { QuakeRemoteDataSource(get(), get()) }

    //Repository
    single<QuakeRepository> { QuakeRepositoryImpl(get(), get(), get(named("ioDispatcher")), get()) }

    //getQuakeUseCase
    single { GetQuakesUseCase(get()) }

    //viewModel
    viewModel { QuakeViewModel(get()) }

}