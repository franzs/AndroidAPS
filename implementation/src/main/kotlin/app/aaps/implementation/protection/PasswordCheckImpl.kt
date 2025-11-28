package app.aaps.implementation.protection

import android.app.Dialog
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import app.aaps.core.interfaces.protection.ExportPasswordDataStore
import app.aaps.core.interfaces.protection.PasswordCheck
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.keys.interfaces.StringPreferenceKey
import app.aaps.core.objects.R
import app.aaps.core.objects.crypto.CryptoUtil
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.core.ui.toast.ToastUtils
import dagger.Reusable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Reusable
class PasswordCheckImpl @Inject constructor(
    private val preferences: Preferences,
    private val cryptoUtil: CryptoUtil,
    private val rxBus: RxBus
) : PasswordCheck {

    @Inject lateinit var exportPasswordDataStore: ExportPasswordDataStore

    /**
     * A custom owner class that provides the necessary platform owners for a ComposeView
     * hosted in a custom Dialog.
     */
    private class ComposeDialogOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private val _viewModelStore = ViewModelStore()
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val viewModelStore: ViewModelStore
            get() = _viewModelStore

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun destroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            _viewModelStore.clear()
        }
    }

    /**
    Asks for "managed" kind of password, checking if it is valid.
     */
    override fun queryPassword(
        context: Context,
        @StringRes labelId: Int,
        preference: StringPreferenceKey,
        ok: ((String) -> Unit)?,
        cancel: (() -> Unit)?,
        fail: (() -> Unit)?,
        pinInput: Boolean
    ) {
        val password = preferences.get(preference)
        if (password == "") {
            ok?.invoke("")
            return
        }

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
                        QueryPasswordDialog(
                            title = context.getString(labelId),
                            pinInput = pinInput,
                            onConfirm = { enteredPassword ->
                                if (cryptoUtil.checkPassword(enteredPassword, password)) {
                                    dialog.dismiss()
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(100)
                                        ok?.invoke(enteredPassword)
                                    }
                                } else {
                                    val msg = if (pinInput) app.aaps.core.ui.R.string.wrongpin else app.aaps.core.ui.R.string.wrongpassword
                                    ToastUtils.errorToast(context, context.getString(msg))
                                    fail?.invoke()
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

    override fun setPassword(
        context: Context,
        @StringRes labelId: Int,
        preference: StringPreferenceKey,
        ok: ((String) -> Unit)?,
        cancel: (() -> Unit)?,
        clear: (() -> Unit)?,
        pinInput: Boolean
    ) {
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
                        SetPasswordDialog(
                            title = context.getString(labelId),
                            pinInput = pinInput,
                            onConfirm = { enteredPassword, enteredPassword2 ->
                                if (enteredPassword != enteredPassword2) {
                                    val msg = if (pinInput) app.aaps.core.ui.R.string.pin_dont_match else app.aaps.core.ui.R.string.passwords_dont_match
                                    ToastUtils.errorToast(context, context.getString(msg))
                                } else if (enteredPassword.isNotEmpty()) {
                                    preferences.put(preference, cryptoUtil.hashPassword(enteredPassword))
                                    exportPasswordDataStore.clearPasswordDataStore(context)
                                    val msg = if (pinInput) app.aaps.core.ui.R.string.pin_set else app.aaps.core.ui.R.string.password_set
                                    ToastUtils.okToast(context, context.getString(msg))
                                    dialog.dismiss()
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(100)
                                        ok?.invoke(enteredPassword)
                                    }
                                } else {
                                    if (preferences.getIfExists(preference) != null) {
                                        preferences.remove(preference)
                                        val msg = if (pinInput) app.aaps.core.ui.R.string.pin_cleared else app.aaps.core.ui.R.string.password_cleared
                                        ToastUtils.graphicalToast(context, context.getString(msg), app.aaps.core.ui.R.drawable.ic_toast_delete_confirm)
                                        dialog.dismiss()
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(100)
                                            clear?.invoke()
                                        }
                                    } else {
                                        val msg = if (pinInput) app.aaps.core.ui.R.string.pin_not_changed else app.aaps.core.ui.R.string.password_not_changed
                                        ToastUtils.warnToast(context, context.getString(msg))
                                        dialog.dismiss()
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(100)
                                            cancel?.invoke()
                                        }
                                    }
                                }
                            },
                            onCancel = {
                                val msg = if (pinInput) app.aaps.core.ui.R.string.pin_not_changed else app.aaps.core.ui.R.string.password_not_changed
                                ToastUtils.infoToast(context, msg)
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

    /**
    Prompt free-form password, with additional help and warning messages.
    Preference ID (preference) is used only to generate ID for password managers,
    since this query does NOT check validity of password.
     */
    override fun queryAnyPassword(
        context: Context,
        @StringRes labelId: Int,
        preference: StringPreferenceKey,
        @StringRes passwordExplanation: Int?,
        @StringRes passwordWarning: Int?,
        ok: ((String) -> Unit)?,
        cancel: (() -> Unit)?
    ) {
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
                        QueryAnyPasswordDialog(
                            title = context.getString(labelId),
                            passwordExplanation = passwordExplanation?.let { context.getString(it) },
                            passwordWarning = passwordWarning?.let { context.getString(it) },
                            onConfirm = { enteredPassword ->
                                dialog.dismiss()
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    ok?.invoke(enteredPassword)
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

    @Composable
    private fun QueryPasswordDialog(
        title: String,
        pinInput: Boolean,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val passwordText = remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = onCancel,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_header_key),
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                OutlinedTextField(
                    value = passwordText.value,
                    onValueChange = { passwordText.value = it },
                    label = {
                        Text(
                            stringResource(
                                if (pinInput) app.aaps.core.ui.R.string.pin_hint
                                else app.aaps.core.ui.R.string.password_hint
                            )
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (pinInput) KeyboardType.NumberPassword else KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            onConfirm(passwordText.value)
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onConfirm(passwordText.value)
                    }
                ) {
                    Text(stringResource(app.aaps.core.ui.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(stringResource(app.aaps.core.ui.R.string.cancel))
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    @Composable
    private fun SetPasswordDialog(
        title: String,
        pinInput: Boolean,
        onConfirm: (String, String) -> Unit,
        onCancel: () -> Unit
    ) {
        val password1 = remember { mutableStateOf("") }
        val password2 = remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = onCancel,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_header_key),
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = password1.value,
                        onValueChange = { password1.value = it },
                        label = {
                            Text(
                                stringResource(
                                    if (pinInput) app.aaps.core.ui.R.string.pin_hint
                                    else app.aaps.core.ui.R.string.password_hint
                                )
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (pinInput) KeyboardType.NumberPassword else KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    OutlinedTextField(
                        value = password2.value,
                        onValueChange = { password2.value = it },
                        label = {
                            Text(
                                stringResource(
                                    if (pinInput) app.aaps.core.ui.R.string.pin_hint
                                    else app.aaps.core.ui.R.string.password_hint
                                )
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (pinInput) KeyboardType.NumberPassword else KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onConfirm(password1.value, password2.value)
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(password1.value, password2.value) }) {
                    Text(stringResource(app.aaps.core.ui.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(stringResource(app.aaps.core.ui.R.string.cancel))
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    @Composable
    private fun QueryAnyPasswordDialog(
        title: String,
        passwordExplanation: String?,
        passwordWarning: String?,
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val passwordText = remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = onCancel,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_header_key),
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    passwordExplanation?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    passwordWarning?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (passwordExplanation != null || passwordWarning != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = passwordText.value,
                        onValueChange = { passwordText.value = it },
                        label = { Text(stringResource(app.aaps.core.ui.R.string.password_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onConfirm(passwordText.value)
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onConfirm(passwordText.value)
                    }
                ) {
                    Text(stringResource(app.aaps.core.ui.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(stringResource(app.aaps.core.ui.R.string.cancel))
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }
}
