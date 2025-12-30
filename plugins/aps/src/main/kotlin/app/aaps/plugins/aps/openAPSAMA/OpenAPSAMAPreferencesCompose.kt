package app.aaps.plugins.aps.openAPSAMA

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.DoubleKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveDoublePreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.aps.R

/**
 * Compose implementation of OpenAPS AMA preferences.
 */
class OpenAPSAMAPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // OpenAPS AMA settings category
        preferenceCategory(
            key = "openapsma_settings",
            titleResId = R.string.openapsama
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
            doubleKey = DoubleKey.ApsAmaMaxIob,
            titleResId = R.string.openapsma_max_iob_title
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
            booleanKey = BooleanKey.ApsAmaAutosensAdjustTargets,
            titleResId = R.string.openapsama_autosens_adjust_targets,
            summaryResId = R.string.openapsama_autosens_adjust_targets_summary
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAmaMin5MinCarbsImpact,
            titleResId = R.string.openapsama_min_5m_carb_impact
        )

        // Advanced settings category
        preferenceCategory(
            key = "absorption_ama_advanced",
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

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsAmaBolusSnoozeDivisor,
            titleResId = R.string.openapsama_bolus_snooze_dia_divisor
        )
    }
}
