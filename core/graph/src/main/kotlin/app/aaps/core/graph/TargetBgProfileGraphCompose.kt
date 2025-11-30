package app.aaps.core.graph

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import app.aaps.core.data.configuration.Constants
import app.aaps.core.data.model.GlucoseUnit
import app.aaps.core.graph.vico.Square
import app.aaps.core.interfaces.profile.Profile
import app.aaps.core.ui.compose.AapsTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

/**
 * Composable that displays target blood glucose range profile data as a line chart using Vico charting library.
 * Supports both single profile view and side-by-side comparison of two profiles.
 *
 * Target BG defines the desired blood glucose range with low and high values.
 * The chart visualizes this range as two lines (high and low) with an area fill between them.
 *
 * **Chart Features:**
 * - Line chart with square point connectors (stepped appearance)
 * - Displays both high and low target BG values at each hour (0-24) across the day
 * - Automatic unit conversion from mg/dL to mmol/L based on profile units
 * - Single mode: Shows high and low lines with 20% alpha area fill between them
 * - Comparison mode: Shows 4 lines total (high/low for each profile) with legend
 * - Vertical axis: Target BG (mg/dL or mmol/L)
 * - Horizontal axis: Hours of day (0-24)
 * - Zoom disabled, chart auto-fits to content
 *
 * **Visual Design:**
 * - Profile 1 high line: Uses profile1Color
 * - Profile 1 low line: Uses profile1Color with area fill (single mode only)
 * - Profile 2 high line: Uses profile2Color (comparison mode)
 * - Profile 2 low line: Uses profile2Color (comparison mode)
 * - Legend: Horizontal pill-shaped indicators showing profile names and colors
 * - Square point connector creates stepped line appearance matching target range time blocks
 * - Area fill visualizes the acceptable BG range
 *
 * @param modifier Modifier for the chart container
 * @param profile1 Primary profile to display (always shown, determines display units)
 * @param profile2 Optional second profile for comparison mode
 * @param profile1Name Display name for the primary profile (shown in legend for comparison)
 * @param profile2Name Display name for the second profile (required if profile2 is provided)
 * @param profile1Color Line and area fill color for profile 1 (default: theme profile1 color)
 * @param profile2Color Line color for profile 2 (default: theme profile2 color)
 *
 * @throws IllegalArgumentException if profile2 is provided but profile2Name is null
 */

private val LegendLabelKey = ExtraStore.Key<List<String>>()

/**
 * Converts a blood glucose value from mg/dL to the specified units.
 * Used for displaying target BG values in user's preferred glucose units.
 *
 * @param value The value in mg/dL
 * @param units Target glucose units (MGDL or MMOL_L)
 * @return The value in the target units (unchanged if MGDL, converted if MMOL_L)
 */
private fun fromMgdlToUnits(value: Double, units: GlucoseUnit): Double =
    if (units == GlucoseUnit.MGDL) value else value * Constants.MGDL_TO_MMOLL

@Composable
fun TargetBgProfileGraphCompose(
    modifier: Modifier = Modifier,
    profile1: Profile,
    profile2: Profile? = null,
    profile1Name: String,
    profile2Name: String? = null,
    profile1Color: Color = AapsTheme.profileHelperColors.profile1,
    profile2Color: Color = AapsTheme.profileHelperColors.profile2
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    // Check profile2Name is provided if profile2 is provided
    require(profile2?.let { profile2Name != null } ?: true)

    LaunchedEffect(profile1, profile2) {
        // Create data with points at each hour
        val units = profile1.units
        val targetLowValues1 = (0..24).map { hour ->
            fromMgdlToUnits(
                profile1.getTargetLowMgdlTimeFromMidnight((hour.coerceAtMost(23)) * 60 * 60),
                units
            )
        }
        val targetHighValues1 = (0..24).map { hour ->
            fromMgdlToUnits(
                profile1.getTargetHighMgdlTimeFromMidnight((hour.coerceAtMost(23)) * 60 * 60),
                units
            )
        }

        if (profile2 != null && profile2Name != null) {
            val targetLowValues2 = (0..24).map { hour ->
                fromMgdlToUnits(
                    profile2.getTargetLowMgdlTimeFromMidnight((hour.coerceAtMost(23)) * 60 * 60),
                    units
                )
            }
            val targetHighValues2 = (0..24).map { hour ->
                fromMgdlToUnits(
                    profile2.getTargetHighMgdlTimeFromMidnight((hour.coerceAtMost(23)) * 60 * 60),
                    units
                )
            }
            modelProducer.runTransaction {
                lineSeries {
                    // Profile 2 high and low
                    series(y = targetHighValues2)
                    series(y = targetLowValues2)
                    // Profile 1 high and low
                    series(y = targetHighValues1)
                    series(y = targetLowValues1)
                }
                extras { extraStore -> extraStore[LegendLabelKey] = listOf(profile2Name, profile1Name) }
            }
        } else {
            modelProducer.runTransaction {
                lineSeries {
                    series(y = targetHighValues1)
                    series(y = targetLowValues1)
                }
            }
        }
    }

    val lineColors = listOf(profile2Color, profile1Color)
    val legendItemLabelComponent = rememberTextComponent(vicoTheme.textColor)
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = if (profile2 != null) {
                    LineCartesianLayer.LineProvider.series(
                        // Profile 2 high line
                        LineCartesianLayer.Line(
                            fill = remember { LineCartesianLayer.LineFill.single(Fill(profile2Color.toArgb())) },
                            pointConnector = Square
                        ),
                        // Profile 2 low line with area fill
                        LineCartesianLayer.Line(
                            fill = remember { LineCartesianLayer.LineFill.single(Fill(profile2Color.toArgb())) },
                            pointConnector = Square
                        ),
                        // Profile 1 high line
                        LineCartesianLayer.Line(
                            fill = remember { LineCartesianLayer.LineFill.single(Fill(profile1Color.toArgb())) },
                            pointConnector = Square
                        ),
                        // Profile 1 low line with area fill
                        LineCartesianLayer.Line(
                            fill = remember { LineCartesianLayer.LineFill.single(Fill(profile1Color.toArgb())) },
                            pointConnector = Square
                        )
                    )
                } else {
                    LineCartesianLayer.LineProvider.series(
                        // High line
                        LineCartesianLayer.Line(
                            fill = remember { LineCartesianLayer.LineFill.single(Fill(profile1Color.toArgb())) },
                            pointConnector = Square
                        ),
                        // Low line with area fill
                        LineCartesianLayer.Line(
                            fill = remember { LineCartesianLayer.LineFill.single(Fill(profile1Color.toArgb())) },
                            areaFill = remember {
                                LineCartesianLayer.AreaFill.single(
                                    Fill(profile1Color.copy(alpha = 0.2f).toArgb())
                                )
                            },
                            pointConnector = Square
                        )
                    )
                }
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
            legend = if (profile2 != null) {
                rememberHorizontalLegend(
                    items = { extraStore ->
                        extraStore[LegendLabelKey].forEachIndexed { index, label ->
                            add(
                                LegendItem(
                                    shapeComponent(fill(lineColors[index]), CorneredShape.Pill),
                                    legendItemLabelComponent,
                                    label,
                                )
                            )
                        }
                    },
                    padding = insets(top = 8.dp, start = 8.dp),
                )
            } else null
        ),
        modelProducer = modelProducer,
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
