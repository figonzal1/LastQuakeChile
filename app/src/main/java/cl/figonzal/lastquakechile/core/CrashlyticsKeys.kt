package cl.figonzal.lastquakechile.core

// Crashlytics custom keys, centralized so the panel's key names live in one place instead of
// scattered across every file that reports one. Values are unchanged from where each key used to
// be declared, to keep continuity with existing data in Firebase.

internal const val FIREBASE_CHANNEL_STATUS = "channel_status"
internal const val FIREBASE_MSG_QUAKE_DATA = "data_msg_received"
internal const val FIREBASE_MSG_GENERIC = "generic_msg_received"
internal const val FIREBASE_SUB_QUAKE = "subscribed_quake"
internal const val FIREBASE_GOOGLE_PLAY_SERVICE_STATE = "google_play_service_state"
internal const val FIREBASE_NIGHT_MODE_STATUS = "night_mode_status"
internal const val FIREBASE_LQCH_UPDATER_STATUS = "lqch_updater_status"
internal const val FIREBASE_AD_RESPONSE_ID = "ad_response_id"

// Activity actualmente visible — ver ApplicationController.trackCurrentScreen().
internal const val FIREBASE_CURRENT_SCREEN = "screen"
