package cl.figonzal.lastquakechile.core.utils

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

/**
 * Thin wrapper over Firebase Analytics so call sites read as a plain function call instead of
 * building a Bundle by hand. `bundleOf` is deprecated in the version of core-ktx this project
 * pins, hence the small manual `when`.
 */
fun logAnalyticsEvent(name: String, vararg params: Pair<String, Any?>) {
    val bundle = Bundle().apply {
        params.forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value?.toString())
            }
        }
    }
    Firebase.analytics.logEvent(name, bundle)
}
