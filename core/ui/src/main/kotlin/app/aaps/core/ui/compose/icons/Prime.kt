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
 * Icon for priming/cannula fill.
 * Represents insulin pump priming operation.
 *
 * Bounding box: x: 3.2-44.8, y: 3.1-44.9 (viewport: 48x48, includes stroke, ~87% width)
 */
val Prime: ImageVector by lazy {
    ImageVector.Builder(
        name = "Prime",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Main syringe body (rotated capsule)
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 3f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(9.026f, 10.692f)
            lineTo(10.146f, 9.57f)
            arcTo(2.7f, 2.7f, 90f, false, true, 13.365f, 9.57f)
            lineTo(36.184f, 32.389f)
            arcTo(2.7f, 2.7f, 90f, false, true, 36.184f, 35.608f)
            lineTo(35.012f, 36.78f)
            arcTo(2.7f, 2.7f, 90f, false, true, 31.793f, 36.78f)
            lineTo(9.026f, 14.011f)
            arcTo(2.7f, 2.7f, 0f, false, true, 9.026f, 10.692f)
            close()
        }
        // Upper plunger line
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 3f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(4.683f, 11.916f)
            lineTo(11.895f, 4.704f)
        }
        // Lower needle extension
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 3f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(35.648f, 37.044f)
            lineTo(40.293f, 41.686f)
        }
        // Drop at needle tip
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 3f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(41.7f, 45.75f)
            curveToRelative(-0.75f, 1.05f, -1.05f, 2.1f, -1.05f, 2.7f)
            arcToRelative(1.2f, 1.2f, 0f, false, false, 2.1f, 0f)
            curveToRelative(0f, -0.6f, -0.3f, -1.65f, -1.05f, -2.7f)
            close()
        }
    }.build()
}
