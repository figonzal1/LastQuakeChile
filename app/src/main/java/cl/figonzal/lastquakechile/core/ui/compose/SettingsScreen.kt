package cl.figonzal.lastquakechile.core.ui.compose

import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.utils.MIN_MAGNITUDE_ALERT
import cl.figonzal.lastquakechile.core.services.notifications.utils.subscribedToQuakes
import cl.figonzal.lastquakechile.core.ui.compose.settings.Preference
import cl.figonzal.lastquakechile.core.ui.compose.settings.PreferenceCategory
import cl.figonzal.lastquakechile.core.ui.compose.settings.SettingsToolbar
import cl.figonzal.lastquakechile.core.ui.compose.settings.SwitchPreference
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.openPrivacyPolicy
import cl.figonzal.lastquakechile.core.utils.readBoolMigrating
import cl.figonzal.lastquakechile.core.utils.readStringMigrating
import cl.figonzal.lastquakechile.core.utils.sendContactEmail
import cl.figonzal.lastquakechile.core.utils.views.toast

/**
 * Compose settings screen. Reads/writes directly to [SharedPrefUtil], the single source of
 * truth consumed by the notification system. Reads fall back to the legacy default-prefs value
 * so settings persisted by the old PreferenceFragmentCompat carry over transparently.
 */
@Composable
fun SettingsScreen(
    sharedPrefUtil: SharedPrefUtil,
    showAdsPolicy: Boolean,
    onBack: () -> Unit,
    onNightModeChanged: (Boolean) -> Unit,
    onShowPrivacyForm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val subscriptionKey = stringResource(R.string.firebase_pref_key)
    val preliminaryKey = stringResource(R.string.quake_preliminary_key)
    val highPriorityKey = stringResource(R.string.high_priority_key)
    val minMagnitudeKey = stringResource(R.string.min_magnitude_alert_key)
    val nightModeKey = stringResource(R.string.night_mode_key)

    var osNotificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                osNotificationsEnabled =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var subscriptionOn by remember {
        mutableStateOf(sharedPrefUtil.readBoolMigrating(context, subscriptionKey, true))
    }
    var preliminaryOn by remember {
        mutableStateOf(sharedPrefUtil.readBoolMigrating(context, preliminaryKey, true))
    }
    var highPriorityOn by remember {
        mutableStateOf(sharedPrefUtil.readBoolMigrating(context, highPriorityKey, true))
    }
    var minMagnitude by remember {
        mutableStateOf(
            sharedPrefUtil.readStringMigrating(
                context,
                minMagnitudeKey,
                MIN_MAGNITUDE_ALERT
            )
        )
    }
    var nightModeOn by remember {
        mutableStateOf(sharedPrefUtil.readBoolMigrating(context, nightModeKey, false))
    }
    var showMinMagnitudeDialog by remember { mutableStateOf(false) }

    // Dependent alert settings are usable only when OS allows notifications AND master switch is ON.
    val alertsActionable = osNotificationsEnabled && subscriptionOn

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsToolbar(
                title = stringResource(R.string.menu_settings),
                onNavigateUp = onBack,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        ) {
            // ── Notifications ────────────────────────────────────────────────
            item {
                PreferenceCategory(
                    title = stringResource(R.string.alert_pref_title),
                    icon = painterResource(R.drawable.round_notifications_24),
                    summary = if (osNotificationsEnabled) stringResource(R.string.alert_pref_summary)
                    else stringResource(R.string.permission_totally_disabled),
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.alert_pref_title_switch),
                    subTitle = onOffSummary(subscriptionOn),
                    checked = subscriptionOn,
                    enabled = osNotificationsEnabled,
                    onCheckedChange = { checked ->
                        subscriptionOn = checked
                        sharedPrefUtil.saveData(subscriptionKey, checked)
                        subscribedToQuakes(checked)
                        context.toast(
                            if (checked) R.string.firebase_pref_key_alert_on
                            else R.string.firebase_pref_key_alert_off
                        )
                    }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.preliminary_pref_title),
                    subTitle = onOffSummary(preliminaryOn),
                    checked = preliminaryOn,
                    enabled = alertsActionable,
                    onCheckedChange = { checked ->
                        preliminaryOn = checked
                        sharedPrefUtil.saveData(preliminaryKey, checked)
                    }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.high_priority_title),
                    subTitle = stringResource(R.string.high_priority_summary),
                    checked = highPriorityOn,
                    enabled = alertsActionable,
                    onCheckedChange = { checked ->
                        highPriorityOn = checked
                        sharedPrefUtil.saveData(highPriorityKey, checked)
                    }
                )
            }
            item {
                Preference(
                    title = stringResource(R.string.minimum_magnitude_title),
                    subTitle = ">=$minMagnitude",
                    enabled = alertsActionable,
                    onClick = { showMinMagnitudeDialog = true }
                )
            }

            // ── Night mode (manual toggle only below Android Q) ──────────────
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                item { HorizontalDivider() }
                item {
                    PreferenceCategory(
                        title = stringResource(R.string.night_mode_pref_title),
                        icon = painterResource(R.drawable.round_night_mode_24),
                        summary = stringResource(R.string.night_mode_pref_summary),
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.night_mode_pref_title),
                        subTitle = onOffSummary(nightModeOn),
                        checked = nightModeOn,
                        onCheckedChange = { checked ->
                            nightModeOn = checked
                            sharedPrefUtil.saveData(nightModeKey, checked)
                            context.toast(
                                if (checked) R.string.night_mode_key_toast_on
                                else R.string.night_mode_key_toast_off
                            )
                            onNightModeChanged(checked)
                        }
                    )
                }
            }

            // ── Ads / consent (only when UMP requires the privacy options) ────
            if (showAdsPolicy) {
                item { HorizontalDivider() }
                item {
                    PreferenceCategory(
                        title = stringResource(R.string.ads_title),
                        icon = painterResource(R.drawable.round_policy_24),
                    )
                }
                item {
                    Preference(
                        title = stringResource(R.string.consent_privacy_preference_title),
                        subTitle = stringResource(R.string.conset_privacy_preference_subtitle),
                        onClick = onShowPrivacyForm,
                    )
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            item { HorizontalDivider() }
            item {
                PreferenceCategory(
                    title = stringResource(R.string.about),
                    icon = painterResource(R.drawable.round_contact_support_24),
                )
            }
            item {
                Preference(
                    title = "",
                    subTitle = stringResource(R.string.about_msg),
                    isTitlePresent = false,
                )
            }
            item { HorizontalDivider() }
            item {
                Preference(
                    title = stringResource(R.string.version),
                    subTitle = BuildConfig.VERSION_NAME,
                )
            }
            item { HorizontalDivider() }
            item {
                Preference(
                    title = stringResource(R.string.privacy_policy),
                    subTitle = "",
                    isTitlePresent = true,
                    onClick = { context.openPrivacyPolicy() }
                )
            }
            item { HorizontalDivider() }
            item {
                Preference(
                    title = stringResource(R.string.contact_developer),
                    subTitle = stringResource(R.string.contact_summary),
                    onClick = { context.sendContactEmail() }
                )
            }
        }
    }

    if (showMinMagnitudeDialog) {
        MinMagnitudeDialog(
            initialValue = minMagnitude,
            onDismiss = { showMinMagnitudeDialog = false },
            onConfirm = { newValue ->
                showMinMagnitudeDialog = false
                if (newValue.toDoubleOrNull() != null) {
                    minMagnitude = newValue
                    sharedPrefUtil.saveData(minMagnitudeKey, newValue)
                }
            }
        )
    }
}

@Composable
private fun onOffSummary(on: Boolean) = stringResource(
    if (on) R.string.alert_pref_summary_on else R.string.alert_pref_summary_off
)

@Composable
private fun MinMagnitudeDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.min_magnitude_dialog_title)) },
        text = {
            androidx.compose.foundation.layout.Column {
                Text(stringResource(R.string.min_magnitude_dialog_message))
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            // primary (#253561 in dark) is invisible on dialog surface — use onSurface instead.
            TextButton(
                onClick = { onConfirm(text.trim()) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
