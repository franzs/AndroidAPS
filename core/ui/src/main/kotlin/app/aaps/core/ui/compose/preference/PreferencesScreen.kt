package app.aaps.core.ui.compose.preference

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Main preferences screen composable.
 * Wraps content in a themed scaffold with lazy column.
 *
 * @param modifier Modifier for the screen
 * @param content Content builder for preferences
 */
@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    ProvidePreferenceTheme {
        val listState = rememberLazyListState()
        Scaffold(
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScrollIndicators(listState),
                state = listState,
                content = content
            )
        }
    }
}

/**
 * Preferences screen with multiple plugin contents.
 *
 * @param modifier Modifier for the screen
 * @param contents List of PreferenceScreenContent providers
 */
@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    contents: List<PreferenceScreenContent>
) {
    PreferencesScreen(modifier = modifier) {
        contents.forEach { content ->
            addPreferenceContent(content)
        }
    }
}
