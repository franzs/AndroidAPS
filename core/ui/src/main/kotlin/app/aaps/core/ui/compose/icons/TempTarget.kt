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
 * Icon for Temporary Target treatment type.
 * Represents temporary blood glucose targets.
 *
 * Bounding box: x: 0.221-20.277, y: 0.313-11.689 (viewport: 22x13.5)
 */
val TempTarget: ImageVector by lazy {
    ImageVector.Builder(
        name = "TempTarget",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 22f,
        viewportHeight = 13.5f
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
            moveTo(5.242f, 11.689f)
            curveToRelative(-0.005f, 0f, -0.011f, 0f, -0.017f, 0f)
            curveToRelative(-0.695f, -0.013f, -1.318f, -0.829f, -1.709f, -2.24f)
            curveToRelative(-0.285f, -1.029f, -0.542f, -2.103f, -0.792f, -3.141f)
            curveToRelative(-0.156f, -0.65f, -0.313f, -1.301f, -0.476f, -1.945f)
            lineTo(2.14f, 3.929f)
            curveToRelative(-0.369f, -1.486f, -0.75f, -3.024f, -1.746f, -3.197f)
            curveToRelative(-0.116f, -0.02f, -0.193f, -0.130f, -0.173f, -0.246f)
            reflectiveCurveToRelative(0.129f, -0.189f, 0.246f, -0.173f)
            curveTo(1.735f, 0.534f, 2.15f, 2.208f, 2.552f, 3.827f)
            lineToRelative(0.108f, 0.432f)
            curveToRelative(0.164f, 0.646f, 0.321f, 1.298f, 0.478f, 1.951f)
            curveToRelative(0.249f, 1.035f, 0.506f, 2.104f, 0.789f, 3.127f)
            curveToRelative(0.322f, 1.163f, 0.834f, 1.92f, 1.307f, 1.929f)
            curveToRelative(0.002f, 0f, 0.005f, 0f, 0.008f, 0f)
            curveToRelative(0.443f, 0f, 0.905f, -0.656f, 1.269f, -1.804f)
            curveToRelative(0.188f, -0.592f, 0.349f, -1.229f, 0.504f, -1.847f)
            lineToRelative(0.124f, -0.487f)
            curveToRelative(0.138f, -0.536f, 0.268f, -1.08f, 0.398f, -1.624f)
            curveToRelative(0.267f, -1.114f, 0.542f, -2.265f, 0.878f, -3.332f)
            curveToRelative(0.373f, -1.182f, 0.928f, -1.836f, 1.565f, -1.842f)
            curveToRelative(0.003f, 0f, 0.005f, 0f, 0.008f, 0f)
            curveToRelative(0.639f, 0f, 1.208f, 0.65f, 1.603f, 1.832f)
            curveToRelative(0.278f, 0.833f, 0.514f, 1.729f, 0.74f, 2.598f)
            lineToRelative(0.177f, 0.671f)
            curveToRelative(0.147f, 0.553f, 0.288f, 1.112f, 0.43f, 1.672f)
            curveToRelative(0.24f, 0.956f, 0.489f, 1.945f, 0.769f, 2.879f)
            curveToRelative(0.229f, 0.77f, 0.619f, 1.26f, 1.017f, 1.28f)
            curveToRelative(0.356f, 0.043f, 0.729f, -0.362f, 1.011f, -1.041f)
            curveToRelative(0.271f, -0.651f, 0.521f, -1.41f, 0.761f, -2.318f)
            curveToRelative(0.333f, -1.265f, 0.648f, -2.547f, 0.964f, -3.829f)
            curveToRelative(0.146f, -0.592f, 0.291f, -1.184f, 0.438f, -1.773f)
            curveToRelative(0.281f, -1.125f, 0.812f, -1.774f, 1.62f, -1.983f)
            curveToRelative(0.111f, -0.028f, 0.229f, 0.04f, 0.259f, 0.153f)
            curveToRelative(0.029f, 0.114f, -0.039f, 0.229f, -0.153f, 0.259f)
            curveToRelative(-0.652f, 0.168f, -1.069f, 0.7f, -1.313f, 1.674f)
            curveToRelative(-0.147f, 0.589f, -0.293f, 1.181f, -0.438f, 1.772f)
            curveToRelative(-0.316f, 1.284f, -0.633f, 2.568f, -0.966f, 3.836f)
            curveToRelative(-0.245f, 0.928f, -0.5f, 1.704f, -0.779f, 2.374f)
            curveToRelative(-0.487f, 1.17f, -1.097f, 1.32f, -1.426f, 1.302f)
            curveToRelative(-0.59f, -0.03f, -1.114f, -0.622f, -1.402f, -1.584f)
            curveToRelative(-0.281f, -0.941f, -0.531f, -1.936f, -0.773f, -2.896f)
            curveToRelative(-0.14f, -0.558f, -0.28f, -1.115f, -0.427f, -1.666f)
            lineToRelative(-0.178f, -0.673f)
            curveToRelative(-0.225f, -0.861f, -0.458f, -1.751f, -0.731f, -2.57f)
            curveToRelative(-0.323f, -0.966f, -0.772f, -1.541f, -1.201f, -1.541f)
            curveToRelative(-0.001f, 0f, -0.002f, 0f, -0.004f, 0f)
            curveToRelative(-0.425f, 0.003f, -0.86f, 0.581f, -1.164f, 1.544f)
            curveToRelative(-0.331f, 1.052f, -0.605f, 2.196f, -0.87f, 3.303f)
            curveToRelative(-0.131f, 0.546f, -0.262f, 1.092f, -0.399f, 1.630f)
            lineToRelative(-0.124f, 0.486f)
            curveToRelative(-0.158f, 0.623f, -0.32f, 1.268f, -0.512f, 1.871f)
            curveTo(6.367f, 11.32f, 5.706f, 11.689f, 5.242f, 11.689f)
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
            moveTo(19.569f, 6.709f)
            horizontalLineToRelative(-4.772f)
            curveToRelative(-0.392f, 0f, -0.709f, -0.317f, -0.709f, -0.709f)
            verticalLineTo(3.177f)
            horizontalLineTo(5.927f)
            verticalLineToRelative(2.823f)
            curveToRelative(0f, 0.392f, -0.317f, 0.709f, -0.708f, 0.709f)
            horizontalLineTo(0.431f)
            curveToRelative(-0.392f, 0f, -0.708f, -0.317f, -0.708f, -0.709f)
            reflectiveCurveToRelative(0.317f, -0.708f, 0.708f, -0.708f)
            horizontalLineTo(4.51f)
            verticalLineToRelative(-2.823f)
            curveToRelative(0f, -0.392f, 0.317f, -0.708f, 0.708f, -0.708f)
            horizontalLineToRelative(9.578f)
            curveToRelative(0.392f, 0f, 0.709f, 0.317f, 0.709f, 0.708f)
            verticalLineToRelative(2.823f)
            horizontalLineToRelative(4.063f)
            curveToRelative(0.392f, 0f, 0.709f, 0.317f, 0.709f, 0.708f)
            reflectiveCurveTo(19.961f, 6.709f, 19.569f, 6.709f)
            close()
        }
    }.build()
}
