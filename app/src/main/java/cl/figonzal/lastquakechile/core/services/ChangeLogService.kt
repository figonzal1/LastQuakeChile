package cl.figonzal.lastquakechile.core.services

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.printChangeLogList
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import timber.log.Timber

class ChangeLogService(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil
) :
    DefaultLifecycleObserver {

    private val version = context.getString(R.string.version) + BuildConfig.VERSION_NAME
    private val listImprovements = listOf(
        "- Se agrega menu de configuración",
        "- Actualizaciones internas necesarias"
    )

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        checkChangeLogVersion()
    }

    private fun checkChangeLogVersion() {
        configChangeLog()
    }

    private fun configChangeLog() {

        //GET PACKAGE VERSION CODE
        val versionCode = BuildConfig.VERSION_CODE

        //GET STORED VERSION CODE
        val sharedVersionCode = sharedPrefUtil.getData(
            context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE),
            0
        ) as Int

        Timber.d("${context.getString(R.string.SHARED_VERSION_CODE_APP)}$sharedVersionCode")
        Timber.d("${context.getString(R.string.VERSION_CODE_APP)}$versionCode")

        when {
            sharedVersionCode < versionCode -> {

                showBottomDialog()
                sharedPrefUtil.saveData(
                    context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE), versionCode
                )

                Timber.d(context.getString(R.string.NEW_VERSION_DETECTED))
            }
            else -> Timber.d(context.getString(R.string.NO_NEW_VERSION_DETECTED))
        }
    }

    private fun showBottomDialog() {

        with(BottomSheetDialog(context)) {

            setContentView(R.layout.changelog_dialog)
            setCancelable(false)

            findViewById<TextView>(R.id.tv_version)?.text = version
            findViewById<TextView>(R.id.tv_changes_list)?.text =
                listImprovements.printChangeLogList()
            findViewById<MaterialButton>(R.id.btn_change_log)?.setOnClickListener { dismiss() }

            show()
        }
    }
}