package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val SHARED_PREF_MASTER_KEY = "lastquakechile"

class SharedPrefUtil(context: Context) {

    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
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
                is String -> putString(key, value)
                else -> throw IllegalArgumentException("Unsupported value type")
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
    inline fun <reified T> getData(key: String, defaultValue: T): T {
        return when (T::class) {
            Int::class -> sharedPreferences.getInt(key, defaultValue as Int) as T
            Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as Boolean) as T
            Float::class -> sharedPreferences.getFloat(key, defaultValue as Float) as T
            Long::class -> sharedPreferences.getLong(key, defaultValue as Long) as T
            String::class -> sharedPreferences.getString(key, defaultValue as String) as T
            else -> throw IllegalArgumentException("Unsupported default value type")
        }
    }
}