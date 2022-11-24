package cl.figonzal.lastquakechile.core.ui

import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.*
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.QuakesNotification
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.subscribedToQuakes
import cl.figonzal.lastquakechile.core.utils.toast
import cl.figonzal.lastquakechile.databinding.SettingsActivityBinding
import timber.log.Timber

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
            title = getString(R.string.settings)
            setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
        }
    }

    //Settings Fragment
    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

        private val sharedPrefUtil: SharedPrefUtil by lazy {
            SharedPrefUtil(requireActivity())
        }
        private val quakesNotification by lazy {
            QuakesNotification(requireActivity(), sharedPrefUtil)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            if (isAdded) {
                configVersionPreferences()

                configNightMode()

                configMinimumMagnitude()
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

        private fun configVersionPreferences() {
            //VERSION
            findPreference<Preference>(getString(R.string.version_key)).apply {
                this?.summary = BuildConfig.VERSION_NAME
            }

            //VERSION
            findPreference<Preference>(getString(R.string.contact_key))?.setOnPreferenceClickListener {

                Intent(ACTION_SEND).apply {
                    putExtra(EXTRA_EMAIL, arrayOf(getString(R.string.mail_to_felipe)))
                    putExtra(EXTRA_SUBJECT, getString(R.string.email_subject))
                    type = "text/plain"

                    when {
                        resolveActivity(requireActivity().packageManager) != null -> {
                            requireActivity().startActivity(
                                createChooser(this, getString(R.string.email_chooser_title))
                            )
                        }
                        else -> requireActivity().toast(R.string.email_intent_fail)
                    }
                }
                true
            }
        }

        private fun configNightMode() {
            //NIGHT MODE PREFERENCE
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {

                    Timber.d(getString(R.string.SHOW_NIGH_MODE))
                    val nightModePrefCategory: PreferenceCategory? =
                        findPreference(getString(R.string.night_mode_category_key))

                    nightModePrefCategory?.isVisible = true
                }
                else -> Timber.d(getString(R.string.HIDE_NIGHT_MODE))
            }
        }

        private fun configMinimumMagnitude() {

            findPreference<EditTextPreference>(getString(R.string.minimum_magnitude_key))?.apply {
                val value =
                    sharedPrefUtil.getData(getString(R.string.minimum_magnitude_key), "5.0")
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
                            requireContext().subscribedToQuakes(true, sharedPrefUtil)
                            toast(R.string.firebase_pref_key_alert_on)
                            alertDependencies(true)
                        }
                        else -> {
                            requireContext().subscribedToQuakes(false, sharedPrefUtil)
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
                        quakesNotification.recreateChannel()
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

            if (key == getString(R.string.minimum_magnitude_key)) {
                val commandPreference = findPreference<Preference>(key)

                val minimumValueSaved = preferences?.getString(
                    getString(R.string.minimum_magnitude_key), "5.0"
                )

                commandPreference?.summary =
                    String.format(">=%s", minimumValueSaved)

                minimumValueSaved?.let {
                    sharedPrefUtil.saveData(getString(R.string.minimum_magnitude_key), it)
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
            findPreference<Preference>(getString(R.string.minimum_magnitude_key))?.also {
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