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
 * Icon for User Entry treatment type.
 * Represents user action log entries (document/note icon).
 *
 * Bounding box: x: 48-912, y: 48-912 (viewport: 960x960, ~90% width)
 */
val UserEntry: ImageVector by lazy {
    ImageVector.Builder(
        name = "UserEntry",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
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
            moveTo(240f, 728f)
            horizontalLineToRelative(336f)
            verticalLineToRelative(-96f)
            lineTo(240f, 632f)
            verticalLineToRelative(96f)
            close()
            moveTo(240f, 536f)
            horizontalLineToRelative(480f)
            verticalLineToRelative(-96f)
            lineTo(240f, 440f)
            verticalLineToRelative(96f)
            close()
            moveTo(240f, 344f)
            horizontalLineToRelative(480f)
            verticalLineToRelative(-96f)
            lineTo(240f, 248f)
            verticalLineToRelative(96f)
            close()
            moveTo(144f, 912f)
            quadToRelative(-39.6f, 0f, -67.8f, -28.2f)
            reflectiveQuadTo(48f, 816f)
            verticalLineToRelative(-672f)
            quadToRelative(0f, -39.6f, 28.2f, -67.8f)
            reflectiveQuadTo(144f, 48f)
            horizontalLineToRelative(672f)
            quadToRelative(39.6f, 0f, 67.8f, 28.2f)
            reflectiveQuadTo(912f, 144f)
            verticalLineToRelative(672f)
            quadToRelative(0f, 39.6f, -28.2f, 67.8f)
            reflectiveQuadTo(816f, 912f)
            lineTo(144f, 912f)
            close()
            moveTo(144f, 816f)
            horizontalLineToRelative(672f)
            verticalLineToRelative(-672f)
            lineTo(144f, 144f)
            verticalLineToRelative(672f)
            close()
            moveTo(144f, 144f)
            verticalLineToRelative(672f)
            verticalLineToRelative(-672f)
            close()
        }
    }.build()
}
