package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.provideApiService
import cl.figonzal.lastquakechile.core.utils.provideDatabase
import cl.figonzal.lastquakechile.quake_feature.di.quakeModule
import cl.figonzal.lastquakechile.reports_feature.di.reportModule
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin application module
 */
val appModule = module {

    //IoDispatcher
    single(named("ioDispatcher")) { Dispatchers.IO }

    //Database
    single(named("database")) { provideDatabase(get()) }

    //Retrofit
    single(named("apiService")) { provideApiService(androidContext().resources.getString(R.string.API_URL)) }

    //Include child modules
    includes(quakeModule, reportModule)
}