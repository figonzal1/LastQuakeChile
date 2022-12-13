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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

private const val SHARED_PERF_VERSION_CODE_SAVED = "actual_v_code"
private const val FIREBASE_CHANGE_LOG_STATUS = "change_log_status"

class ChangeLogService(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil,
    private val crashlytics: FirebaseCrashlytics
) : DefaultLifecycleObserver {

    private var versionCode: Int = BuildConfig.VERSION_CODE
    private val versionName = context.getString(R.string.version) + BuildConfig.VERSION_NAME
    private val listImprovements = listOf(
        context.getString(R.string.improvement_1),
        context.getString(R.string.improvement_2)
    )

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        configChangeLog()
    }

    private fun configChangeLog() {

        //GET STORED VERSION CODE
        val sharedVersionCode = sharedPrefUtil.getData(SHARED_PERF_VERSION_CODE_SAVED, 0) as Int

        Timber.d("Saved version code: $sharedVersionCode")
        Timber.d("Installed version code: $versionCode")

        when {
            sharedVersionCode < versionCode && sharedVersionCode != 0 && listImprovements.isNotEmpty() -> {

                Timber.d("New version detected")
                crashlytics.setCustomKey(FIREBASE_CHANGE_LOG_STATUS, "Showing change log dialog")

                showBottomDialog()
            }
            else -> {
                saveVersionCode()

                Timber.d("New version NOT detected")
                crashlytics.setCustomKey(FIREBASE_CHANGE_LOG_STATUS, "Not show change log dialog")
            }
        }
    }

    private fun showBottomDialog() {

        with(BottomSheetDialog(context)) {

            setContentView(R.layout.changelog_dialog)
            setCancelable(false)

            findViewById<TextView>(R.id.tv_version)?.text = versionName
            findViewById<TextView>(R.id.tv_changes_list)?.text =
                listImprovements.printChangeLogList()
            findViewById<MaterialButton>(R.id.btn_change_log)?.setOnClickListener {
                saveVersionCode()
                dismiss()
            }

            show()
        }
    }

    private fun saveVersionCode() {
        sharedPrefUtil.saveData(SHARED_PERF_VERSION_CODE_SAVED, versionCode)
    }
}