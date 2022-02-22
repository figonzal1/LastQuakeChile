package cl.figonzal.lastquakechile.core

import android.app.Application
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.core.di.appModule
import cl.figonzal.lastquakechile.core.di.networkModule
import cl.figonzal.lastquakechile.core.services.AppOpenService
import cl.figonzal.lastquakechile.quake_feature.di.quakeModule
import cl.figonzal.lastquakechile.reports_feature.di.reportModule
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import timber.log.Timber.DebugTree


class ApplicationController : Application() {

    companion object {
        lateinit var appOpenService: AppOpenService
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(
                when {
                    BuildConfig.DEBUG -> Level.ERROR
                    else -> Level.NONE
                }
            )
            androidContext(this@ApplicationController)

            modules(appModule, networkModule, quakeModule, reportModule)
        }

        when {
            BuildConfig.DEBUG -> Timber.plant(DebugTree())
            else -> Timber.plant(CrashlyticsTree())
        }

        MobileAds.initialize(this) { }

        appOpenService = AppOpenService(this)
    }
}