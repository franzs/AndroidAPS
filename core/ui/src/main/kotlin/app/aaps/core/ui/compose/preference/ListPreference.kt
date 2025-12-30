/*
 * Copyright 2023 Google LLC
 * Adapted for AndroidAPS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.aaps.core.ui.compose.preference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.sharedPreferences.SP

enum class ListPreferenceType {
    ALERT_DIALOG,
    DROPDOWN_MENU,
}

fun <T> LazyListScope.listPreference(
    key: String,
    defaultValue: T,
    values: List<T>,
    title: @Composable (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    rememberState: @Composable () -> MutableState<T>,
    enabled: (T) -> Boolean = { true },
    icon: @Composable ((T) -> Unit)? = null,
    summary: @Composable ((T) -> Unit)? = null,
    type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
    valueToText: (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
    item: @Composable (value: T, currentValue: T, onClick: () -> Unit) -> Unit =
        ListPreferenceDefaults.item(type, valueToText),
) {
    item(key = key, contentType = "ListPreference") {
        val state = rememberState()
        val value by state
        ListPreference(
            state = state,
            values = values,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled(value),
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            type = type,
            valueToText = valueToText,
            item = item,
        )
    }
}

/**
 * Convenience function to create a string list preference backed by SP.
 * Uses resource IDs to avoid cross-module Compose compiler issues.
 *
 * @param entries Map of value to display text resource ID
 */
fun LazyListScope.stringListPreference(
    sp: SP,
    key: String,
    defaultValue: String,
    entries: Map<String, Int>,
    titleResId: Int,
    type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
) {
    item(key = key, contentType = "StringListPreference") {
        val state = rememberSPStringState(sp, key, defaultValue)
        val value by state
        // Pre-resolve resource strings during composition to use in non-composable lambda
        val resolvedEntries = entries.mapValues { (_, resId) -> stringResource(resId) }
        ListPreference(
            state = state,
            values = entries.keys.toList(),
            title = { Text(stringResource(titleResId)) },
            summary = { Text(resolvedEntries[value] ?: value) },
            type = type,
            valueToText = { v -> AnnotatedString(resolvedEntries[v] ?: v) },
        )
    }
}

/**
 * Convenience function to create an int list preference backed by SP.
 */
fun LazyListScope.intListPreference(
    sp: SP,
    key: String,
    defaultValue: Int,
    values: List<Int>,
    title: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: (Int) -> Boolean = { true },
    icon: @Composable ((Int) -> Unit)? = null,
    summary: @Composable ((Int) -> Unit)? = null,
    type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
    valueToText: (Int) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
    listPreference(
        key = key,
        defaultValue = defaultValue,
        values = values,
        title = title,
        modifier = modifier,
        rememberState = { rememberSPIntState(sp, key, defaultValue) },
        enabled = enabled,
        icon = icon,
        summary = summary,
        type = type,
        valueToText = valueToText,
    )
}

@Composable
fun <T> ListPreference(
    state: MutableState<T>,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
    valueToText: (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
    item: @Composable (value: T, currentValue: T, onClick: () -> Unit) -> Unit =
        ListPreferenceDefaults.item(type, valueToText),
) {
    var value by state
    ListPreference(
        value = value,
        onValueChange = { value = it },
        values = values,
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = summary,
        type = type,
        valueToText = valueToText,
        item = item,
    )
}

@Composable
fun <T> ListPreference(
    value: T,
    onValueChange: (T) -> Unit,
    values: List<T>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    type: ListPreferenceType = ListPreferenceType.ALERT_DIALOG,
    valueToText: (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
    item: @Composable (value: T, currentValue: T, onClick: () -> Unit) -> Unit =
        ListPreferenceDefaults.item(type, valueToText),
) {
    var openSelector by rememberSaveable { mutableStateOf(false) }
    if (openSelector) {
        when (type) {
            ListPreferenceType.ALERT_DIALOG  -> {
                PreferenceAlertDialog(
                    onDismissRequest = { openSelector = false },
                    title = title,
                    buttons = {
                        TextButton(onClick = { openSelector = false }) {
                            Text(text = stringResource(android.R.string.cancel))
                        }
                    },
                ) {
                    val lazyListState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScrollIndicators(lazyListState),
                        state = lazyListState,
                    ) {
                        items(values) { itemValue ->
                            item(itemValue, value) {
                                onValueChange(itemValue)
                                openSelector = false
                            }
                        }
                    }
                }
            }

            ListPreferenceType.DROPDOWN_MENU -> {
                val theme = LocalPreferenceTheme.current
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(theme.padding.copy(vertical = 0.dp))
                ) {
                    DropdownMenu(
                        expanded = openSelector,
                        onDismissRequest = { openSelector = false },
                    ) {
                        for (itemValue in values) {
                            item(itemValue, value) {
                                onValueChange(itemValue)
                                openSelector = false
                            }
                        }
                    }
                }
            }
        }
    }
    Preference(
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = summary,
    ) {
        openSelector = true
    }
}

object ListPreferenceDefaults {

    fun <T> item(
        type: ListPreferenceType,
        valueToText: (T) -> AnnotatedString,
    ): @Composable (value: T, currentValue: T, onClick: () -> Unit) -> Unit =
        when (type) {
            ListPreferenceType.ALERT_DIALOG  -> {
                { value, currentValue, onClick ->
                    DialogItem(value, currentValue, valueToText, onClick)
                }
            }

            ListPreferenceType.DROPDOWN_MENU -> {
                { value, currentValue, onClick ->
                    DropdownMenuItemContent(value, currentValue, valueToText, onClick)
                }
            }
        }

    @Composable
    private fun <T> DialogItem(
        value: T,
        currentValue: T,
        valueToText: (T) -> AnnotatedString,
        onClick: () -> Unit,
    ) {
        val selected = value == currentValue
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .selectable(selected, true, Role.RadioButton, onClick = onClick)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = valueToText(value),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    @Composable
    private fun <T> DropdownMenuItemContent(
        value: T,
        currentValue: T,
        valueToText: (T) -> AnnotatedString,
        onClick: () -> Unit,
    ) {
        DropdownMenuItem(
            text = { Text(text = valueToText(value)) },
            onClick = onClick,
            modifier =
                if (value == currentValue) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                } else {
                    Modifier
                },
            colors = MenuDefaults.itemColors(),
        )
    }
}
