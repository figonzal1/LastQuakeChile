package cl.figonzal.lastquakechile.core.services.notifications.utils

//Constants used in settings preferences
internal const val ROOT_PREF_HIGH_PRIORITY_NOTIFICATION = "high_priority_notifications"
internal const val ROOT_PREF_QUAKE_PRELIMINARY = "quake_preliminary"
internal const val ROOT_PREF_SUBSCRIPTION = "pref_suscrito_quake"

//Crashlytics custom keys — see core/CrashlyticsKeys.kt for FIREBASE_CHANNEL_STATUS,
//FIREBASE_MSG_QUAKE_DATA, FIREBASE_MSG_GENERIC and FIREBASE_SUB_QUAKE.
internal const val FIREBASE_TOPIC_CHANNEL = "quakes_v2"

//Notification channel IDs (fixed, coexisting)
internal const val CHANNEL_ID_HIGH = "quakes_high"
internal const val CHANNEL_ID_DEFAULT = "quakes_default"

//Legacy key — used only during migration from random channel scheme
internal const val RANDOM_CHANNEL_ID_LEGACY = "random_channel_id"
const val MIN_MAGNITUDE_ALERT = "1.0"

//ANDROID > 13 permission request for notifications
internal const val SHARED_PREF_PERMISSION_ALERT_ANDROID_13 = "alert_permission_granted"

// Tracks whether POST_NOTIFICATIONS was ever requested at least once, so we can distinguish
// "never asked" (should show system dialog) from "permanently denied" (should open Settings).
internal const val SHARED_PREF_PERMISSION_ASKED_ONCE = "post_notifications_asked"

//JSON KEYS
const val QUAKE = "quake"
internal const val CITY = "ciudad"
internal const val REFERENCE = "referencia"
internal const val LATITUDE = "latitud"
internal const val LONGITUDE = "longitud"
internal const val UTC_DATE = "fecha_utc"
internal const val MAGNITUDE = "magnitud"
internal const val DEPTH = "profundidad"
internal const val SCALE = "escala"
internal const val IS_SENSIBLE = "sensible"
internal const val QUAKE_CODE = "imagen_url"
internal const val STATE = "estado"
internal const val IS_UPDATE = "is_update"

const val IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET = "isSnapshotRequestFromBottomSheet"

// Marks the intent built by QuakeNotificationImpl so QuakeDetailsActivity can tell "opened by
// tapping the push notification" apart from every other way of opening the same screen.
const val IS_FROM_NOTIFICATION = "isFromNotification"