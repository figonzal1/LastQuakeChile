package cl.figonzal.lastquakechile.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.core.di.appModule
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import timber.log.Timber.DebugTree

private const val FIREBASE_CURRENT_SCREEN = "screen"

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

            modules(appModule)
        }

        when {
            BuildConfig.DEBUG -> Timber.plant(DebugTree())
            else -> Timber.plant(CrashlyticsTree())
        }

        trackCurrentScreen()
    }

    /**
     * Custom key "screen" en Crashlytics con la Activity visible: el dato #1 para triage,
     * saber en qué pantalla estaba el usuario cuando ocurrió el crash.
     */
    private fun trackCurrentScreen() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                Firebase.crashlytics.setCustomKey(
                    FIREBASE_CURRENT_SCREEN,
                    activity::class.java.simpleName
                )
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
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