package cl.figonzal.lastquakechile

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class ApplicationController : Application() {

    override fun onCreate() {
        super.onCreate()

        when {
            BuildConfig.DEBUG -> Timber.plant(DebugTree())
            else -> Timber.plant(CrashlyticsTree())
        }
    }
}