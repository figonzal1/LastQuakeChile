package cl.figonzal.lastquakechile.core.ui

import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.QuakesNotification
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
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
            }
        }

        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {

            if (isAdded) {
                handleChangesNightMode(preferences, key)

                handleChangesNotificationSubscription(preferences, key)
            }
        }

        private fun nightModeAndRecreate(mode: Int) {
            setDefaultNightMode(mode)

            with(requireActivity()) {
                setTheme(R.style.AppTheme)
                recreate()
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
                                createChooser(
                                    this,
                                    getString(R.string.email_chooser_title)
                                )
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

        private fun handleChangesNightMode(preferences: SharedPreferences?, key: String?) {

            if (key.equals(getString(R.string.night_mode_key))) {

                when (preferences?.getBoolean(
                    getString(R.string.night_mode_key),
                    false
                )) {
                    true -> {

                        sharedPrefUtil.saveData(
                            getString(R.string.night_mode_key),
                            true
                        )

                        nightModeAndRecreate(MODE_NIGHT_YES)

                        toast(R.string.night_mode_key_toast_on)

                    }
                    else -> {

                        sharedPrefUtil.saveData(
                            getString(R.string.night_mode_key),
                            false
                        )
                        nightModeAndRecreate(MODE_NIGHT_NO)

                        toast(R.string.night_mode_key_toast_off)
                    }
                }

            }
        }

        private fun handleChangesNotificationSubscription(
            preferences: SharedPreferences?,
            key: String?
        ) {

            if (key == getString(R.string.firebase_pref_key)) {

                preferences?.getBoolean(
                    getString(R.string.firebase_pref_key),
                    true
                ).also {

                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    when (it) {
                        true -> {
                            quakesNotification.subscribedToQuakes(true)
                            toast(R.string.firebase_pref_key_alert_on)
                        }
                        else -> {
                            quakesNotification.subscribedToQuakes(false)
                            toast(R.string.firebase_pref_key_alert_off)
                        }
                    }
                }
            }
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