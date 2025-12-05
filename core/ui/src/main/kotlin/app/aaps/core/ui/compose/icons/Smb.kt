package app.aaps.core.ui.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon for Super Micro Bolus (SMB).
 * Represents a drop/bolus with a downward arrow.
 *
 * Bounding box: x: 3.4-44.6, y: 3.0-44.4 (viewport: 48x48, includes stroke, ~90% width)
 */
val Smb: ImageVector by lazy {
    ImageVector.Builder(
        name = "Smb",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Drop/bolus shape
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 6.42f)
            curveToRelative(-4.40f, 6.00f, -8.00f, 12.01f, -8.00f, 20.01f)
            curveToRelative(0f, 8.80f, 7.21f, 16.01f, 16.01f, 16.01f)
            reflectiveCurveToRelative(16.01f, -7.21f, 16.01f, -16.01f)
            curveToRelative(0f, -8.00f, -3.60f, -14.01f, -8.00f, -20.01f)
            close()
        }
        // Vertical line (arrow shaft)
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 24.48f)
            verticalLineTo(40.44f)
        }
        // Arrow head (down)
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(16.27f, 32.63f)
            lineTo(24f, 40.44f)
            lineTo(31.73f, 32.63f)
        }
    }.build()
}
