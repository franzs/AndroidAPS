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
 * Icon for Bolus and Carbs treatment type.
 * Represents insulin bolus and carbohydrate entries.
 *
 * Bounding box: x: 0.4-11.6, y: 0.425-11.579 (viewport: 12x12)
 */
val Carbs: ImageVector by lazy {
    ImageVector.Builder(
        name = "BolusCarbs",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 12f,
        viewportHeight = 12f
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
            moveTo(10.074f, 7.883f)
            curveToRelative(-0.332f, 0.416f, -0.691f, 0.722f, -1.09f, 0.993f)
            curveToRelative(-0.378f, 0.257f, -0.843f, 0.39f, -1.203f, 0.468f)
            curveToRelative(-0.688f, 0.125f, -0.25f, 0.188f, 0.096f, 0.657f)
            curveToRelative(-1.247f, 1.781f, -3.348f, 1.9f, -5.102f, 0.991f)
            curveToRelative(-0.141f, -0.073f, -0.272f, -0.165f, -0.403f, -0.255f)
            curveToRelative(-0.276f, -0.19f, -0.398f, -0.236f, -0.63f, 0.004f)
            curveToRelative(-0.187f, 0.193f, -0.628f, 0.618f, -0.852f, 0.838f)
            lineTo(0.4f, 11.047f)
            curveToRelative(0.241f, -0.219f, 0.518f, -0.491f, 0.718f, -0.678f)
            curveToRelative(0.435f, -0.408f, 0.336f, -0.458f, -0.011f, -0.955f)
            curveToRelative(-1.205f, -1.725f, -0.836f, -3.976f, 0.859f, -5.236f)
            curveToRelative(0.722f, 0.462f, 0.566f, 0.353f, 0.801f, -0.327f)
            curveToRelative(0.251f, -0.726f, 0.677f, -1.345f, 1.325f, -1.777f)
            curveToRelative(0.659f, 0.582f, 0.549f, 0.316f, 0.814f, -0.351f)
            curveToRelative(0.286f, -0.721f, 0.734f, -1.309f, 1.353f, -1.806f)
            curveToRelative(0.695f, 0.458f, 1.014f, 0.966f, 1.252f, 1.538f)
            curveToRelative(0.273f, 0.656f, 0.379f, 0.435f, 0.858f, -0.03f)
            curveToRelative(1.023f, -0.992f, 2.273f, -1.385f, 3.491f, -1.087f)
            curveToRelative(0.287f, 1.586f, -0.39f, 3.005f, -1.859f, 3.932f)
            curveToRelative(0.771f, 0.208f, 1.5f, 0.625f, 2.215f, 1.468f)
            curveToRelative(-0.439f, 0.643f, -1.213f, 1.259f, -1.993f, 1.39f)
            curveTo(9.105f, 7.317f, 9.734f, 7.359f, 10.074f, 7.883f)
            close()
            moveTo(11.184f, 1.469f)
            curveToRelative(0.013f, -0.442f, -0.128f, -0.559f, -0.51f, -0.508f)
            curveToRelative(-1.244f, 0.167f, -2.413f, 1.284f, -2.621f, 2.504f)
            curveToRelative(-0.072f, 0.422f, 0.15f, 0.648f, 0.571f, 0.551f)
            curveToRelative(0.267f, -0.062f, 0.538f, -0.144f, 0.783f, -0.265f)
            curveTo(10.39f, 3.263f, 11f, 2.484f, 11.184f, 1.469f)
            close()
            moveTo(7.259f, 3.211f)
            curveToRelative(0.011f, -0.699f, -0.231f, -1.258f, -0.566f, -1.784f)
            curveToRelative(-0.322f, -0.506f, -0.546f, -0.505f, -0.868f, -0.011f)
            curveToRelative(-0.744f, 1.138f, -0.442f, 2.461f, 0.152f, 3.433f)
            curveToRelative(0.19f, 0.311f, 0.445f, 0.313f, 0.669f, 0.011f)
            curveTo(7.017f, 4.357f, 7.273f, 3.801f, 7.259f, 3.211f)
            close()
            moveTo(4.925f, 10.86f)
            curveToRelative(0.517f, 0f, 1.245f, -0.233f, 1.618f, -0.515f)
            curveToRelative(0.317f, -0.24f, 0.344f, -0.436f, 0.03f, -0.652f)
            curveToRelative(-1.164f, -0.802f, -2.354f, -0.977f, -3.585f, -0.119f)
            curveToRelative(-0.414f, 0.289f, -0.391f, 0.523f, 0.063f, 0.79f)
            curveTo(3.629f, 10.703f, 4.26f, 10.846f, 4.925f, 10.86f)
            close()
            moveTo(5.109f, 5.297f)
            curveToRelative(0.02f, -0.666f, -0.231f, -1.243f, -0.569f, -1.786f)
            curveToRelative(-0.316f, -0.507f, -0.532f, -0.495f, -0.854f, 0.026f)
            curveToRelative(-0.622f, 1.004f, -0.57f, 2.408f, 0.126f, 3.37f)
            curveToRelative(0.241f, 0.334f, 0.449f, 0.355f, 0.707f, 0.03f)
            curveTo(4.896f, 6.459f, 5.123f, 5.916f, 5.109f, 5.297f)
            close()
            moveTo(9.068f, 6.639f)
            curveToRelative(0.661f, -0.003f, 1.381f, -0.217f, 1.756f, -0.502f)
            curveToRelative(0.32f, -0.244f, 0.329f, -0.403f, 0.036f, -0.66f)
            curveToRelative(-0.936f, -0.82f, -2.634f, -0.874f, -3.605f, -0.113f)
            curveToRelative(-0.351f, 0.275f, -0.358f, 0.527f, 0.019f, 0.737f)
            curveTo(7.85f, 6.423f, 8.46f, 6.658f, 9.068f, 6.639f)
            close()
            moveTo(1.124f, 7.258f)
            curveToRelative(-0.023f, 0.548f, 0.124f, 1.116f, 0.447f, 1.633f)
            curveToRelative(0.305f, 0.489f, 0.569f, 0.494f, 0.893f, 0.036f)
            curveToRelative(0.704f, -0.995f, 0.645f, -2.507f, -0.135f, -3.438f)
            curveToRelative(-0.254f, -0.303f, -0.471f, -0.302f, -0.713f, 0.03f)
            curveTo(1.253f, 6.017f, 1.116f, 6.587f, 1.124f, 7.258f)
            close()
            moveTo(6.831f, 6.928f)
            curveToRelative(-0.667f, -0.031f, -1.25f, 0.192f, -1.77f, 0.589f)
            curveToRelative(-0.26f, 0.198f, -0.271f, 0.416f, -0.005f, 0.636f)
            curveToRelative(0.852f, 0.704f, 2.692f, 0.765f, 3.599f, 0.114f)
            curveToRelative(0.367f, -0.263f, 0.376f, -0.482f, 0.005f, -0.726f)
            curveTo(8.106f, 7.175f, 7.512f, 6.911f, 6.831f, 6.928f)
            close()
        }
    }.build()
}
