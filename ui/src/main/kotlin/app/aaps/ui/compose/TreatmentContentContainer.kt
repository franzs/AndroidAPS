package app.aaps.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aaps.ui.R

/**
 * Container for treatment screen content that handles loading and empty states.
 * Shows loading indicator, empty message, or content based on state.
 */
@Composable
fun TreatmentContentContainer(
    isLoading: Boolean,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            isEmpty   -> {
                Text(
                    text = stringResource(R.string.no_records_available),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(50.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else      -> content()
        }
    }
}
