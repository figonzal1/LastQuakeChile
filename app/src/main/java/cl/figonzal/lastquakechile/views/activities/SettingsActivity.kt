package cl.figonzal.lastquakechile.views.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.databinding.SettingsActivityBinding
import cl.figonzal.lastquakechile.services.NightModeService
import cl.figonzal.lastquakechile.services.notifications.QuakesNotification
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Check modo noche
        NightModeService(this, this.lifecycle, window)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_menu, SettingsFragment())
            .commit()

        setSupportActionBar(binding.toolbarNoMain.toolBar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.settings)
        }


    }

    override fun onBackPressed() {
        val intent = Intent(this@SettingsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        super.onBackPressed()
    }

    //Settings Fragment
    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

        private var seekBarPreference: SeekBarPreference? = null
        private var sharedPrefUtil: SharedPrefUtil? = null
        private var quakesNotification: QuakesNotification? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            sharedPrefUtil = SharedPrefUtil(requireContext())
            quakesNotification = QuakesNotification(context, sharedPrefUtil)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            //Seek bar
            seekBarPreference =
                findPreference(requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER))

            if (seekBarPreference != null) {

                seekBarPreference!!.min = 10
                seekBarPreference!!.max = 30

                val limite = sharedPrefUtil!!.getData(
                    requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0
                ) as String

                //Setear resumen por defecto
                when (limite) {
                    "0" -> {
                        seekBarPreference!!.value = 15
                        seekBarPreference!!.summary =
                            String.format(
                                requireContext().getString(R.string.LIST_QUAKE_NUMBER_SUMMARY),
                                15
                            )
                    }
                    else -> seekBarPreference!!.summary = String.format(
                        requireContext().getString(R.string.LIST_QUAKE_NUMBER_SUMMARY),
                        limite.toInt()
                    )
                }
            } else {
                Timber.e("Seek bar nulo")
            }
        }

        override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {


            /*
             * Preferencia Modo Noche
             * MANUAL
             */
            if (key == requireContext().getString(R.string.NIGHT_MODE_MANUAL_KEY)) {

                //Si el modo manual esta activado
                when {
                    preferences.getBoolean(
                        requireContext().getString(R.string.NIGHT_MODE_MANUAL_KEY), false
                    ) -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        requireContext().setTheme(R.style.AppTheme)
                        requireActivity().recreate()

                        //Mostrar toast modo manual activado
                        Toast.makeText(
                            requireContext().applicationContext,
                            getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_ON),
                            Toast.LENGTH_LONG
                        ).show()

                        //Setear automatico como false si manual esta activado
                        val edit = preferences.edit()
                        edit.putBoolean(
                            requireContext().getString(R.string.NIGHT_MODE_AUTO_KEY),
                            false
                        )
                        edit.apply()

                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        requireContext().setTheme(R.style.AppTheme)
                        requireActivity().recreate()

                        //Mostrar toast modo manual desactivado
                        Toast.makeText(
                            requireContext().applicationContext,
                            requireContext().getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_OFF),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


            } else if (key == requireContext().getString(R.string.NIGHT_MODE_AUTO_KEY)) {

                //Si automatico esta activado, preguntar el estado del modo
                when {
                    preferences.getBoolean(
                        requireContext().getString(R.string.NIGHT_MODE_AUTO_KEY),
                        false
                    ) -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        requireContext().setTheme(R.style.AppTheme)
                        requireActivity().recreate()

                        //Mostrar toast con mensaje de modo noche automatico activado
                        Toast.makeText(
                            requireContext().applicationContext,
                            requireContext().getString(R.string.NIGHT_MODE_AUTO_KEY_TOAST_ON),
                            Toast.LENGTH_LONG
                        ).show()

                        //Setear automatico como false si manual esta activado
                        val edit = preferences.edit()
                        edit.putBoolean(
                            requireContext().getString(R.string.NIGHT_MODE_MANUAL_KEY),
                            false
                        )
                        edit.apply()
                    }
                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        requireContext().setTheme(R.style.AppTheme)
                        requireActivity().recreate()

                        //Mostrar toast con mensaje de modo noche automatico desactivado
                        Toast.makeText(
                            requireContext().applicationContext,
                            requireContext().getString(R.string.NIGHT_MODE_AUTO_KEY_TOAST_OFF),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }


            /*
             * Preferencias de alertas
             */
            if (key == requireContext().getString(R.string.FIREBASE_PREF_KEY)) {

                val notificationSwitch =
                    preferences.getBoolean(
                        requireContext().getString(R.string.FIREBASE_PREF_KEY),
                        true
                    )

                //Si el switch esta ON, lanzar toast con SUSCRITO
                if (notificationSwitch) {
                    quakesNotification!!.suscribedToQuakes(true)
                    Toast.makeText(
                        context,
                        requireContext().getString(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_ON),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    quakesNotification!!.suscribedToQuakes(false)
                    Toast.makeText(
                        context,
                        requireContext().getString(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_OFF),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            /*
             * Preferencias de numero de sismos
             */if (key == requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER)) {

                seekBarPreference!!.summary = String.format(
                    requireContext().getString(R.string.LIST_QUAKE_NUMBER_SUMMARY),
                    seekBarPreference!!.value
                )
                sharedPrefUtil!!.saveData(
                    requireContext().getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER),
                    seekBarPreference!!.value
                )
                Timber.i(
                    requireContext().getString(R.string.TAG_FRAGMENT_SETTINGS_QUAKE_LIST_LIMIT),
                    seekBarPreference!!.value
                )
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
}