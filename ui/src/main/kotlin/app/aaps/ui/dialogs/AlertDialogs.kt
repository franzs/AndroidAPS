package app.aaps.ui.dialogs

import android.app.Dialog
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.R
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A utility object for showing various standard alert dialogs using Jetpack Compose.
 * This object provides a simple API for showing informational, confirmation, and
 * choice-based dialogs.
 */
class AlertDia(
    private val preferences: Preferences,
    private val rxBus: RxBus
) {

    /**
     * A custom owner class that provides the necessary platform owners for a ComposeView
     * hosted in a custom Dialog. This is required to prevent crashes when using
     * state-saving mechanisms like `rememberSavable`.
     */
    private class ComposeDialogOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private val _viewModelStore = ViewModelStore()
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            // Restore state and move to RESUMED
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val viewModelStore: ViewModelStore
            get() = _viewModelStore

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        /**
         * Marks the lifecycle as DESTROYED and clears the ViewModelStore.
         */
        fun destroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            _viewModelStore.clear()
        }
    }

    /**
     * Displays a simple alert dialog with a title, a message, and an OK button.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param runOnDismiss If true, the [onFinish] will also be executed when the dialog is dismissed
     *   by tapping outside or pressing the back button. Run in UI thread.
     * @param onFinish The action to perform when the OK button is clicked or the dialog is dismissed. Run in UI thread.
     */
    fun showOkDialog(context: Context, title: String, message: String, onFinish: (() -> Unit)? = null) {
        showOkComposeDialog(context, title, message, onFinish)
    }

    /** @see showOkDialog */
    fun showOkDialog(context: Context, @StringRes title: Int, @StringRes message: Int, onFinish: (() -> Unit)? = null) {
        showOkComposeDialog(context, context.getString(title), context.getString(message), onFinish)
    }

    /**
     * Displays a confirmation dialog with a title, a message, a custom icon, and OK/Cancel buttons.
     *
     * @param context The host activity.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param ok The action to perform when the OK button is clicked. Run in UI thread.
     * @param cancel The action to perform when the Cancel button is clicked or the dialog is dismissed. Run in UI thread.
     * @param icon The drawable resource ID for the custom icon. Defaults to a check icon if null.
     */
    fun showOkCancelDialog(context: Context, @StringRes title: Int, @StringRes message: Int, ok: (() -> Unit)?, cancel: (() -> Unit)?, @DrawableRes icon: Int?) {
        showOkCancelComposeDialog(context = context, title = context.getString(title), message = context.getString(message), ok = ok, cancel = cancel, icon = icon)
    }

    /** @see showOkCancelDialog */
    fun showOkCancelDialog(context: Context, title: String, message: String, ok: (() -> Unit)?, cancel: (() -> Unit)?, @DrawableRes icon: Int?) {
        showOkCancelComposeDialog(context, title, message, ok = ok, cancel = cancel, icon = icon)
    }

    /**
     * Displays an alert dialog with a title, two messages, a custom icon, and OK/Cancel buttons.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The primary message to display in the dialog. HTML formatted text is accepted.
     * @param secondMessage The secondary message to display in the dialog (styled with accent color).
     * @param ok The action to perform when the OK button is clicked. Run in UI thread.
     * @param cancel The action to perform when the Cancel button is clicked or the dialog is dismissed. Run in UI thread.
     * @param icon The drawable resource ID for the custom icon. Defaults to a check icon if null.
     */
    fun showOkCancelDialog(context: Context, title: String, message: String, secondMessage: String, ok: (() -> Unit)?, cancel: (() -> Unit)?, @DrawableRes icon: Int?) {
        showOkCancelComposeDialog(context, title, message, secondMessage, ok, cancel, icon)
    }

    /**
     * Displays a dialog with a title, a message, and Yes/No/Cancel buttons.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param yes The action to perform when the Yes button is clicked. Run in UI thread.
     * @param no The action to perform when the No button is clicked. The dialog is dismissed on cancel. Run in UI thread.
     */
    fun showYesNoCancel(context: Context, @StringRes title: Int, @StringRes message: Int, yes: Runnable?, no: (() -> Unit)? = null) {
        showYesNoCancelComposeDialog(context, context.getString(title), context.getString(message), yes, no)
    }

    /** @see showYesNoCancel */
    fun showYesNoCancel(context: Context, title: String, message: String, yes: (() -> Unit)?, no: (() -> Unit)? = null) {
        showYesNoCancelComposeDialog(context, title, message, yes, no)
    }

    /**
     * Displays a warning dialog with a title, a message, a warning icon, and Dismiss/optional positive button.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param positiveButton The resource ID for the positive button text, or -1 if no positive button.
     * @param ok The action to perform when the positive button is clicked. Run in UI thread.
     * @param cancel The action to perform when the Dismiss button is clicked or the dialog is dismissed. Run in UI thread.
     */
    fun showError(context: Context, title: String, message: String, @StringRes positiveButton: Int?, ok: (() -> Unit)? = null, cancel: (() -> Unit)? = null) {
        showErrorComposeDialog(context, title, message, positiveButton, ok, cancel)
    }

    private fun showOkComposeDialog(context: Context, title: String, message: String, onFinish: (() -> Unit)?) {
        val dialog = Dialog(context)
        val owner = ComposeDialogOwner()
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(owner))
            setContent {
                CompositionLocalProvider(
                    LocalPreferences provides preferences,
                    LocalRxBus provides rxBus
                ) {
                    AapsTheme {
                        OKAlertDialog(
                            title = title.ifEmpty { context.getString(R.string.message) },
                            message = message,
                            onDismiss = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    onFinish?.invoke()
                                }
                            },
                            onConfirm = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    onFinish?.invoke()
                                }
                            }
                        )
                    }
                }
            }
        }
        dialog.setContentView(composeView)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener { owner.destroy() }
        dialog.show()
    }

    @Composable
    private fun OKAlertDialog(
        title: String,
        message: String,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text(text = AnnotatedString.fromHtml(message.replace("\n", "<br>"))) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.ok))
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    private fun showYesNoCancelComposeDialog(context: Context, title: String, message: String, yes: Runnable?, no: Runnable?) {
        val dialog = Dialog(context)
        val owner = ComposeDialogOwner()
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(owner))
            setContent {
                CompositionLocalProvider(
                    LocalPreferences provides preferences,
                    LocalRxBus provides rxBus
                ) {
                    AapsTheme {
                        YesNoCancelAlertDialog(
                            title = title,
                            message = message,
                            onYes = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    yes?.run()
                                }
                            },
                            onNo = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    no?.run()
                                }
                            },
                            onCancel = {
                                dialog.dismiss()
                            }
                        )
                    }
                }
            }
        }
        dialog.setContentView(composeView)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener { owner.destroy() }
        dialog.show()
    }

    @Composable
    private fun YesNoCancelAlertDialog(
        title: String,
        message: String,
        onYes: () -> Unit,
        onNo: () -> Unit,
        onCancel: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = { onCancel() },
            title = { Text(text = title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Column {
                    Text(text = AnnotatedString.fromHtml(message.replace("\n", "<br>")))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onCancel() }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onNo() }
                        ) {
                            Text(stringResource(R.string.no))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { onYes() }
                        ) {
                            Text(stringResource(R.string.yes))
                        }
                    }
                }
            },
            confirmButton = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    private fun showErrorComposeDialog(context: Context, title: String, message: String, @StringRes positiveButton: Int?, ok: (() -> Unit)?, cancel: (() -> Unit)?) {
        val dialog = Dialog(context)
        val owner = ComposeDialogOwner()
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(owner))
            setContent {
                CompositionLocalProvider(
                    LocalPreferences provides preferences,
                    LocalRxBus provides rxBus
                ) {
                    AapsTheme {
                        ErrorAlertDialog(
                            title = title,
                            message = message,
                            positiveButton = positiveButton?.let { context.getString(positiveButton) },
                            onDismiss = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    cancel?.invoke()
                                }
                            },
                            onPositive = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    ok?.invoke()
                                }
                            }
                        )
                    }
                }
            }
        }
        dialog.setContentView(composeView)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnDismissListener { owner.destroy() }
        dialog.show()
    }

    @Composable
    private fun ErrorAlertDialog(
        title: String,
        message: String,
        positiveButton: String?,
        onDismiss: () -> Unit,
        onPositive: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Text(
                    text = AnnotatedString.fromHtml(message.replace("\n", "<br>")),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                positiveButton?.let {
                    TextButton(onClick = onPositive) {
                        Text(positiveButton)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dismiss))
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }

    private fun showOkCancelComposeDialog(context: Context, title: String, message: String, secondMessage: String? = null, ok: (() -> Unit)?, cancel: (() -> Unit)?, @DrawableRes icon: Int?) {
        val dialog = Dialog(context)
        val owner = ComposeDialogOwner()
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(owner))
            setContent {
                CompositionLocalProvider(
                    LocalPreferences provides preferences,
                    LocalRxBus provides rxBus
                ) {
                    AapsTheme {
                        OkCancelAlertDialog(
                            title = title,
                            message = message,
                            secondMessage = secondMessage,
                            icon = icon,
                            onOk = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    ok?.invoke()
                                }
                            },
                            onCancel = {
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    cancel?.invoke()
                                }
                            }
                        )
                    }
                }
            }
        }
        dialog.setContentView(composeView)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener { owner.destroy() }
        dialog.show()
    }
}

@Composable
private fun OkCancelAlertDialog(
    title: String,
    message: String,
    secondMessage: String? = null,
    @DrawableRes icon: Int?,
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            icon?.let {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = { Text(text = title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = AnnotatedString.fromHtml(message.replace("\n", "<br>")),
                    textAlign = TextAlign.Center
                )
                secondMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = secondMessage,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOk) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
