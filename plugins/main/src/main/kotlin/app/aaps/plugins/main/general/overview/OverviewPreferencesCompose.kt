package app.aaps.plugins.main.general.overview

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.nsclient.NSSettingsStatus
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.sharedPreferences.SP
import app.aaps.core.keys.BooleanKey
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.core.ui.compose.preference.switchPreference
import app.aaps.plugins.main.R

/**
 * Compose implementation of Overview preferences.
 */
class OverviewPreferencesCompose(
    private val sp: SP,
    private val rh: ResourceHelper,
    private val activePlugin: ActivePlugin,
    private val nsSettingStatus: NSSettingsStatus
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Overview category
        preferenceCategory(
            key = "overview_settings",
            titleResId = R.string.overview
        )

        // Keep Screen On
        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewKeepScreenOn.key,
            defaultValue = BooleanKey.OverviewKeepScreenOn.defaultValue,
            titleResId = R.string.keep_screen_on_title,
            summaryResId = R.string.keep_screen_on_summary
        )

        // Buttons Settings Category
        preferenceCategory(
            key = "overview_buttons_settings",
            titleResId = R.string.overview_buttons_selection
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowTreatmentButton.key,
            defaultValue = BooleanKey.OverviewShowTreatmentButton.defaultValue,
            titleResId = app.aaps.core.ui.R.string.treatments
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowWizardButton.key,
            defaultValue = BooleanKey.OverviewShowWizardButton.defaultValue,
            titleResId = R.string.calculator_label
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowInsulinButton.key,
            defaultValue = BooleanKey.OverviewShowInsulinButton.defaultValue,
            titleResId = app.aaps.core.ui.R.string.configbuilder_insulin
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowCarbsButton.key,
            defaultValue = BooleanKey.OverviewShowCarbsButton.defaultValue,
            titleResId = app.aaps.core.ui.R.string.carbs
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowCgmButton.key,
            defaultValue = BooleanKey.OverviewShowCgmButton.defaultValue,
            titleResId = R.string.cgm,
            summaryResId = R.string.show_cgm_button_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowCalibrationButton.key,
            defaultValue = BooleanKey.OverviewShowCalibrationButton.defaultValue,
            titleResId = app.aaps.core.ui.R.string.calibration,
            summaryResId = R.string.show_calibration_button_summary
        )

        // Display Settings
        preferenceCategory(
            key = "overview_display_settings",
            titleResId = R.string.overview_buttons_selection
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShortTabTitles.key,
            defaultValue = BooleanKey.OverviewShortTabTitles.defaultValue,
            titleResId = R.string.short_tabtitles
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowNotesInDialogs.key,
            defaultValue = BooleanKey.OverviewShowNotesInDialogs.defaultValue,
            titleResId = R.string.overview_show_notes_field_in_dialogs_title
        )

        // Status Lights Category
        preferenceCategory(
            key = "statuslights_overview_advanced",
            titleResId = app.aaps.core.ui.R.string.statuslights
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewShowStatusLights.key,
            defaultValue = BooleanKey.OverviewShowStatusLights.defaultValue,
            titleResId = R.string.show_statuslights
        )

        // Bolus Settings
        preferenceCategory(
            key = "overview_bolus_settings",
            titleResId = app.aaps.core.ui.R.string.bolus
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewUseBolusAdvisor.key,
            defaultValue = BooleanKey.OverviewUseBolusAdvisor.defaultValue,
            titleResId = R.string.enable_bolus_advisor,
            summaryResId = R.string.enable_bolus_advisor_summary
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewUseBolusReminder.key,
            defaultValue = BooleanKey.OverviewUseBolusReminder.defaultValue,
            titleResId = R.string.enablebolusreminder,
            summaryResId = R.string.enablebolusreminder_summary
        )

        // Advanced Settings Category
        preferenceCategory(
            key = "overview_advanced_settings",
            titleResId = app.aaps.core.ui.R.string.advanced_settings_title
        )

        switchPreference(
            sp = sp,
            key = BooleanKey.OverviewUseSuperBolus.key,
            defaultValue = BooleanKey.OverviewUseSuperBolus.defaultValue,
            titleResId = R.string.enablesuperbolus,
            summaryResId = R.string.enablesuperbolus_summary
        )
    }
}
