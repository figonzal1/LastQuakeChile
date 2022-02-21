package cl.figonzal.lastquakechile.core.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
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
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
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

            //QUAKE LIMIT PREFERENCE
            seekBarPreference =
                findPreference(resources.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER))

            seekBarPreference?.apply {

                min = 10
                max = 30

                val limite = sharedPrefUtil.getData(
                    resources.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0
                ) as Int

                //Setear resumen por defecto
                value = when (limite) {
                    0 -> 15
                    else -> limite
                }

                summary =
                    resources.getQuantityString(
                        R.plurals.LIST_QUAKE_NUMBER_SUMMARY,
                        value,
                        value
                    )
            }

            //NIGHT MODE PREFERENCE
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {

                    Timber.d(getString(R.string.show_night_mode))
                    val nightModePrefCategory: PreferenceCategory? =
                        findPreference(getString(R.string.NIGH_MODE_CATEGORY_KEY))

                    nightModePrefCategory?.isVisible = true
                }
                else -> Timber.d(getString(R.string.hide_night_mode))
            }
        }

        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {

            /*
             * Preferencia Modo Noche
             */
            if (key.equals(resources.getString(R.string.NIGHT_MODE_KEY))) {

                //Si el modo manual esta activado
                when (preferences?.getBoolean(
                    resources.getString(R.string.NIGHT_MODE_KEY), false
                )) {
                    true -> {

                        sharedPrefUtil.saveData(
                            resources.getString(R.string.NIGHT_MODE_KEY),
                            true
                        )

                        nightModeAndRecreate(MODE_NIGHT_YES)

                        //Mostrar toast modo manual activado
                        requireActivity().toast(R.string.NIGHT_MODE_KEY_TOAST_ON)

                    }
                    else -> {

                        sharedPrefUtil.saveData(
                            resources.getString(R.string.NIGHT_MODE_KEY),
                            false
                        )
                        nightModeAndRecreate(MODE_NIGHT_NO)

                        //Mostrar toast modo noche desactivado
                        requireActivity().toast(R.string.NIGHT_MODE_KEY_TOAST_OFF)
                    }
                }
            }

            /*
             * Preferencias de alertas
             */
            if (key == resources.getString(R.string.FIREBASE_PREF_KEY)) {

                preferences?.getBoolean(
                    resources.getString(R.string.FIREBASE_PREF_KEY),
                    true
                ).also {

                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    when (it) {
                        true -> {
                            quakesNotification.suscribedToQuakes(true)
                            requireActivity().toast(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_ON)
                        }
                        else -> {
                            quakesNotification.suscribedToQuakes(false)
                            requireActivity().toast(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_OFF)
                        }
                    }
                }
            }

            /*
             * Preferencias de numero de sismos
             */
            if (key == resources.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER)) {

                seekBarPreference?.apply {

                    summary =
                        resources.getQuantityString(
                            R.plurals.LIST_QUAKE_NUMBER_SUMMARY,
                            value,
                            value
                        )

                    sharedPrefUtil.saveData(
                        resources.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER),
                        value
                    )
                    Timber.d(
                        resources.getString(R.string.TAG_FRAGMENT_SETTINGS_QUAKE_LIST_LIMIT),
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