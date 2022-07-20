package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.ui.MainFragmentStateAdapter
import cl.figonzal.lastquakechile.core.utils.provideApiService
import cl.figonzal.lastquakechile.core.utils.provideDatabase
import cl.figonzal.lastquakechile.quake_feature.di.quakeModule
import cl.figonzal.lastquakechile.reports_feature.di.reportModule
import kotlinx.coroutines.Dispatchers
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
    single(named("apiService")) { provideApiService("http://192.168.1.101:8080/") }

    //StateAdapter
    single { MainFragmentStateAdapter(get(), get()) }

    //Include child modules
    includes(quakeModule, reportModule)
}