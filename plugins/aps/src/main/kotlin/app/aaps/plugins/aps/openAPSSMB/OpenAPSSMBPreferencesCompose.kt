package app.aaps.plugins.aps.openAPSSMB

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.DoubleKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveDoublePreference
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.aps.R

/**
 * Compose implementation of OpenAPS SMB preferences.
 */
class OpenAPSSMBPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // OpenAPS SMB settings category
        preferenceCategory(
            key = "openapssmb_settings",
            titleResId = R.string.openapssmb
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsMaxBasal,
            titleResId = R.string.openapsma_max_basal_title
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsSmbMaxIob,
            titleResId = R.string.openapssmb_max_iob_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseDynamicSensitivity,
            titleResId = R.string.use_dynamic_sensitivity_title,
            summaryResId = R.string.use_dynamic_sensitivity_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseAutosens,
            titleResId = R.string.openapsama_use_autosens
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsDynIsfAdjustmentFactor,
            titleResId = R.string.dyn_isf_adjust_title
        )

        // Note: UnitPreference (ApsLgsThreshold) not yet supported in Compose

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsDynIsfAdjustSensitivity,
            titleResId = R.string.dynisf_adjust_sensitivity,
            summaryResId = R.string.dynisf_adjust_sensitivity_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsSensitivityRaisesTarget,
            titleResId = R.string.sensitivity_raises_target_title,
            summaryResId = R.string.sensitivity_raises_target_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsResistanceLowersTarget,
            titleResId = R.string.resistance_lowers_target_title,
            summaryResId = R.string.resistance_lowers_target_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseSmb,
            titleResId = R.string.enable_smb,
            summaryResId = R.string.enable_smb_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseSmbWithHighTt,
            titleResId = R.string.enable_smb_with_high_temp_target,
            summaryResId = R.string.enable_smb_with_high_temp_target_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseSmbAlways,
            titleResId = R.string.enable_smb_always,
            summaryResId = R.string.enable_smb_always_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseSmbWithCob,
            titleResId = R.string.enable_smb_with_cob,
            summaryResId = R.string.enable_smb_with_cob_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseSmbWithLowTt,
            titleResId = R.string.enable_smb_with_temp_target,
            summaryResId = R.string.enable_smb_with_temp_target_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseSmbAfterCarbs,
            titleResId = R.string.enable_smb_after_carbs,
            summaryResId = R.string.enable_smb_after_carbs_summary
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsMaxSmbFrequency,
            titleResId = R.string.smb_interval_summary
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsMaxMinutesOfBasalToLimitSmb,
            titleResId = R.string.smb_max_minutes_summary
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsUamMaxMinutesOfBasalToLimitSmb,
            titleResId = R.string.uam_smb_max_minutes_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseUam,
            titleResId = R.string.enable_uam,
            summaryResId = R.string.enable_uam_summary
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsCarbsRequestThreshold,
            titleResId = R.string.carbs_req_threshold
        )

        // Advanced settings category
        preferenceCategory(
            key = "absorption_smb_advanced",
            titleResId = app.aaps.core.ui.R.string.advanced_settings_title
        )

        // Note: IntentPreference (ApsLinkToDocs) not yet supported in Compose

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsAlwaysUseShortDeltas,
            titleResId = R.string.always_use_short_avg,
            summaryResId = R.string.always_use_short_avg_summary
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsMaxDailyMultiplier,
            titleResId = R.string.openapsama_max_daily_safety_multiplier
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsMaxCurrentBasalMultiplier,
            titleResId = R.string.openapsama_current_basal_safety_multiplier
        )
    }
}
