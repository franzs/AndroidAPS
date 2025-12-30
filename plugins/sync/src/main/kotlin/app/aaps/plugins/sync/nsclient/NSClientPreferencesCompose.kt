package app.aaps.plugins.sync.nsclient

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.StringKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveStringPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.sync.R

/**
 * Compose implementation of NSClient preferences.
 */
class NSClientPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // NSClient settings category
        preferenceCategory(
            key = "ns_client_settings",
            titleResId = R.string.ns_client_title
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.NsClientUrl,
            titleResId = R.string.ns_client_url_title
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.NsClientApiSecret,
            titleResId = R.string.ns_client_secret_title
        )

        // Synchronization options category
        preferenceCategory(
            key = "ns_client_synchronization",
            titleResId = R.string.ns_sync_options
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUploadData,
            titleResId = R.string.ns_upload,
            summaryResId = R.string.ns_upload_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.BgSourceUploadToNs,
            titleResId = app.aaps.core.ui.R.string.do_ns_upload_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptCgmData,
            titleResId = R.string.ns_receive_cgm,
            summaryResId = R.string.ns_receive_cgm_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptProfileStore,
            titleResId = R.string.ns_receive_profile_store,
            summaryResId = R.string.ns_receive_profile_store_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptTempTarget,
            titleResId = R.string.ns_receive_temp_target,
            summaryResId = R.string.ns_receive_temp_target_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptProfileSwitch,
            titleResId = R.string.ns_receive_profile_switch,
            summaryResId = R.string.ns_receive_profile_switch_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptInsulin,
            titleResId = R.string.ns_receive_insulin,
            summaryResId = R.string.ns_receive_insulin_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptCarbs,
            titleResId = R.string.ns_receive_carbs,
            summaryResId = R.string.ns_receive_carbs_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptTherapyEvent,
            titleResId = R.string.ns_receive_therapy_events,
            summaryResId = R.string.ns_receive_therapy_events_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptRunningMode,
            titleResId = R.string.ns_receive_running_mode,
            summaryResId = R.string.ns_receive_running_mode_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientAcceptTbrEb,
            titleResId = R.string.ns_receive_tbr_eb,
            summaryResId = R.string.ns_receive_tbr_eb_summary
        )

        // Alarm options category
        preferenceCategory(
            key = "ns_client_alarm_options",
            titleResId = R.string.ns_alarm_options
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientNotificationsFromAlarms,
            titleResId = R.string.ns_alarms
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientNotificationsFromAnnouncements,
            titleResId = R.string.ns_announcements
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.NsClientAlarmStaleData,
            titleResId = R.string.ns_alarm_stale_data_value_label
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.NsClientUrgentAlarmStaleData,
            titleResId = R.string.ns_alarm_urgent_stale_data_value_label
        )

        // Connection options category
        preferenceCategory(
            key = "ns_client_connection_options",
            titleResId = R.string.connection_settings_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseCellular,
            titleResId = R.string.ns_cellular
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseRoaming,
            titleResId = R.string.ns_allow_roaming
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseWifi,
            titleResId = R.string.ns_wifi
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.NsClientWifiSsids,
            titleResId = R.string.ns_wifi_ssids
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseOnBattery,
            titleResId = R.string.ns_battery
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseOnCharging,
            titleResId = R.string.ns_charging
        )

        // Advanced settings category
        preferenceCategory(
            key = "ns_client_advanced",
            titleResId = app.aaps.core.ui.R.string.advanced_settings_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientLogAppStart,
            titleResId = R.string.ns_log_app_started_event
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientCreateAnnouncementsFromErrors,
            titleResId = R.string.ns_create_announcements_from_errors_title,
            summaryResId = R.string.ns_create_announcements_from_errors_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientCreateAnnouncementsFromCarbsReq,
            titleResId = R.string.ns_create_announcements_from_carbs_req_title,
            summaryResId = R.string.ns_create_announcements_from_carbs_req_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientSlowSync,
            titleResId = R.string.ns_sync_slow
        )
    }
}
