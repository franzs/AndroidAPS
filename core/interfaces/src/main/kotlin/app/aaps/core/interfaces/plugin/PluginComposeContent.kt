package app.aaps.core.interfaces.plugin

/**
 * Interface for plugins that provide Jetpack Compose content.
 *
 * Plugins migrated to Compose should implement this interface and provide
 * it via [PluginDescription.composeContent]. The [content] function will be
 * called by SingleFragmentActivity when the plugin UI is displayed.
 *
 * The content function receives callbacks for toolbar configuration and navigation,
 * allowing the Compose content to integrate with the activity's toolbar.
 *
 * Example usage in a plugin:
 * ```kotlin
 * class MyPluginComposeContent @Inject constructor(
 *     private val viewModel: MyViewModel,
 *     private val uiInteraction: UiInteraction
 * ) : PluginComposeContent {
 *     override val content: @Composable (setToolbar: (ToolbarConfig) -> Unit, onBack: () -> Unit) -> Unit = { setToolbar, onBack ->
 *         MyScreen(viewModel, uiInteraction, setToolbar, onBack)
 *     }
 * }
 * ```
 */
interface PluginComposeContent {

    /**
     * The Composable content function.
     *
     * This is typed as Any to avoid Compose dependency in core:interfaces.
     * The actual type is: `@Composable (setToolbarConfig: (ToolbarConfig) -> Unit, onNavigateBack: () -> Unit) -> Unit`
     *
     * SingleFragmentActivity will cast this to the proper Compose function type.
     */
    val content: Any
}
