package cl.figonzal.lastquakechile.core.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
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
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_menu, SettingsFragment())
                .commit()
        }

        setSupportActionBar(binding.toolbarNoMain.toolBar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings)
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }
    }

    //Settings Fragment
    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

        private lateinit var seekBarPreference: SeekBarPreference

        private val sharedPrefUtil: SharedPrefUtil by lazy {
            SharedPrefUtil(requireContext())
        }
        private val quakesNotification by lazy {
            QuakesNotification(requireContext(), sharedPrefUtil)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            //Seek bar
            findPreference<SeekBarPreference>(requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER))?.apply {

                min = 10
                max = 30

                val limite = sharedPrefUtil.getData(
                    requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0
                )

                //Setear resumen por defecto
                when (limite) {
                    0 -> {
                        value = 15
                        summary = String.format(
                            requireContext().getString(R.string.LIST_QUAKE_NUMBER_SUMMARY),
                            15
                        )
                    }
                    else -> summary = String.format(
                        requireContext().getString(R.string.LIST_QUAKE_NUMBER_SUMMARY),
                        limite
                    )
                }
            }
        }

        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {

            /*
             * Preferencia Modo Noche
             */
            if (key.equals(requireContext().getString(R.string.NIGHT_MODE_KEY))) {

                //Si el modo manual esta activado
                when (preferences?.getBoolean(
                    requireContext().getString(R.string.NIGHT_MODE_KEY), false
                )) {
                    true -> {

                        sharedPrefUtil.saveData(
                            requireContext().getString(R.string.NIGHT_MODE_KEY),
                            true
                        )

                        nightModeAndRecreate(MODE_NIGHT_YES)

                        //Mostrar toast modo manual activado
                        requireContext().toast(R.string.NIGHT_MODE_KEY_TOAST_ON)

                    }
                    else -> {

                        sharedPrefUtil.saveData(
                            requireContext().getString(R.string.NIGHT_MODE_KEY),
                            false
                        )
                        nightModeAndRecreate(MODE_NIGHT_NO)

                        //Mostrar toast modo noche desactivado
                        requireContext().toast(R.string.NIGHT_MODE_KEY_TOAST_OFF)
                    }
                }
            }

            /*
             * Preferencias de alertas
             */
            if (key == requireContext().getString(R.string.FIREBASE_PREF_KEY)) {

                preferences?.getBoolean(
                    requireContext().getString(R.string.FIREBASE_PREF_KEY),
                    true
                ).also {

                    //Si el switch esta ON, lanzar toast con SUSCRITO
                    when (it) {
                        true -> {
                            quakesNotification.suscribedToQuakes(true)
                            requireContext().toast(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_ON)
                        }
                        else -> {
                            quakesNotification.suscribedToQuakes(false)
                            requireContext().toast(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_OFF)
                        }
                    }
                }
            }

            /*
             * Preferencias de numero de sismos
             */
            if (key == requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER)) {

                seekBarPreference.apply {

                    summary = String.format(
                        requireContext().getString(R.string.LIST_QUAKE_NUMBER_SUMMARY),
                        value
                    )
                    sharedPrefUtil.saveData(
                        requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER),
                        value
                    )
                    Timber.i(
                        requireContext().getString(R.string.TAG_FRAGMENT_SETTINGS_QUAKE_LIST_LIMIT),
                        value
                    )
                }

            }
        }

        private fun nightModeAndRecreate(mode: Int) {
            setDefaultNightMode(mode)
            requireContext().setTheme(R.style.AppTheme)
            requireActivity().recreate()
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