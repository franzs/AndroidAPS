package app.aaps.core.ui.compose.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icon for calculator/bolus calculator.
 * Represents a calculator device with display and buttons.
 *
 * Bounding box: x: 7.4-41.7, y: 2.7-45.1 (viewport: 48x48, ~90% height)
 */
val Calculator: ImageVector by lazy {
    ImageVector.Builder(
        name = "Calculator",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Minus sign (top right area)
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(36.36f, 31.36f)
            horizontalLineTo(27.39f)
            curveToRelative(-0.138f, 0f, -0.248f, 0.112f, -0.248f, 0.248f)
            verticalLineToRelative(2.84f)
            curveToRelative(0f, 0.138f, 0.112f, 0.248f, 0.248f, 0.248f)
            horizontalLineTo(36.36f)
            curveToRelative(0.138f, 0f, 0.248f, -0.112f, 0.248f, -0.248f)
            verticalLineToRelative(-2.84f)
            curveTo(36.61f, 31.47f, 36.50f, 31.36f, 36.36f, 31.36f)
            close()
        }
        // Plus sign vertical top part
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(33.60f, 28.52f)
            curveToRelative(0f, -0.138f, -0.112f, -0.248f, -0.248f, -0.248f)
            horizontalLineToRelative(-2.84f)
            curveToRelative(-0.138f, 0f, -0.248f, 0.112f, -0.248f, 0.248f)
            verticalLineToRelative(2.23f)
            horizontalLineToRelative(3.34f)
            verticalLineTo(28.52f)
            close()
        }
        // Plus sign vertical bottom part
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(30.26f, 37.95f)
            curveToRelative(0f, 0.138f, 0.112f, 0.248f, 0.248f, 0.248f)
            horizontalLineToRelative(2.84f)
            curveToRelative(0.138f, 0f, 0.248f, -0.112f, 0.248f, -0.248f)
            verticalLineToRelative(-2.24f)
            horizontalLineToRelative(-3.34f)
            verticalLineTo(37.95f)
            close()
        }
        // Main calculator body with display
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(41.57f, 2.68f)
            horizontalLineToRelative(-33.19f)
            curveToRelative(-0.756f, 0f, -1.372f, 0.613f, -1.372f, 1.372f)
            verticalLineToRelative(40.38f)
            curveToRelative(0f, 0.759f, 0.613f, 1.372f, 1.372f, 1.372f)
            horizontalLineTo(41.57f)
            curveToRelative(0.759f, 0f, 1.372f, -0.613f, 1.372f, -1.372f)
            verticalLineTo(4.05f)
            curveTo(42.94f, 3.29f, 42.33f, 2.68f, 41.57f, 2.68f)
            close()
            moveTo(40.20f, 42.05f)
            horizontalLineTo(9.76f)
            verticalLineTo(13.12f)
            horizontalLineToRelative(30.44f)
            verticalLineTo(42.05f)
            close()
            moveTo(40.20f, 10.38f)
            horizontalLineTo(9.76f)
            verticalLineTo(4.68f)
            horizontalLineToRelative(30.44f)
            verticalLineTo(10.38f)
            close()
        }
        // Minus sign (middle area)
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(27.39f, 22.73f)
            horizontalLineToRelative(8.97f)
            curveToRelative(0.138f, 0f, 0.248f, -0.112f, 0.248f, -0.248f)
            verticalLineToRelative(-2.84f)
            curveToRelative(0f, -0.138f, -0.112f, -0.248f, -0.248f, -0.248f)
            horizontalLineToRelative(-8.97f)
            curveToRelative(-0.138f, 0f, -0.248f, 0.112f, -0.248f, 0.248f)
            verticalLineToRelative(2.84f)
            curveTo(27.14f, 22.62f, 27.25f, 22.73f, 27.39f, 22.73f)
            close()
        }
        // Plus sign (left side)
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(13.27f, 22.73f)
            horizontalLineToRelative(2.82f)
            verticalLineToRelative(2.82f)
            curveToRelative(0f, 0.138f, 0.112f, 0.248f, 0.248f, 0.248f)
            horizontalLineToRelative(2.84f)
            curveToRelative(0.138f, 0f, 0.248f, -0.112f, 0.248f, -0.248f)
            verticalLineToRelative(-2.82f)
            horizontalLineToRelative(2.82f)
            curveToRelative(0.138f, 0f, 0.248f, -0.112f, 0.248f, -0.248f)
            verticalLineToRelative(-2.84f)
            curveToRelative(0f, -0.138f, -0.112f, -0.248f, -0.248f, -0.248f)
            horizontalLineToRelative(-2.82f)
            verticalLineToRelative(-2.82f)
            curveToRelative(0f, -0.138f, -0.112f, -0.248f, -0.248f, -0.248f)
            horizontalLineToRelative(-2.84f)
            curveToRelative(-0.138f, 0f, -0.248f, 0.112f, -0.248f, 0.248f)
            verticalLineToRelative(2.82f)
            horizontalLineToRelative(-2.82f)
            curveToRelative(-0.138f, 0f, -0.248f, 0.112f, -0.248f, 0.248f)
            verticalLineToRelative(2.84f)
            curveTo(13.02f, 22.62f, 13.13f, 22.73f, 13.27f, 22.73f)
            close()
        }
        // Multiply/X sign (bottom left)
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(20.12f, 33.22f)
            lineToRelative(1.99f, -1.99f)
            curveToRelative(0.046f, -0.046f, 0.073f, -0.110f, 0.073f, -0.176f)
            reflectiveCurveToRelative(-0.026f, -0.130f, -0.073f, -0.176f)
            lineToRelative(-2.01f, -2.01f)
            curveToRelative(-0.097f, -0.099f, -0.255f, -0.099f, -0.352f, 0f)
            lineToRelative(-1.99f, 1.99f)
            lineToRelative(-1.99f, -1.99f)
            curveToRelative(-0.097f, -0.099f, -0.255f, -0.099f, -0.352f, 0f)
            lineToRelative(-2.01f, 2.01f)
            curveToRelative(-0.097f, 0.097f, -0.097f, 0.255f, 0f, 0.352f)
            lineToRelative(1.99f, 1.99f)
            lineToRelative(-1.99f, 1.99f)
            curveToRelative(-0.097f, 0.097f, -0.097f, 0.255f, 0f, 0.352f)
            lineToRelative(2.01f, 2.01f)
            curveToRelative(0.046f, 0.046f, 0.110f, 0.073f, 0.176f, 0.073f)
            reflectiveCurveToRelative(0.130f, -0.026f, 0.176f, -0.073f)
            lineToRelative(1.99f, -1.99f)
            lineToRelative(1.99f, 1.99f)
            curveToRelative(0.046f, 0.046f, 0.110f, 0.073f, 0.176f, 0.073f)
            reflectiveCurveToRelative(0.130f, -0.026f, 0.176f, -0.073f)
            lineToRelative(2.01f, -2.01f)
            curveToRelative(0.097f, -0.097f, 0.097f, -0.255f, 0f, -0.352f)
            lineTo(20.12f, 33.22f)
            close()
        }
    }.build()
}
