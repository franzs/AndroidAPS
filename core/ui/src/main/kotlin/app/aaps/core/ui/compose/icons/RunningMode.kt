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
 * Icon for Running Mode treatment type.
 * Represents running mode changes (closed loop, open loop, etc.).
 *
 * Bounding box: x: 0.4-19.0, y: 1.48-22.98 (viewport: 24x24)
 */
val RunningMode: ImageVector by lazy {
    ImageVector.Builder(
        name = "RunningMode",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(13.49f, 5.48f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
            reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
            reflectiveCurveTo(12.39f, 5.48f, 13.49f, 5.48f)
            close()
            moveTo(9.89f, 19.38f)
            lineToRelative(1f, -4.4f)
            lineToRelative(2.1f, 2f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-7.5f)
            lineToRelative(-2.1f, -2f)
            lineToRelative(0.6f, -3f)
            curveToRelative(1.3f, 1.5f, 3.3f, 2.5f, 5.5f, 2.5f)
            verticalLineToRelative(-2f)
            curveToRelative(-1.9f, 0f, -3.5f, -1f, -4.3f, -2.4f)
            lineToRelative(-1f, -1.6f)
            curveToRelative(-0.4f, -0.6f, -1f, -1f, -1.7f, -1f)
            curveToRelative(-0.3f, 0f, -0.5f, 0.1f, -0.8f, 0.1f)
            lineToRelative(-5.2f, 2.2f)
            verticalLineToRelative(4.7f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-3.4f)
            lineToRelative(1.8f, -0.7f)
            lineToRelative(-1.6f, 8.1f)
            lineToRelative(-4.9f, -1f)
            lineToRelative(-0.4f, 2f)
            lineToRelative(7f, 1.4f)
            close()
        }
    }.build()
}
