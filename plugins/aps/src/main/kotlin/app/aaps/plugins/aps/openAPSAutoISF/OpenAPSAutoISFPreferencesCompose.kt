package app.aaps.plugins.aps.openAPSAutoISF

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
 * Compose implementation of OpenAPS AutoISF preferences.
 */
class OpenAPSAutoISFPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Main OpenAPS AutoISF settings category
        preferenceCategory(
            key = "openapsautoisf_settings",
            titleResId = R.string.openaps_auto_isf
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
            booleanKey = BooleanKey.ApsUseAutosens,
            titleResId = R.string.openapsama_use_autosens
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
            booleanKey = BooleanKey.ApsAutoIsfHighTtRaisesSens,
            titleResId = R.string.high_temptarget_raises_sensitivity_title,
            summaryResId = R.string.high_temptarget_raises_sensitivity_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsAutoIsfLowTtLowersSens,
            titleResId = R.string.low_temptarget_lowers_sensitivity_title,
            summaryResId = R.string.low_temptarget_lowers_sensitivity_summary
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsAutoIsfHalfBasalExerciseTarget,
            titleResId = R.string.half_basal_exercise_target_title
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

        // AutoISF settings category
        preferenceCategory(
            key = "auto_isf_settings",
            titleResId = R.string.autoISF_settings_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsUseAutoIsfWeights,
            titleResId = R.string.openapsama_enable_autoISF,
            summaryResId = R.string.openapsama_enable_autoISF
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfMin,
            titleResId = R.string.openapsama_autoISF_min
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfMax,
            titleResId = R.string.openapsama_autoISF_max
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfBgAccelWeight,
            titleResId = R.string.openapsama_bgAccel_ISF_weight
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfBgBrakeWeight,
            titleResId = R.string.openapsama_bgBrake_ISF_weight
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfLowBgWeight,
            titleResId = R.string.openapsama_lower_ISFrange_weight
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfHighBgWeight,
            titleResId = R.string.openapsama_higher_ISFrange_weight
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfPpWeight,
            titleResId = R.string.openapsama_pp_ISF_weight
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfDuraWeight,
            titleResId = R.string.openapsama_dura_ISF_weight
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.ApsAutoIsfIobThPercent,
            titleResId = R.string.openapsama_iob_threshold_percent
        )

        // SMB Delivery settings category
        preferenceCategory(
            key = "smb_delivery_settings",
            titleResId = R.string.smb_delivery_settings_title
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfSmbDeliveryRatio,
            titleResId = R.string.openapsama_smb_delivery_ratio
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfSmbDeliveryRatioMin,
            titleResId = R.string.openapsama_smb_delivery_ratio_min
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfSmbDeliveryRatioMax,
            titleResId = R.string.openapsama_smb_delivery_ratio_max
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfSmbDeliveryRatioBgRange,
            titleResId = R.string.openapsama_smb_delivery_ratio_bg_range
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAutoIsfSmbMaxRangeExtension,
            titleResId = R.string.openapsama_smb_max_range_extension
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.ApsAutoIsfSmbOnEvenTarget,
            titleResId = R.string.enableSMB_EvenOn_OddOff_always,
            summaryResId = R.string.enableSMB_EvenOn_OddOff_always_summary
        )
    }
}
