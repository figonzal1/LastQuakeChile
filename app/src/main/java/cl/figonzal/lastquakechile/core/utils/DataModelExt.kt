package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import timber.log.Timber
import java.util.Locale

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

/** Opens the privacy policy URL matching the current locale (es/en). */
fun Context.openPrivacyPolicy() {
    val url = when (Locale.getDefault().language) {
        "es" -> getString(R.string.PRIVACY_POLICY_URL_ES)
        else -> getString(R.string.PRIVACY_POLICY_URL_EN)
    }
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

/** Opens an email chooser to contact the developer. */
fun Context.sendContactEmail() {
    val intent = Intent(
        Intent.ACTION_SENDTO,
        Uri.parse("mailto:${getString(R.string.mail_to_felipe)}?subject=${getString(R.string.email_subject)}")
    ).apply {
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
    }
    startActivity(Intent.createChooser(intent, getString(R.string.email_chooser_title)))
}
