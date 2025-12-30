package app.aaps.plugins.sync.nsclientV3

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.sharedPreferences.SP
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.StringKey
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.core.ui.compose.preference.stringTextFieldPreference
import app.aaps.core.ui.compose.preference.switchPreference
import app.aaps.plugins.sync.R

/**
 * Compose implementation of NSClientV3 preferences.
 */
class NSClientV3PreferencesCompose(
    private val sp: SP
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Main NSClient category
        preferenceCategory(
            key = "ns_client_settings",
            titleResId = R.string.ns_client_v3_title
        )

        // Nightscout URL
        stringTextFieldPreference(
            sp = sp,
            key = StringKey.NsClientUrl.key,
            defaultValue = StringKey.NsClientUrl.defaultValue,
            titleResId = R.string.ns_client_url_title,
            summaryResId = R.string.ns_client_url_dialog_message
        )

        // Access Token
        stringTextFieldPreference(
            sp = sp,
            key = StringKey.NsClientAccessToken.key,
            defaultValue = StringKey.NsClientAccessToken.defaultValue,
            titleResId = R.string.nsclient_token_title,
            summaryResId = R.string.nsclient_token_dialog_message,
            isPassword = true
        )

        // Use WebSockets
        switchPreference(
            sp = sp,
            key = BooleanKey.NsClient3UseWs.key,
            defaultValue = BooleanKey.NsClient3UseWs.defaultValue,
            titleResId = R.string.ns_use_ws_title,
            summaryResId = R.string.ns_use_ws_summary
        )

        // Synchronization category
        preferenceCategory(
            key = "ns_client_synchronization",
            titleResId = R.string.ns_sync_options
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientUploadData.key,
            defaultValue = BooleanKey.NsClientUploadData.defaultValue,
            titleResId = R.string.ns_upload,
            summaryResId = R.string.ns_upload_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.BgSourceUploadToNs.key,
            defaultValue = BooleanKey.BgSourceUploadToNs.defaultValue,
            titleResId = app.aaps.core.ui.R.string.do_ns_upload_title
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptCgmData.key,
            defaultValue = BooleanKey.NsClientAcceptCgmData.defaultValue,
            titleResId = R.string.ns_receive_cgm,
            summaryResId = R.string.ns_receive_cgm_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptProfileStore.key,
            defaultValue = BooleanKey.NsClientAcceptProfileStore.defaultValue,
            titleResId = R.string.ns_receive_profile_store,
            summaryResId = R.string.ns_receive_profile_store_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptTempTarget.key,
            defaultValue = BooleanKey.NsClientAcceptTempTarget.defaultValue,
            titleResId = R.string.ns_receive_temp_target,
            summaryResId = R.string.ns_receive_temp_target_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptProfileSwitch.key,
            defaultValue = BooleanKey.NsClientAcceptProfileSwitch.defaultValue,
            titleResId = R.string.ns_receive_profile_switch,
            summaryResId = R.string.ns_receive_profile_switch_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptInsulin.key,
            defaultValue = BooleanKey.NsClientAcceptInsulin.defaultValue,
            titleResId = R.string.ns_receive_insulin,
            summaryResId = R.string.ns_receive_insulin_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptCarbs.key,
            defaultValue = BooleanKey.NsClientAcceptCarbs.defaultValue,
            titleResId = R.string.ns_receive_carbs,
            summaryResId = R.string.ns_receive_carbs_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptTherapyEvent.key,
            defaultValue = BooleanKey.NsClientAcceptTherapyEvent.defaultValue,
            titleResId = R.string.ns_receive_therapy_events,
            summaryResId = R.string.ns_receive_therapy_events_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptRunningMode.key,
            defaultValue = BooleanKey.NsClientAcceptRunningMode.defaultValue,
            titleResId = R.string.ns_receive_running_mode,
            summaryResId = R.string.ns_receive_running_mode_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientAcceptTbrEb.key,
            defaultValue = BooleanKey.NsClientAcceptTbrEb.defaultValue,
            titleResId = R.string.ns_receive_tbr_eb,
            summaryResId = R.string.ns_receive_tbr_eb_summary
        )

        // Alarm options category
        preferenceCategory(
            key = "ns_client_alarm_options",
            titleResId = R.string.ns_alarm_options
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientNotificationsFromAlarms.key,
            defaultValue = BooleanKey.NsClientNotificationsFromAlarms.defaultValue,
            titleResId = R.string.ns_alarms
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientNotificationsFromAnnouncements.key,
            defaultValue = BooleanKey.NsClientNotificationsFromAnnouncements.defaultValue,
            titleResId = R.string.ns_announcements
        )

        // IntKey.NsClientAlarmStaleData and IntKey.NsClientUrgentAlarmStaleData
        // These would need AdaptiveIntPreference compose equivalent

        // Connection settings category
        preferenceCategory(
            key = "ns_client_connection_options",
            titleResId = R.string.connection_settings_title
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientUseCellular.key,
            defaultValue = BooleanKey.NsClientUseCellular.defaultValue,
            titleResId = R.string.ns_cellular
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientUseRoaming.key,
            defaultValue = BooleanKey.NsClientUseRoaming.defaultValue,
            titleResId = R.string.ns_allow_roaming
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientUseWifi.key,
            defaultValue = BooleanKey.NsClientUseWifi.defaultValue,
            titleResId = R.string.ns_wifi
        )

        stringTextFieldPreference(
            sp = sp,
            key = StringKey.NsClientWifiSsids.key,
            defaultValue = StringKey.NsClientWifiSsids.defaultValue,
            titleResId = R.string.ns_wifi_ssids,
            summaryResId = R.string.ns_wifi_allowed_ssids
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientUseOnBattery.key,
            defaultValue = BooleanKey.NsClientUseOnBattery.defaultValue,
            titleResId = R.string.ns_battery
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientUseOnCharging.key,
            defaultValue = BooleanKey.NsClientUseOnCharging.defaultValue,
            titleResId = R.string.ns_charging
        )

        // Advanced settings category
        preferenceCategory(
            key = "ns_client_advanced",
            titleResId = app.aaps.core.ui.R.string.advanced_settings_title
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientLogAppStart.key,
            defaultValue = BooleanKey.NsClientLogAppStart.defaultValue,
            titleResId = R.string.ns_log_app_started_event
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientCreateAnnouncementsFromErrors.key,
            defaultValue = BooleanKey.NsClientCreateAnnouncementsFromErrors.defaultValue,
            titleResId = R.string.ns_create_announcements_from_errors_title,
            summaryResId = R.string.ns_create_announcements_from_errors_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientCreateAnnouncementsFromCarbsReq.key,
            defaultValue = BooleanKey.NsClientCreateAnnouncementsFromCarbsReq.defaultValue,
            titleResId = R.string.ns_create_announcements_from_carbs_req_title,
            summaryResId = R.string.ns_create_announcements_from_carbs_req_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.NsClientSlowSync.key,
            defaultValue = BooleanKey.NsClientSlowSync.defaultValue,
            titleResId = R.string.ns_sync_slow
        )
    }
}
