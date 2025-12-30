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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Adds a preference category header to the lazy list.
 * Note: This function uses a string resource ID instead of a composable lambda to avoid
 * cross-module inline function issues with the Compose compiler.
 */
fun LazyListScope.preferenceCategory(
    key: String,
    titleResId: Int,
) {
    item(key = key, contentType = "PreferenceCategory") {
        PreferenceCategory(titleResId = titleResId)
    }
}

/**
 * Adds a preference category header to the lazy list with custom modifier.
 */
fun LazyListScope.preferenceCategory(
    key: String,
    titleResId: Int,
    modifier: Modifier,
) {
    item(key = key, contentType = "PreferenceCategory") {
        PreferenceCategory(titleResId = titleResId, modifier = modifier)
    }
}

@Composable
fun PreferenceCategory(titleResId: Int, modifier: Modifier = Modifier) {
    PreferenceCategory(title = { Text(stringResource(titleResId)) }, modifier = modifier)
}

@Composable
fun PreferenceCategory(title: String, modifier: Modifier = Modifier) {
    PreferenceCategory(title = { Text(title) }, modifier = modifier)
}

@Composable
fun PreferenceCategory(title: @Composable () -> Unit, modifier: Modifier = Modifier) {
    BasicPreference(
        textContainer = {
            val theme = LocalPreferenceTheme.current
            Box(
                modifier = Modifier.padding(theme.categoryPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                CompositionLocalProvider(LocalContentColor provides theme.categoryColor) {
                    ProvideTextStyle(value = theme.categoryTextStyle, content = title)
                }
            }
        },
        modifier = modifier,
    )
}
