package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.SharedPreferences
import cl.figonzal.lastquakechile.R

class SharedPrefUtil(context: Context) {

    private var sharedPreferences: SharedPreferences? = null

    init {
        sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.SHARED_PREF_MASTER_KEY),
            Context.MODE_PRIVATE
        )
    }

    fun saveData(key: String?, value: Any?) {
        val editor = sharedPreferences!!.edit()

        when (value) {
            is Int -> editor.putInt(key, (value as Int?)!!)
            is String -> editor.putString(key, value as String?)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, (value as Float?)!!)
            is Long -> editor.putLong(key, (value as Long?)!!)
        }
        editor.apply()
    }

    fun getData(key: String?, defaultvalue: Any?): Any? {
        val result: Any?
        result = when (defaultvalue) {
            is Long -> sharedPreferences!!.getLong(key, (defaultvalue as Long?)!!)
            is Float -> sharedPreferences!!.getFloat(key, (defaultvalue as Float?)!!)
            is Int -> sharedPreferences!!.getInt(key, (defaultvalue as Int?)!!)
            is Boolean -> sharedPreferences!!.getBoolean(key, (defaultvalue as Boolean?)!!)
            else -> sharedPreferences!!.getString(key, defaultvalue as String?)
        }
        return result
    }
}