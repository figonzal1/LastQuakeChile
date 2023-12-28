package cl.figonzal.lastquakechile.core.ui

import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.createChooser
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.QuakeNotificationImpl
import cl.figonzal.lastquakechile.core.services.notifications.utils.MIN_MAGNITUDE_ALERT
import cl.figonzal.lastquakechile.core.services.notifications.utils.SHARED_PREF_PERMISSION_ALERT_ANDROID_13
import cl.figonzal.lastquakechile.core.services.notifications.utils.subscribedToQuakes
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.SettingsActivityBinding
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import timber.log.Timber
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.includeToolbar.materialToolbar)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.menu_settings)
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
        }
    }

    //Settings Fragment
    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

        private val fcm = Firebase.messaging
        private val crashlytics = Firebase.crashlytics

        private val sharedPrefUtil: SharedPrefUtil by lazy { SharedPrefUtil(requireActivity()) }
        private val notificationServiceImpl by lazy {
            QuakeNotificationImpl(requireActivity(), sharedPrefUtil)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            if (isAdded) {

                configNotifications()

                configVersionPreferences()

                configNightMode()

                contactAdsPolicy()

                configMinimumMagnitude()

                configPrivacyPolicy()
            }
        }

        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {

            if (isAdded) {
                handleChangesNightMode(preferences, key)

                handleChangesNotificationSub(preferences, key)

                handleNotificationsPriority(preferences, key)

                handlePreliminaryNotifications(preferences, key)

                handleMinimumMagnitude(preferences, key)
            }
        }

        private fun nightModeAndRecreate(mode: Int) {
            setDefaultNightMode(mode)

            with(requireActivity()) {
                setTheme(R.style.AppTheme)
                recreate()
            }
        }

        private fun configNotifications() {
            val isGrantedPermission =
                sharedPrefUtil.getData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, true) as Boolean

            when {
                !isGrantedPermission -> {

                    findPreference<SwitchPreferenceCompat>(getString(R.string.firebase_pref_key))?.apply {
                        isChecked = false
                        isEnabled = false
                    }
                    findPreference<PreferenceCategory>(getString(R.string.notifications_category_key))?.summary =
                        getString(R.string.permission_totally_disabled)

                    subscribedToQuakes(false, sharedPrefUtil, fcm, crashlytics)
                    alertDependencies(false)
                }
            }
        }

        private fun contactAdsPolicy() {
            val consentInformation = UserMessagingPlatform.getConsentInformation(requireContext())

            val isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

            if (isPrivacyOptionsRequired) {
                val adsCategory: PreferenceCategory? =
                    findPreference(getString(R.string.ads_category_key))

                adsCategory?.isVisible = true

                findPreference<Preference>(getString(R.string.ads_policy_key))?.setOnPreferenceClickListener {
                    UserMessagingPlatform.showPrivacyOptionsForm(requireActivity()) { formError ->
                        formError?.let {
                            Timber.w("Privacy options form: ${it.message}")
                        }
                    }
                    true
                }

            }
        }

        private fun configPrivacyPolicy() {
            findPreference<Preference>(getString(R.string.privacy_policy_key))?.setOnPreferenceClickListener {

                Intent(ACTION_VIEW).apply {

                    data = when (Locale.getDefault().language) {
                        "es" -> Uri.parse(getString(R.string.PRIVACY_POLICY_URL_ES))
                        else -> Uri.parse(getString(R.string.PRIVACY_POLICY_URL_EN))
                    }
                    startActivity(this)
                }
                true
            }
        }

        private fun configVersionPreferences() {
            //VERSION
            findPreference<Preference>(getString(R.string.version_key)).apply {
                this?.summary = BuildConfig.VERSION_NAME
            }

            //VERSION
            findPreference<Preference>(getString(R.string.contact_key))?.setOnPreferenceClickListener {

                Intent(
                    ACTION_SENDTO,
                    Uri.parse(
                        "mailto:${getString(R.string.mail_to_felipe)}" +
                                "?subject=${getString(R.string.email_subject)}"
                    )
                ).apply {
                    putExtra(EXTRA_SUBJECT, getString(R.string.email_subject))
                    requireActivity().startActivity(
                        createChooser(this, getString(R.string.email_chooser_title))
                    )
                }
                true
            }
        }

        private fun configNightMode() {
            //NIGHT MODE PREFERENCE
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {

                    Timber.d("Show night mode preference")
                    val nightModePrefCategory: PreferenceCategory? =
                        findPreference(getString(R.string.night_mode_category_key))

                    nightModePrefCategory?.isVisible = true
                }

                else -> Timber.d("Don't show night mode preference")
            }
        }

        private fun configMinimumMagnitude() {

            findPreference<EditTextPreference>(getString(R.string.min_magnitude_alert_key))?.apply {
                val value =
                    sharedPrefUtil.getData(
                        getString(R.string.min_magnitude_alert_key),
                        MIN_MAGNITUDE_ALERT
                    )
                summary = String.format(">=%s", value)

                setOnBindEditTextListener {
                    it.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
            }
        }

        private fun handleChangesNightMode(preferences: SharedPreferences?, key: String?) {

            if (key == getString(R.string.night_mode_key)) {

                when (preferences?.getBoolean(getString(R.string.night_mode_key), false)) {
                    true -> {

                        sharedPrefUtil.saveData(getString(R.string.night_mode_key), true)

                        nightModeAndRecreate(MODE_NIGHT_YES)

                        toast(R.string.night_mode_key_toast_on)

                    }

                    else -> {
                        sharedPrefUtil.saveData(getString(R.string.night_mode_key), false)

                        nightModeAndRecreate(MODE_NIGHT_NO)

                        toast(R.string.night_mode_key_toast_off)
                    }
                }

            }
        }

        private fun handleChangesNotificationSub(preferences: SharedPreferences?, key: String?) {

            if (key == getString(R.string.firebase_pref_key)) {

                preferences?.getBoolean(getString(R.string.firebase_pref_key), true).also {

                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    when (it) {
                        true -> {
                            subscribedToQuakes(true, sharedPrefUtil, fcm, crashlytics)
                            toast(R.string.firebase_pref_key_alert_on)
                            alertDependencies(true)
                        }

                        else -> {
                            subscribedToQuakes(false, sharedPrefUtil, fcm, crashlytics)
                            toast(R.string.firebase_pref_key_alert_off)
                            alertDependencies(false)
                        }
                    }
                }
            }
        }

        private fun handleNotificationsPriority(preferences: SharedPreferences?, key: String?) {

            if (key == getString(R.string.high_priority_key)) {

                preferences?.getBoolean(getString(R.string.high_priority_key), true)?.also {

                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    sharedPrefUtil.saveData(getString(R.string.high_priority_key), it)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationServiceImpl.recreateChannel()
                    }
                }
            }
        }

        private fun handlePreliminaryNotifications(preferences: SharedPreferences?, key: String?) {

            if (key == getString(R.string.quake_preliminary_key)) {

                preferences?.getBoolean(getString(R.string.quake_preliminary_key), true)?.also {
                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    sharedPrefUtil.saveData(getString(R.string.quake_preliminary_key), it)
                }
            }
        }

        private fun handleMinimumMagnitude(preferences: SharedPreferences?, key: String?) {

            if (key == getString(R.string.min_magnitude_alert_key)) {
                val commandPreference = findPreference<Preference>(key)

                val minimumValueSaved = preferences?.getString(
                    getString(R.string.min_magnitude_alert_key), MIN_MAGNITUDE_ALERT
                )

                commandPreference?.summary =
                    String.format(">=%s", minimumValueSaved)

                minimumValueSaved?.let {
                    sharedPrefUtil.saveData(getString(R.string.min_magnitude_alert_key), it)
                }
            }
        }

        /**
         * Function to disable alert preferences dependencies
         */
        private fun alertDependencies(isEnabled: Boolean) {

            findPreference<SwitchPreferenceCompat>(getString(R.string.quake_preliminary_key))?.also {
                it.isEnabled = isEnabled
            }
            findPreference<SwitchPreferenceCompat>(getString(R.string.high_priority_key))?.also {
                it.isEnabled = isEnabled
            }
            findPreference<Preference>(getString(R.string.min_magnitude_alert_key))?.also {
                it.isEnabled = isEnabled
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}