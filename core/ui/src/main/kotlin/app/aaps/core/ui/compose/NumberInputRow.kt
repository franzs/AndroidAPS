package app.aaps.core.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

/**
 * Composable that displays a numeric input with label, current value, and Material 3 slider.
 * Used for inputting profile calculation parameters like age, TDD, weight, and basal percentage.
 *
 * **Layout:**
 * ```
 * Label                    42
 * ──────○────────────────
 * ```
 *
 * The component displays:
 * - Top row: Label (left, labelLarge) and current value (right, titleMedium bold in primary color, clickable)
 * - Bottom: Material 3 Slider with +/- buttons
 * - Clicking on value opens a dialog for direct input
 *
 * **Value Display:**
 * - Value is displayed as an integer (decimal portion is truncated)
 * - Slider supports fractional values, but display shows whole numbers only
 * - Suitable for parameters that are conceptually integers (age, weight) or where decimals aren't needed in display
 *
 * @param label Display label for the input (e.g., "Age", "TDD", "Weight")
 * @param value Current numeric value (can be fractional, displayed as integer)
 * @param onValueChange Callback invoked when slider value changes, receives new value as Double
 * @param minValue Minimum allowed value for the slider range
 * @param maxValue Maximum allowed value for the slider range
 * @param step Step increment for slider (determines number of discrete positions)
 * @param modifier Modifier for the root Column container
 */
@Composable
fun NumberInputRow(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    minValue: Double,
    maxValue: Double,
    step: Double,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val valueFormat = remember { DecimalFormat("0") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        SliderWithButtons(
            value = value,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            step = step,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog) {
        ValueInputDialog(
            currentValue = value,
            valueRange = minValue..maxValue,
            step = step,
            label = label,
            valueFormat = valueFormat,
            onValueConfirm = onValueChange,
            onDismiss = { showDialog = false }
        )
    }
}
