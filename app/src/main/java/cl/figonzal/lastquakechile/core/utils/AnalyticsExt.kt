package cl.figonzal.lastquakechile.core.utils

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

/**
 * Thin wrapper over Firebase Analytics so call sites read as a plain function call.
 * ParametersBuilder.param only accepts String/Long/Double, so anything else (Boolean, Int) is
 * normalized to one of those instead of silently being dropped by the SDK.
 */
fun logAnalyticsEvent(name: String, vararg params: Pair<String, Any?>) =
    Firebase.analytics.logEvent(name) {
        params.forEach { (key, value) ->
            when (value) {
                is Double -> param(key, value)
                is Number -> param(key, value.toLong())
                else -> param(key, value.toString())
            }
        }
    }
