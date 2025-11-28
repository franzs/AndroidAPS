package app.aaps.ui.alertDialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
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
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.ui.IconsProvider
import app.aaps.core.interfaces.utils.fabric.FabricPrivacy
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AboutDialog @Inject constructor(
    private val preferences: Preferences,
    private val rxBus: RxBus,
    private val config: Config,
    private val rh: ResourceHelper,
    private val fabricPrivacy: FabricPrivacy,
    private val activePlugin: ActivePlugin,
    private val iconsProvider: IconsProvider
) {

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

    fun showAboutDialog(context: Context, @StringRes appName: Int) {
        var message = "Build: ${config.BUILD_VERSION}\n"
        message += "Flavor: ${config.FLAVOR}${config.BUILD_TYPE}\n"
        message += "${rh.gs(app.aaps.core.ui.R.string.configbuilder_nightscoutversion_label)} ${activePlugin.activeNsClient?.detectedNsVersion() ?: rh.gs(app.aaps.core.ui.R.string.not_available_full)}"
        if (config.isEngineeringMode()) message += "\n${rh.gs(app.aaps.core.ui.R.string.engineering_mode_enabled)}"
        if (config.isUnfinishedMode()) message += "\nUnfinished mode enabled"
        if (!fabricPrivacy.fabricEnabled()) message += "\n${rh.gs(app.aaps.core.ui.R.string.fabric_upload_disabled)}"
        message += rh.gs(app.aaps.core.ui.R.string.about_link_urls)

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
                        AboutAlertDialog(
                            title = rh.gs(appName) + " " + config.VERSION,
                            message = message,
                            icon = iconsProvider.getIcon(),
                            onDismiss = { dialog.dismiss() },
                            onDontKillMyApp = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        ("https://dontkillmyapp.com/" + Build.MANUFACTURER.lowercase().replace(" ", "-")).toUri()
                                    )
                                )
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
    private fun AboutAlertDialog(
        title: String,
        message: String,
        icon: Int,
        onDismiss: () -> Unit,
        onDontKillMyApp: () -> Unit
    ) {
        val annotatedMessage = buildClickableMessage(message)

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
            },
            title = { Text(text = title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = annotatedMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(app.aaps.core.ui.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDontKillMyApp) {
                    Text(stringResource(app.aaps.core.ui.R.string.cta_dont_kill_my_app_info))
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }

    @Composable
    private fun buildClickableMessage(message: String): AnnotatedString {
        return buildAnnotatedString {
            // Simple URL detection pattern
            val urlPattern = Regex("https?://[^\\s]+")
            var lastIndex = 0

            urlPattern.findAll(message).forEach { matchResult ->
                // Add text before URL
                append(message.substring(lastIndex, matchResult.range.first))

                // Add URL with LinkAnnotation
                val url = matchResult.value
                withLink(LinkAnnotation.Url(url)) {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(url)
                    }
                }

                lastIndex = matchResult.range.last + 1
            }

            // Add remaining text
            if (lastIndex < message.length) {
                append(message.substring(lastIndex))
            }
        }
    }
}