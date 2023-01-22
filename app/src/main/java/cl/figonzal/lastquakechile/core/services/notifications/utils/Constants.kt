package cl.figonzal.lastquakechile.core.services.notifications.utils

//Constants used in settings preferences
internal const val ROOT_PREF_HIGH_PRIORITY_NOTIFICATION = "high_priority_notifications"
internal const val ROOT_PREF_QUAKE_PRELIMINARY = "quake_preliminary"
internal const val ROOT_PREF_MIN_MAGNITUDE = "minimum_magnitude"
internal const val ROOT_PREF_SUBSCRIPTION = "pref_suscrito_quake"


//Constants used for crashlytics logger
internal const val FIREBASE_CHANNEL_STATUS = "channel_status"
internal const val FIREBASE_MSG_QUAKE_DATA = "data_msg_received"
internal const val FIREBASE_MSG_GENERIC = "generic_msg_received"
internal const val FIREBASE_SUB_QUAKE = "subscribed_quake"
internal const val FIREBASE_TOPIC_CHANNEL = "quakes_v2"

//Other constants
internal const val RANDOM_CHANNEL_ID = "random_channel_id"

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