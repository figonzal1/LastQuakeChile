package cl.figonzal.lastquakechile.core

import android.app.Application
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.core.di.appModule
import cl.figonzal.lastquakechile.onboarding_feature.di.onboardingModule
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

        installDeadSystemExceptionFilter()

        startKoin {
            androidLogger(
                when {
                    BuildConfig.DEBUG -> Level.ERROR
                    else -> Level.NONE
                }
            )
            androidContext(this@ApplicationController)

            fragmentFactory()

            modules(appModule, onboardingModule)//, instrumentationTestModule)
        }

        when {
            BuildConfig.DEBUG -> Timber.plant(DebugTree())
            else -> Timber.plant(CrashlyticsTree())
        }
    }

    private fun installDeadSystemExceptionFilter() {
        val upstream = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isDeadSystemException(throwable)) {
                Timber.w("Ignored DeadSystemRuntimeException: system server died")
                return@setDefaultUncaughtExceptionHandler
            }
            upstream?.uncaughtException(thread, throwable)
        }
    }

    private fun isDeadSystemException(throwable: Throwable): Boolean {
        val name = throwable::class.java.name
        return name == "android.os.DeadSystemRuntimeException" ||
                name == "android.os.DeadSystemException"
    }
}