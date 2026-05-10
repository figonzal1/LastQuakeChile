package cl.figonzal.lastquakechile.core

import android.app.Application
import cl.figonzal.lastquakechile.core.di.appModule
import cl.figonzal.lastquakechile.core.di.instrumentationTestModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            allowOverride(true)
            androidContext(this@TestApplication)
            fragmentFactory()
            modules(appModule, instrumentationTestModule)
        }

        Timber.plant(Timber.DebugTree())
    }
}
