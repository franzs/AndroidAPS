/*
 * Adaptive Preference Support for Jetpack Compose
 * Provides preference components that support PreferenceKey types with visibility/validation logic
 */

package app.aaps.core.ui.compose.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.BooleanPreferenceKey
import app.aaps.core.keys.interfaces.DoublePreferenceKey
import app.aaps.core.keys.interfaces.IntPreferenceKey
import app.aaps.core.keys.interfaces.PreferenceKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.keys.interfaces.StringPreferenceKey

/**
 * Data class holding visibility and enabled state for a preference
 */
data class PreferenceVisibility(
    val visible: Boolean,
    val enabled: Boolean
)

/**
 * Calculates visibility and enabled state for a preference based on mode settings and dependencies.
 */
fun calculatePreferenceVisibility(
    preferenceKey: PreferenceKey,
    preferences: Preferences,
    config: Config,
    engineeringModeOnly: Boolean = false
): PreferenceVisibility {
    var visible = true
    var enabled = true

    // Check simple mode
    if (preferences.simpleMode && preferenceKey.defaultedBySM) {
        visible = false
    }

    // Check APS mode
    if (preferences.apsMode && !preferenceKey.showInApsMode) {
        visible = false
        enabled = false
    }

    // Check NSClient mode
    if (preferences.nsclientMode && !preferenceKey.showInNsClientMode) {
        visible = false
        enabled = false
    }

    // Check PumpControl mode
    if (preferences.pumpControlMode && !preferenceKey.showInPumpControlMode) {
        visible = false
        enabled = false
    }

    // Check engineering mode
    if (!config.isEngineeringMode() && engineeringModeOnly) {
        visible = false
        enabled = false
    }

    // Check dependency
    preferenceKey.dependency?.let {
        if (!preferences.get(it)) {
            visible = false
        }
    }

    // Check negative dependency
    preferenceKey.negativeDependency?.let {
        if (preferences.get(it)) {
            visible = false
        }
    }

    return PreferenceVisibility(visible, enabled)
}

/**
 * Remembers a MutableState for a BooleanPreferenceKey backed by Preferences.
 */
@Composable
fun rememberPreferenceBooleanState(
    preferences: Preferences,
    key: BooleanPreferenceKey
): MutableState<Boolean> {
    return remember(key, preferences) {
        PreferenceBooleanState(preferences, key)
    }
}

/**
 * Remembers a MutableState for a StringPreferenceKey backed by Preferences.
 */
@Composable
fun rememberPreferenceStringState(
    preferences: Preferences,
    key: StringPreferenceKey
): MutableState<String> {
    return remember(key, preferences) {
        PreferenceStringState(preferences, key)
    }
}

/**
 * Remembers a MutableState for an IntPreferenceKey backed by Preferences.
 */
@Composable
fun rememberPreferenceIntState(
    preferences: Preferences,
    key: IntPreferenceKey
): MutableState<Int> {
    return remember(key, preferences) {
        PreferenceIntState(preferences, key)
    }
}

/**
 * Remembers a MutableState for a DoublePreferenceKey backed by Preferences.
 */
@Composable
fun rememberPreferenceDoubleState(
    preferences: Preferences,
    key: DoublePreferenceKey
): MutableState<Double> {
    return remember(key, preferences) {
        PreferenceDoubleState(preferences, key)
    }
}

@Stable
private class PreferenceBooleanState(
    private val preferences: Preferences,
    private val key: BooleanPreferenceKey
) : MutableState<Boolean> {

    private val state = mutableStateOf(preferences.get(key))

    override var value: Boolean
        get() = state.value
        set(value) {
            state.value = value
            preferences.put(key, value)
        }

    override fun component1(): Boolean = value
    override fun component2(): (Boolean) -> Unit = { value = it }
}

@Stable
private class PreferenceStringState(
    private val preferences: Preferences,
    private val key: StringPreferenceKey
) : MutableState<String> {

    private val state = mutableStateOf(preferences.get(key))

    override var value: String
        get() = state.value
        set(value) {
            state.value = value
            preferences.put(key, value)
        }

    override fun component1(): String = value
    override fun component2(): (String) -> Unit = { value = it }
}

@Stable
private class PreferenceIntState(
    private val preferences: Preferences,
    private val key: IntPreferenceKey
) : MutableState<Int> {

    private val state = mutableStateOf(preferences.get(key))

    override var value: Int
        get() = state.value
        set(value) {
            // Clamp to min/max
            val clampedValue = value.coerceIn(key.min, key.max)
            state.value = clampedValue
            preferences.put(key, clampedValue)
        }

    override fun component1(): Int = value
    override fun component2(): (Int) -> Unit = { value = it }
}

@Stable
private class PreferenceDoubleState(
    private val preferences: Preferences,
    private val key: DoublePreferenceKey
) : MutableState<Double> {

    private val state = mutableStateOf(preferences.get(key))

    override var value: Double
        get() = state.value
        set(value) {
            // Clamp to min/max
            val clampedValue = value.coerceIn(key.min, key.max)
            state.value = clampedValue
            preferences.put(key, clampedValue)
        }

    override fun component1(): Double = value
    override fun component2(): (Double) -> Unit = { value = it }
}

// =================================
// Adaptive Switch Preference
// =================================

/**
 * Adaptive switch preference that uses BooleanPreferenceKey directly.
 * Handles visibility based on mode settings and dependencies.
 * Uses resource IDs to avoid cross-module Compose compiler issues.
 */
fun LazyListScope.adaptiveSwitchPreference(
    preferences: Preferences,
    config: Config,
    booleanKey: BooleanPreferenceKey,
    titleResId: Int,
    summaryResId: Int? = null,
    summaryOnResId: Int? = null,
    summaryOffResId: Int? = null
) {
    val visibility = calculatePreferenceVisibility(
        preferenceKey = booleanKey,
        preferences = preferences,
        config = config,
        engineeringModeOnly = booleanKey.engineeringModeOnly
    )

    if (!visibility.visible) return

    item(key = booleanKey.key, contentType = "AdaptiveSwitchPreference") {
        val state = rememberPreferenceBooleanState(preferences, booleanKey)
        SwitchPreference(
            state = state,
            title = { Text(stringResource(titleResId)) },
            summary = when {
                summaryOnResId != null && summaryOffResId != null -> {
                    { Text(stringResource(if (state.value) summaryOnResId else summaryOffResId)) }
                }

                summaryResId != null                              -> {
                    { Text(stringResource(summaryResId)) }
                }

                else                                              -> null
            },
            enabled = visibility.enabled
        )
    }
}

// =================================
// Adaptive Int Preference
// =================================

/**
 * Adaptive int preference that uses IntPreferenceKey directly.
 * Handles visibility based on mode settings and dependencies.
 * Validates input against min/max values.
 * Uses resource IDs to avoid cross-module Compose compiler issues.
 */
fun LazyListScope.adaptiveIntPreference(
    preferences: Preferences,
    config: Config,
    intKey: IntPreferenceKey,
    titleResId: Int,
    unit: String = "",
    showRange: Boolean = true
) {
    val visibility = calculatePreferenceVisibility(
        preferenceKey = intKey,
        preferences = preferences,
        config = config,
        engineeringModeOnly = intKey.engineeringModeOnly
    )

    if (!visibility.visible) return

    item(key = intKey.key, contentType = "AdaptiveIntPreference") {
        val state = rememberPreferenceIntState(preferences, intKey)
        val value = state.value

        TextFieldPreference(
            state = state,
            title = { Text(stringResource(titleResId)) },
            textToValue = { text ->
                text.toIntOrNull()?.coerceIn(intKey.min, intKey.max)
            },
            enabled = visibility.enabled,
            summary = if (showRange) {
                { Text("$value$unit (${intKey.min}-${intKey.max})") }
            } else {
                { Text("$value$unit") }
            }
        )
    }
}

// =================================
// Adaptive Double Preference
// =================================

/**
 * Adaptive double preference that uses DoublePreferenceKey directly.
 * Handles visibility based on mode settings and dependencies.
 * Validates input against min/max values.
 * Uses resource IDs to avoid cross-module Compose compiler issues.
 */
fun LazyListScope.adaptiveDoublePreference(
    preferences: Preferences,
    config: Config,
    doubleKey: DoublePreferenceKey,
    titleResId: Int,
    unit: String = "",
    showRange: Boolean = true
) {
    val visibility = calculatePreferenceVisibility(
        preferenceKey = doubleKey,
        preferences = preferences,
        config = config
    )

    // Also check calculatedBySM for doubles
    if (!visibility.visible || (preferences.simpleMode && doubleKey.calculatedBySM)) return

    item(key = doubleKey.key, contentType = "AdaptiveDoublePreference") {
        val state = rememberPreferenceDoubleState(preferences, doubleKey)
        val value = state.value

        TextFieldPreference(
            state = state,
            title = { Text(stringResource(titleResId)) },
            textToValue = { text ->
                text.toDoubleOrNull()?.coerceIn(doubleKey.min, doubleKey.max)
            },
            enabled = visibility.enabled,
            summary = if (showRange) {
                { Text("$value$unit (${doubleKey.min}-${doubleKey.max})") }
            } else {
                { Text("$value$unit") }
            }
        )
    }
}

// =================================
// Adaptive String Preference
// =================================

/**
 * Adaptive string preference that uses StringPreferenceKey directly.
 * Handles visibility based on mode settings and dependencies.
 * Uses resource IDs to avoid cross-module Compose compiler issues.
 */
fun LazyListScope.adaptiveStringPreference(
    preferences: Preferences,
    config: Config,
    stringKey: StringPreferenceKey,
    titleResId: Int,
    summaryResId: Int? = null,
    isPassword: Boolean = false
) {
    val visibility = calculatePreferenceVisibility(
        preferenceKey = stringKey,
        preferences = preferences,
        config = config
    )

    if (!visibility.visible) return

    item(key = stringKey.key, contentType = "AdaptiveStringPreference") {
        val state = rememberPreferenceStringState(preferences, stringKey)
        val value = state.value

        TextFieldPreference(
            state = state,
            title = { Text(stringResource(titleResId)) },
            textToValue = { it },
            enabled = visibility.enabled,
            summary = when {
                isPassword || stringKey.isPassword -> {
                    { if (value.isNotEmpty()) Text("••••••••") else summaryResId?.let { Text(stringResource(it)) } }
                }

                value.isNotEmpty()                 -> {
                    { Text(value) }
                }

                summaryResId != null               -> {
                    { Text(stringResource(summaryResId)) }
                }

                else                               -> null
            }
        )
    }
}
