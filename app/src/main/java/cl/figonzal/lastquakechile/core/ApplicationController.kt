package cl.figonzal.lastquakechile.core

import android.app.Application
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.core.di.appModule
import cl.figonzal.lastquakechile.core.services.AppOpenService
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import timber.log.Timber.DebugTree


class ApplicationController : Application() {


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

            fragmentFactory()

            modules(appModule)//, instrumentationTestModule)
        }

        when {
            BuildConfig.DEBUG -> Timber.plant(DebugTree())
            else -> Timber.plant(CrashlyticsTree())
        }

        MobileAds.initialize(this) { }

        AppOpenService(this)
    }
}