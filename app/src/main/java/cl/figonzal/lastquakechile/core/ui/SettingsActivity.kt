package cl.figonzal.lastquakechile.core.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
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
            .replace(R.id.settings_menu, SettingsFragment())
            .commit()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings)
            setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
        }
    }

    //Settings Fragment
    class SettingsFragment : PreferenceFragmentCompat(),
        OnSharedPreferenceChangeListener {

        private var seekBarPreference: SeekBarPreference? = null

        private val sharedPrefUtil: SharedPrefUtil by lazy {
            SharedPrefUtil(requireContext())
        }
        private val quakesNotification by lazy {
            QuakesNotification(requireContext(), sharedPrefUtil)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            //VERSION
            findPreference<Preference>(getString(R.string.version_key)).apply {
                this?.summary = BuildConfig.VERSION_NAME
            }

            //VERSION
            findPreference<Preference>(getString(R.string.contact_key))?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse(getString(R.string.mail_to_felipe))

                    if (resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(this)
                    }
                }

                true
            }

            //QUAKE LIMIT PREFERENCE
            seekBarPreference =
                findPreference(resources.getString(R.string.shared_pref_list_quake_limit))

            seekBarPreference?.apply {

                min = 10
                max = 30

                val limite = sharedPrefUtil.getData(
                    resources.getString(R.string.shared_pref_list_quake_limit), 0
                ) as Int

                //Setear resumen por defecto
                value = when (limite) {
                    0 -> 15
                    else -> limite
                }

                summary =
                    resources.getQuantityString(
                        R.plurals.list_quake_number_summary,
                        value,
                        value
                    )
            }

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

        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {

            /*
             * Preferencia Modo Noche
             */
            if (key.equals(resources.getString(R.string.night_mode_key))) {

                //Si el modo manual esta activado
                when (preferences?.getBoolean(
                    resources.getString(R.string.night_mode_key), false
                )) {
                    true -> {

                        sharedPrefUtil.saveData(
                            resources.getString(R.string.night_mode_key),
                            true
                        )

                        nightModeAndRecreate(MODE_NIGHT_YES)

                        //Mostrar toast modo manual activado
                        requireActivity().toast(R.string.night_mode_key_toast_on)

                    }
                    else -> {

                        sharedPrefUtil.saveData(
                            resources.getString(R.string.night_mode_key),
                            false
                        )
                        nightModeAndRecreate(MODE_NIGHT_NO)

                        //Mostrar toast modo noche desactivado
                        requireActivity().toast(R.string.night_mode_key_toast_off)
                    }
                }
            }

            /*
             * Preferencias de alertas
             */
            if (key == resources.getString(R.string.firebase_pref_key)) {

                preferences?.getBoolean(
                    resources.getString(R.string.firebase_pref_key),
                    true
                ).also {

                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    when (it) {
                        true -> {
                            quakesNotification.suscribedToQuakes(true)
                            requireActivity().toast(R.string.firebase_pref_key_alert_on)
                        }
                        else -> {
                            quakesNotification.suscribedToQuakes(false)
                            requireActivity().toast(R.string.firebase_pref_key_alert_off)
                        }
                    }
                }
            }

            /*
             * Preferencias de numero de sismos
             */
            if (key == resources.getString(R.string.shared_pref_list_quake_limit)) {

                seekBarPreference?.apply {

                    summary =
                        resources.getQuantityString(
                            R.plurals.list_quake_number_summary,
                            value,
                            value
                        )

                    sharedPrefUtil.saveData(
                        resources.getString(R.string.shared_pref_list_quake_limit),
                        value
                    )
                    Timber.d(
                        resources.getString(R.string.FRAGMENT_SETTINGS_QUAKE_LIST_LIMIT),
                        value
                    )
                }

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}