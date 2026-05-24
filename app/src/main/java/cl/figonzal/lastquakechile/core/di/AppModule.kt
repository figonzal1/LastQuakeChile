package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.core.services.notifications.QuakeNotificationImpl
import cl.figonzal.lastquakechile.core.ui.MainFragmentStateAdapter
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.provideApiService
import cl.figonzal.lastquakechile.core.utils.provideDatabase
import cl.figonzal.lastquakechile.quake_feature.di.quakeModule
import cl.figonzal.lastquakechile.reports_feature.di.reportModule
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    single(named("ioDispatcher")) { Dispatchers.IO }
    single(named("mainDispatcher")) { Dispatchers.Main }
    single(named("defaultDispatcher")) { Dispatchers.Default }

    single { SharedPrefUtil(androidContext()) }

    single { QuakeNotificationImpl(androidContext(), get()) }

    single(named("database")) { provideDatabase(get()) }

    single(named("apiService")) { provideApiService(BuildConfig.API_URL) }

    single { MainFragmentStateAdapter(get(), get()) }

    includes(quakeModule, reportModule)
}
