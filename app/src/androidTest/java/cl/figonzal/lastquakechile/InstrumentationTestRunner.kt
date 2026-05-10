package cl.figonzal.lastquakechile

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import cl.figonzal.lastquakechile.core.TestApplication

class InstrumentationTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(cl, TestApplication::class.java.name, context)
}
