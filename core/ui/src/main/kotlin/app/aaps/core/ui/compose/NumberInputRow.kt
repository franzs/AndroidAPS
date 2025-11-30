package app.aaps.core.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
 * - Top row: Label (left, labelLarge) and current value (right, titleMedium bold in primary color)
 * - Bottom: Material 3 Slider with defined range and step increments
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
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            steps = ((maxValue - minValue) / step - 1).toInt(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
