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
 * Icon for Temporary Basal treatment type.
 * Represents temporary basal rate adjustments.
 *
 * Bounding box: x: -0.11-16.11, y: -0.17-12.17 (viewport: 18x14.5, ~90% width)
 */
val TempBasal: ImageVector by lazy {
    ImageVector.Builder(
        name = "TempBasal",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 18f,
        viewportHeight = 14.5f
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
            moveTo(13.20f, 12.17f)
            verticalLineToRelative(-11.83f)
            horizontalLineToRelative(-3.96f)
            verticalLineToRelative(11.83f)
            horizontalLineToRelative(-10.02f)
            verticalLineToRelative(-1.08f)
            horizontalLineToRelative(8.94f)
            verticalLineToRelative(-11.83f)
            horizontalLineToRelative(6.12f)
            verticalLineToRelative(11.83f)
            horizontalLineToRelative(1.76f)
            verticalLineToRelative(1.08f)
            close()
        }
    }.build()
}
