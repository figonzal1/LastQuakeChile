package cl.figonzal.lastquakechile.quake_feature.di

import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeAPI
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.data.repository.QuakeRepositoryImpl
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.quake_feature.ui.map.MapsFragment
import cl.figonzal.lastquakechile.quake_feature.ui.share.QuakeStoryRenderer
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Koin sub module for quakes
 */
val quakeModule = module {

    //Local DataSources Dependency
    single { get<AppDatabase>(named("database")).quakeDao() }
    single { QuakeLocalDataSource(get()) }

    //Remote DataSources Dependency
    single { get<Retrofit>(named("apiService")).create(QuakeAPI::class.java) }
    single { QuakeRemoteDataSource(get()) }

    //Repository
    single<QuakeRepository> {
        QuakeRepositoryImpl(
            get(),
            get(),
            get(named("ioDispatcher"))
        )
    }

    //viewModel
    viewModel { QuakeViewModel(get()) }

    //Adapter
    factory { QuakeAdapter() }

    //Share
    factory { QuakeStoryRenderer(androidContext()) }

    //QuakeFragment
    fragment { QuakeFragment() }

    //Map Fragment
    fragment { MapsFragment() }
}