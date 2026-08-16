package cl.figonzal.lastquakechile.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.content.pm.PackageManager.ResolveInfoFlags
import android.net.Uri
import android.os.Build
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

internal const val INSTAGRAM_PACKAGE = "com.instagram.android"
private const val INSTAGRAM_STORY_ACTION = "com.instagram.share.ADD_TO_STORY"
internal const val WHATSAPP_PACKAGE = "com.whatsapp"

fun Context.buildShareText(quake: Quake): String = String.format(
    """
        [${getString(R.string.SHARE_TITLE)}]

        ${getString(R.string.SHARE_SUB_TITLE)}
        ${getString(R.string.SHARE_CITY)}: %1${"$"}s
        ${getString(R.string.SHARE_LOCAL_HOUR)}: %2${"$"}s
        ${getString(R.string.SHARE_MAGNITUDE)}: %3$.1f %4${"$"}s
        ${getString(R.string.SHARE_DEPTH)}: %5$.1f Km
        ${getString(R.string.SHARE_GEO_REF)}: %6${"$"}s

        ${getString(R.string.SHARE_DOWNLOAD_MSG)} %7${"$"}s

    """.trimIndent(),
    quake.city,
    quake.localDate,
    quake.magnitude,
    quake.scale,
    quake.depth,
    quake.reference,
    getString(R.string.APP_LINK)
)

/**
 * Same intent shape used to probe availability and to actually launch the share, so the two
 * can never drift apart again - a probe built with a different MIME type/action than the real
 * intent will silently fail to resolve even when the target app is installed, since an
 * intent-filter with a `mimeType` never matches a typeless intent.
 */
private fun instagramStoryIntent(imageUri: Uri?): Intent = Intent(INSTAGRAM_STORY_ACTION).apply {
    setPackage(INSTAGRAM_PACKAGE)
    type = "image/*"
    imageUri?.let { putExtra("interactive_asset_uri", it) }
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

private fun whatsAppSendIntent(): Intent =
    Intent(Intent.ACTION_SEND).setPackage(WHATSAPP_PACKAGE).setType("image/*")

fun Context.isInstagramStoriesAvailable(): Boolean =
    resolveActivityOrNull(instagramStoryIntent(null)) != null

fun Context.isWhatsAppAvailable(): Boolean =
    resolveActivityOrNull(whatsAppSendIntent()) != null

/**
 * Sends [imageUri] + the quake text to Instagram Stories as a movable/scalable sticker over
 * a gradient background. Instagram has required `source_application` since January 2023 -
 * without it the share fails with "The app you shared from doesn't currently support sharing
 * to Stories". Both the unprefixed and the `com.instagram.sharedSticker.*` background color
 * extras are sent since different Instagram versions read one or the other.
 */
fun Context.shareQuakeToInstagramStory(
    imageUri: Uri,
    topBackgroundColor: String,
    bottomBackgroundColor: String
): Boolean {
    val intent = instagramStoryIntent(imageUri).apply {
        putExtra("source_application", BuildConfig.FB_APP_ID)
        putExtra("top_background_color", topBackgroundColor)
        putExtra("bottom_background_color", bottomBackgroundColor)
        putExtra("com.instagram.sharedSticker.backgroundTopColor", topBackgroundColor)
        putExtra("com.instagram.sharedSticker.backgroundBottomColor", bottomBackgroundColor)
    }

    if (resolveActivityOrNull(intent) == null) return false

    grantUriPermission(INSTAGRAM_PACKAGE, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
    return true
}

fun Context.shareQuakeToWhatsApp(quake: Quake, imageUri: Uri?): Boolean {
    val intent = whatsAppSendIntent().apply {
        putExtra(Intent.EXTRA_TEXT, buildShareText(quake))
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    if (resolveActivityOrNull(intent) == null) return false

    imageUri?.let { grantUriPermission(WHATSAPP_PACKAGE, it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    startActivity(intent)
    return true
}

fun Context.copyQuakeText(quake: Quake) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.SHARE_TITLE), buildShareText(quake)))
}

/**
 * Original system chooser flow: `ACTION_SEND` with the quake text + image, granting read
 * access to every app the chooser can resolve to (grantUriPermission on the chooser Intent
 * itself doesn't propagate to the resolved target on all OEM/API combinations).
 */
fun Context.shareQuakeGeneric(quake: Quake, imageUri: Uri?) {
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, buildShareText(quake))
        putExtra(Intent.EXTRA_STREAM, imageUri)
        type = "image/*"

        val chooser = Intent.createChooser(this, getString(R.string.intent_chooser))

        val resInfoList = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> packageManager.queryIntentActivities(
                chooser,
                ResolveInfoFlags.of(MATCH_DEFAULT_ONLY.toLong())
            )

            else -> packageManager.queryIntentActivities(chooser, MATCH_DEFAULT_ONLY)
        }

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            imageUri?.let {
                grantUriPermission(
                    packageName,
                    it,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        startActivity(chooser)
    }
}

private fun Context.resolveActivityOrNull(intent: Intent) = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
        packageManager.resolveActivity(intent, ResolveInfoFlags.of(0))

    else -> @Suppress("DEPRECATION") packageManager.resolveActivity(intent, 0)
}
