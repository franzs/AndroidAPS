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
 * Icon for Extended Bolus treatment type.
 * Represents extended or dual-wave bolus deliveries.
 *
 * Bounding box: x: 1.227-14.768, y: 0.288-15.119 (viewport: 15x16)
 */
val ExtendedBolus: ImageVector by lazy {
    ImageVector.Builder(
        name = "ExtendedBolus",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 15f,
        viewportHeight = 16f
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
            moveTo(11.714f, 3.697f)
            lineToRelative(0.353f, -0.684f)
            curveToRelative(0.18f, -0.348f, 0.043f, -0.775f, -0.305f, -0.955f)
            curveToRelative(-0.351f, -0.181f, -0.775f, -0.043f, -0.955f, 0.305f)
            lineToRelative(-0.354f, 0.685f)
            curveToRelative(-0.379f, -0.148f, -0.772f, -0.265f, -1.18f, -0.344f)
            verticalLineTo(2.106f)
            curveToRelative(0.45f, -0.061f, 0.802f, -0.431f, 0.802f, -0.897f)
            curveToRelative(0f, -0.509f, -0.412f, -0.921f, -0.921f, -0.921f)
            horizontalLineToRelative(-2.313f)
            curveToRelative(-0.509f, 0f, -0.921f, 0.413f, -0.921f, 0.921f)
            curveToRelative(0f, 0.467f, 0.352f, 0.836f, 0.802f, 0.897f)
            verticalLineToRelative(0.598f)
            curveToRelative(-3.125f, 0.599f, -5.495f, 3.349f, -5.495f, 6.646f)
            curveToRelative(0f, 3.732f, 3.037f, 6.77f, 6.77f, 6.77f)
            curveToRelative(3.732f, 0f, 6.77f, -3.037f, 6.77f, -6.77f)
            curveTo(14.768f, 6.989f, 13.551f, 4.909f, 11.714f, 3.697f)
            close()
            moveTo(7.998f, 15.119f)
            curveToRelative(-3.182f, 0f, -5.77f, -2.588f, -5.77f, -5.77f)
            reflectiveCurveToRelative(2.588f, -5.77f, 5.77f, -5.77f)
            reflectiveCurveToRelative(5.77f, 2.588f, 5.77f, 5.77f)
            reflectiveCurveTo(11.18f, 15.119f, 7.998f, 15.119f)
            close()
        }
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
            moveTo(8f, 4.814f)
            curveToRelative(-0.086f, 0f, -0.169f, 0.035f, -0.23f, 0.096f)
            reflectiveCurveToRelative(-0.096f, 0.144f, -0.096f, 0.23f)
            verticalLineToRelative(4.198f)
            curveToRelative(0f, 0.08f, 0.029f, 0.157f, 0.083f, 0.217f)
            lineToRelative(2.789f, 3.14f)
            curveToRelative(0.058f, 0.064f, 0.139f, 0.104f, 0.226f, 0.108f)
            curveToRelative(0.006f, 0.001f, 0.013f, 0.001f, 0.019f, 0.001f)
            curveToRelative(0.08f, 0f, 0.157f, -0.029f, 0.217f, -0.083f)
            curveToRelative(0.971f, -0.866f, 1.527f, -2.095f, 1.527f, -3.372f)
            curveTo(12.533f, 6.85f, 10.5f, 4.816f, 8f, 4.814f)
            close()
        }
    }.build()
}
