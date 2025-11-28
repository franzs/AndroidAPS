package app.aaps.pump.eopatch.ui.dialogs

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import app.aaps.core.ui.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object AlertDialogHelper {

    fun Builder(context: Context, @StyleRes themeResId: Int = R.style.AppTheme) =
        MaterialAlertDialogBuilder(ContextThemeWrapper(context, themeResId))

}