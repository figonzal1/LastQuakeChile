package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import cl.figonzal.lastquakechile.R

class SharedPrefUtil(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.shared_pref_master_key),
        Context.MODE_PRIVATE
    )

    /**
     * Function date save data in shared preferences
     *
     * @param key Key that store the data in shared preferences
     * @param value The value which will be store in shared preferences
     */
    fun saveData(key: String, value: Any) {

        sharedPreferences.edit(true) {
            when (value) {
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                else -> putString(key, value as String)
            }
        }
    }

    /**
     * Function that retrieve data from shared preferences
     *
     * @param key Key that store the data in shared preferences
     * @param defaultvalue If the store value is inaccessible
     * @return Any
     */
    fun getData(key: String, defaultvalue: Any): Any {

        with(sharedPreferences) {
            return when (defaultvalue) {
                is Int -> getInt(key, defaultvalue)
                is Boolean -> getBoolean(key, defaultvalue)
                is Float -> getFloat(key, defaultvalue)
                is Long -> getLong(key, defaultvalue)
                else -> getString(key, defaultvalue as String)!!

            }
        }
    }
}