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
 * Icon for NSClient background service.
 * Represents the Nightscout client connection status.
 *
 * Bounding box: x: 6.0-42.0, y: 1.9-46.0 (viewport: 48x48, ~92% height)
 */
val Ns: ImageVector by lazy {
    ImageVector.Builder(
        name = "Ns",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        // Main pin/location marker shape
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
            moveTo(37.58f, 19.73f)
            curveToRelative(0f, 1.40f, -0.19f, 3.09f, -0.28f, 3.65f)
            curveToRelative(-0.28f, 1.59f, -0.66f, 3.09f, -1.12f, 4.68f)
            curveToRelative(-0.66f, 1.97f, -1.40f, 3.84f, -2.53f, 5.71f)
            curveToRelative(-0.66f, 1.12f, -1.31f, 2.25f, -1.97f, 3.28f)
            curveToRelative(-0.75f, 1.12f, -1.50f, 2.25f, -2.43f, 3.37f)
            curveToRelative(-0.84f, 1.03f, -1.59f, 1.97f, -2.53f, 3.09f)
            curveToRelative(-0.28f, 0.28f, -0.56f, 0.66f, -0.84f, 0.94f)
            curveToRelative(-0.56f, 0.66f, -1.03f, 1.12f, -1.69f, 1.69f)
            curveToRelative(-1.40f, 1.31f, -3.46f, 1.22f, -4.68f, -0.19f)
            curveToRelative(-0.84f, -0.84f, -1.78f, -1.87f, -2.62f, -2.81f)
            curveToRelative(-1.03f, -1.22f, -1.97f, -2.43f, -3.00f, -3.65f)
            curveToRelative(-1.40f, -1.87f, -2.62f, -3.84f, -3.74f, -5.90f)
            curveToRelative(-0.84f, -1.40f, -1.40f, -3.09f, -1.97f, -4.59f)
            curveToRelative(-0.75f, -2.15f, -1.40f, -4.31f, -1.50f, -6.55f)
            curveToRelative(0f, -0.28f, -0.09f, -1.40f, -0.09f, -2.53f)
            curveToRelative(0f, -1.31f, 0.19f, -2.25f, 0.19f, -2.62f)
            curveToRelative(0.28f, -1.40f, 0.75f, -2.81f, 1.40f, -4.21f)
            curveToRelative(0.47f, -1.12f, 1.22f, -2.06f, 1.97f, -3.09f)
            curveToRelative(0.75f, -0.84f, 1.59f, -1.69f, 2.53f, -2.43f)
            curveToRelative(1.03f, -0.84f, 2.06f, -1.40f, 3.28f, -1.97f)
            curveToRelative(1.31f, -0.56f, 2.62f, -0.94f, 4.03f, -1.12f)
            curveToRelative(0.28f, 0f, 1.12f, -0.19f, 2.25f, -0.19f)
            curveToRelative(1.12f, 0f, 2.06f, 0.19f, 2.34f, 0.19f)
            curveToRelative(1.50f, 0.28f, 3.00f, 0.75f, 4.40f, 1.50f)
            curveToRelative(1.22f, 0.56f, 2.25f, 1.31f, 3.28f, 2.15f)
            curveToRelative(0.84f, 0.75f, 1.59f, 1.50f, 2.34f, 2.43f)
            curveToRelative(0.94f, 1.12f, 1.59f, 2.25f, 2.06f, 3.46f)
            curveToRelative(0.56f, 1.40f, 0.94f, 2.81f, 1.12f, 4.31f)
            curveTo(37.49f, 17.58f, 37.58f, 18.42f, 37.58f, 19.73f)
            close()
            moveTo(33.85f, 19.73f)
            curveToRelative(0f, -1.03f, -0.09f, -2.06f, -0.37f, -3.09f)
            curveToRelative(-0.28f, -1.03f, -0.75f, -2.06f, -1.31f, -2.90f)
            curveToRelative(-0.66f, -1.03f, -1.50f, -1.87f, -2.43f, -2.62f)
            curveToRelative(-0.94f, -0.75f, -2.06f, -1.22f, -3.18f, -1.50f)
            curveToRelative(-1.22f, -0.28f, -2.43f, -0.37f, -3.65f, -0.28f)
            curveToRelative(-1.31f, 0.09f, -2.53f, 0.47f, -3.65f, 1.03f)
            curveToRelative(-1.22f, 0.56f, -2.25f, 1.40f, -3.09f, 2.43f)
            curveToRelative(-0.84f, 1.03f, -1.50f, 2.25f, -1.87f, 3.46f)
            curveToRelative(-0.37f, 1.31f, -0.47f, 2.71f, -0.37f, 4.03f)
            curveToRelative(0.09f, 1.22f, 0.47f, 2.43f, 1.03f, 3.56f)
            curveToRelative(0.66f, 1.31f, 1.59f, 2.53f, 2.62f, 3.56f)
            curveToRelative(1.12f, 1.12f, 2.43f, 1.97f, 3.84f, 2.34f)
            curveToRelative(1.22f, 0.37f, 2.53f, 0.47f, 3.74f, 0.28f)
            curveToRelative(1.31f, -0.19f, 2.53f, -0.66f, 3.65f, -1.40f)
            curveToRelative(1.12f, -0.75f, 2.06f, -1.78f, 2.71f, -2.90f)
            curveToRelative(0.66f, -1.22f, 1.03f, -2.53f, 1.22f, -3.84f)
            curveTo(33.76f, 21.98f, 33.85f, 20.86f, 33.85f, 19.73f)
            close()
        }
    }.build()
}
