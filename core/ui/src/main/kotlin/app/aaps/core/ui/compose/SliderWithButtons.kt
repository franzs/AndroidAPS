package app.aaps.core.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * A Slider with +/- buttons on each side for fine-grained value control.
 * Optionally displays a clickable value label that opens a dialog for direct input.
 *
 * @param value Current value
 * @param onValueChange Called when value changes
 * @param valueRange The range of values the slider can represent
 * @param step The step size for +/- buttons (default 0.1)
 * @param showValue Whether to show a clickable value label (default false)
 * @param valueFormat Format for the value label
 * @param unitLabel Unit label to show after value
 * @param dialogLabel Label for the input dialog
 * @param modifier Modifier for the Row container
 */
@Composable
fun SliderWithButtons(
    value: Double,
    onValueChange: (Double) -> Unit,
    valueRange: ClosedFloatingPointRange<Double>,
    step: Double = 0.1,
    showValue: Boolean = false,
    valueFormat: DecimalFormat = DecimalFormat("0.0"),
    unitLabel: String = "",
    dialogLabel: String? = null,
    modifier: Modifier = Modifier
) {
    val minValue = valueRange.start
    val maxValue = valueRange.endInclusive
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minus button
        FilledTonalIconButton(
            onClick = {
                val newValue = roundToStep(value - step, step).coerceIn(minValue, maxValue)
                onValueChange(newValue)
            },
            enabled = value > minValue,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "-",
                modifier = Modifier.size(16.dp)
            )
        }

        // Slider
        Slider(
            value = value.toFloat(),
            onValueChange = { newValue ->
                val rounded = roundToStep(newValue.toDouble(), step)
                onValueChange(rounded.coerceIn(minValue, maxValue))
            },
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            modifier = Modifier.weight(1f)
        )

        // Plus button
        FilledTonalIconButton(
            onClick = {
                val newValue = roundToStep(value + step, step).coerceIn(minValue, maxValue)
                onValueChange(newValue)
            },
            enabled = value < maxValue,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "+",
                modifier = Modifier.size(16.dp)
            )
        }

        // Optional clickable value label
        if (showValue) {
            Text(
                text = "${valueFormat.format(value)}${if (unitLabel.isNotEmpty()) " $unitLabel" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .widthIn(min = if (unitLabel.isEmpty()) 40.dp else 56.dp)
                    .clickable { showDialog = true }
                    .padding(start = 4.dp)
            )
        }
    }

    // Value input dialog
    if (showDialog) {
        ValueInputDialog(
            currentValue = value,
            valueRange = valueRange,
            step = step,
            label = dialogLabel,
            unitLabel = unitLabel,
            valueFormat = valueFormat,
            onValueConfirm = onValueChange,
            onDismiss = { showDialog = false }
        )
    }
}

private fun roundToStep(value: Double, step: Double): Double {
    return (value / step).roundToInt() * step
}
