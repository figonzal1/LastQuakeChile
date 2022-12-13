package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import androidx.core.content.edit

private const val SHARED_PREF_MASTER_KEY = "lastquakechile"

class SharedPrefUtil(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        SHARED_PREF_MASTER_KEY,
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
     * @param defaultValue If the store value is inaccessible
     * @return Any
     */
    fun getData(key: String, defaultValue: Any): Any? {

        with(sharedPreferences) {
            return when (defaultValue) {
                is Int -> getInt(key, defaultValue)
                is Boolean -> getBoolean(key, defaultValue)
                is Float -> getFloat(key, defaultValue)
                is Long -> getLong(key, defaultValue)
                else -> getString(key, defaultValue as String)
            }
        }
    }
}