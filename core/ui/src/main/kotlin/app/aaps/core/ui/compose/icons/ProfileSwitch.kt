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
 * Icon for Profile Switch treatment type.
 * Represents profile changes and adjustments.
 *
 * Bounding box: x: 2.37-21.63, y: 2.68-20.63 (viewport: 24x24, includes stroke, ~90% width)
 */
val ProfileSwitch: ImageVector by lazy {
    ImageVector.Builder(
        name = "ProfileSwitch",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.446f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(21.332f, 9.679f)
            curveToRelative(-0.163f, -0.502f, -0.595f, -0.868f, -1.118f, -0.943f)
            lineToRelative(-4.816f, -0.700f)
            lineTo(13.241f, 3.671f)
            curveToRelative(-0.466f, -0.947f, -2.018f, -0.947f, -2.484f, 0f)
            lineTo(8.601f, 8.036f)
            lineTo(3.785f, 8.736f)
            curveTo(3.264f, 8.811f, 2.83f, 9.177f, 2.667f, 9.679f)
            curveToRelative(-0.163f, 0.501f, -0.028f, 1.052f, 0.351f, 1.420f)
            lineToRelative(3.486f, 3.397f)
            lineTo(5.681f, 19.324f)
            curveToRelative(-0.089f, 0.521f, 0.124f, 1.046f, 0.551f, 1.356f)
            curveToRelative(0.241f, 0.176f, 0.527f, 0.265f, 0.814f, 0.265f)
            curveToRelative(0.221f, 0f, 0.442f, -0.053f, 0.645f, -0.160f)
            lineTo(12f, 18.521f)
            lineToRelative(4.310f, 2.264f)
            curveToRelative(0.470f, 0.245f, 1.030f, 0.208f, 1.460f, -0.105f)
            curveToRelative(0.425f, -0.310f, 0.640f, -0.836f, 0.549f, -1.356f)
            lineToRelative(-0.823f, -4.799f)
            lineToRelative(3.486f, -3.397f)
            curveTo(21.360f, 10.731f, 21.497f, 10.180f, 21.332f, 9.679f)
            close()
            moveTo(20.063f, 10.365f)
            lineToRelative(-3.808f, 3.713f)
            lineToRelative(0.899f, 5.242f)
            curveToRelative(0.017f, 0.105f, -0.025f, 0.210f, -0.111f, 0.271f)
            curveToRelative(-0.048f, 0.036f, -0.105f, 0.053f, -0.163f, 0.053f)
            curveToRelative(-0.043f, 0f, -0.088f, -0.010f, -0.130f, -0.033f)
            lineToRelative(-4.708f, -2.474f)
            lineToRelative(-4.708f, 2.474f)
            curveToRelative(-0.090f, 0.053f, -0.205f, 0.043f, -0.292f, -0.020f)
            curveToRelative(-0.084f, -0.061f, -0.128f, -0.166f, -0.110f, -0.271f)
            lineToRelative(0.899f, -5.242f)
            lineToRelative(-3.810f, -3.713f)
            curveToRelative(-0.076f, -0.074f, -0.102f, -0.184f, -0.070f, -0.284f)
            curveToRelative(0.033f, -0.100f, 0.119f, -0.172f, 0.223f, -0.188f)
            lineToRelative(5.265f, -0.764f)
            lineToRelative(2.354f, -4.770f)
            curveToRelative(0.094f, -0.190f, 0.402f, -0.190f, 0.496f, 0f)
            lineToRelative(2.353f, 4.770f)
            lineToRelative(5.265f, 0.764f)
            curveToRelative(0.105f, 0.016f, 0.190f, 0.088f, 0.223f, 0.188f)
            reflectiveCurveTo(20.139f, 10.291f, 20.063f, 10.365f)
            close()
        }
    }.build()
}
