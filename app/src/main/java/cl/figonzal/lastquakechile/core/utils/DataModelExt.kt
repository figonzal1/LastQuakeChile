package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.Intent
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import timber.log.Timber

fun Context.openQuakeDetails(quake: Quake, isSnapshotRequestInBottomSheet: Boolean = false) {
    Intent(this, QuakeDetailsActivity::class.java).apply {
        putExtra(QUAKE, quake)

        if (isSnapshotRequestInBottomSheet) {
            putExtra(IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET, true)
        }

        Timber.d("QuakeDetail intent")
        startActivity(this)
    }
}
