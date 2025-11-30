package app.aaps.core.interfaces.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import app.aaps.core.interfaces.R
import app.aaps.core.interfaces.nsclient.NSAlarm

/**
 * Interface to use activities located in different modules
 * usage: startActivity(Intent(context, activityNames.xxxx))
 */
interface UiInteraction {

    /** The main activity of the application. */
    val mainActivity: Class<*>

    /** The activity for displaying TDD stats. */
    val tddStatsActivity: Class<*>

    /** The activity for browsing history. */
    val historyBrowseActivity: Class<*>

    /** The activity for displaying error information. */
    val errorHelperActivity: Class<*>

    /** The activity for showing bolus progress. */
    val bolusProgressHelperActivity: Class<*>

    /** A generic activity that can host a single fragment. */
    val singleFragmentActivity: Class<*>

    /** The main preferences activity. */
    val preferencesActivity: Class<*>

    /** The fragment for "My Preferences". */
    val myPreferenceFragment: Class<*>

    /** The activity for the Quick Wizard list. */
    val quickWizardListActivity: Class<*>

    companion object {

        /** Key for passing a plugin name in an Intent extra. */
        const val PLUGIN_NAME = "PluginName"

        /**
         * Key for identifying a preference from [Preferences].
         */
        const val PREFERENCE = "Preference"
    }

    /**
     * Enum for specific preference screens.
     */
    enum class Preferences {

        /** The protection preference screen. */
        PROTECTION
    }

    /**
     * Display names for units preferences.
     */
    val unitsEntries: Array<CharSequence>

    /**
     * Value names for units preferences.
     */
    val unitsValues: Array<CharSequence>

    /**
     * Show ErrorHelperActivity and start alarm.
     * @param status message inside dialog
     * @param title title of dialog
     * @param soundId sound resource. if == 0 alarm is not started
     */
    fun runAlarm(status: String, title: String, @RawRes soundId: Int = 0)

    /**
     * Triggers an update of the application widget.
     * @param context The context.
     * @param from A string indicating the source of the update request.
     */
    fun updateWidget(context: Context, from: String)

    /**
     * Shows the bolus wizard dialog.
     * @param fragmentManager The fragment manager to use.
     * @param carbs Optional pre-filled carbs value.
     * @param name Optional pre-filled name/note.
     */
    fun runWizardDialog(fragmentManager: FragmentManager, carbs: Int? = null, name: String? = null)

    /**
     * Shows a dialog related to the loop status.
     * @param fragmentManager The fragment manager to use.
     * @param showOkCancel A flag to determine dialog buttons.
     */
    fun runLoopDialog(fragmentManager: FragmentManager, showOkCancel: Int)

    /**
     * Shows the profile switch dialog.
     * @param fragmentManager The fragment manager to use.
     * @param profileName Optional pre-selected profile name.
     */
    fun runProfileSwitchDialog(fragmentManager: FragmentManager, profileName: String? = null)

    /**
     * Shows the temporary basal dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runTempBasalDialog(fragmentManager: FragmentManager)

    /**
     * Shows the treatment dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runTreatmentDialog(fragmentManager: FragmentManager)

    /**
     * Shows the insulin dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runInsulinDialog(fragmentManager: FragmentManager)

    /**
     * Shows the calibration dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runCalibrationDialog(fragmentManager: FragmentManager)

    /**
     * Shows the carbs dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runCarbsDialog(fragmentManager: FragmentManager)

    /**
     * Shows the temporary target dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runTempTargetDialog(fragmentManager: FragmentManager)

    /**
     * Shows the extended bolus dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runExtendedBolusDialog(fragmentManager: FragmentManager)

    /**
     * Shows the pump fill/prime dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runFillDialog(fragmentManager: FragmentManager)

    /**
     * Defines modes for the site rotation dialog.
     */
    enum class SiteMode(val i: Int) {

        /** View existing site change history. */
        VIEW(1),

        /** Record a new site change. */
        EDIT(2)
    }

    /**
     * Shows the site rotation dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runSiteRotationDialog(fragmentManager: FragmentManager)

    /**
     * Shows the bolus progress dialog.
     * @param fragmentManager The fragment manager to use.
     */
    fun runBolusProgressDialog(fragmentManager: FragmentManager)

    /**
     * Defines modes for the profile viewer dialog.
     */
    enum class Mode(val i: Int) {

        /** View the currently running profile. */
        RUNNING_PROFILE(1),

        /** View a custom profile from a string. */
        CUSTOM_PROFILE(2),

        /** View a profile from the database. */
        DB_PROFILE(3),

        /** Compare two custom profiles. */
        PROFILE_COMPARE(4)
    }

    /**
     * Shows the profile viewer dialog.
     * @param fragmentManager The fragment manager to use.
     * @param time The time for which to view the profile.
     * @param mode The viewing mode.
     * @param customProfile The first custom profile JSON string (for applicable modes).
     * @param customProfileName The name of the first custom profile.
     * @param customProfile2 The second custom profile JSON string (for comparison mode).
     */
    fun runProfileViewerActivity(context: Context, time: Long, mode: Mode, customProfile: String? = null, customProfileName: String? = null, customProfile2: String? = null)

    /**
     * Defines types of care portal events.
     */
    enum class EventType {

        /** A blood glucose check. */
        BGCHECK,

        /** A CGM sensor insertion. */
        SENSOR_INSERT,

        /** A pump battery change. */
        BATTERY_CHANGE,

        /** A general note. */
        NOTE,

        /** An exercise event. */
        EXERCISE,

        /** A question/prompt. */
        QUESTION,

        /** An announcement. */
        ANNOUNCEMENT
    }

    /**
     * Shows the care portal dialog for a specific event type.
     * @param fragmentManager The fragment manager to use.
     * @param options The type of event.
     * @param event A string resource for the event title.
     */
    fun runCareDialog(fragmentManager: FragmentManager, options: EventType, @StringRes event: Int)

    /**
     * Opens the preferences screen for a specific plugin.
     * @param activity The host activity.
     * @param pluginSimpleName The simple class name of the plugin. If null, no activity started
     */
    fun runPreferencesForPlugin(activity: FragmentActivity, pluginSimpleName: String?)

    /**
     * Remove notification
     * @param id if of notification
     */
    fun dismissNotification(id: Int)

    /**
     * Adds a simple notification.
     * @param id The notification ID.
     * @param text The notification text.
     * @param level The urgency level.
     */
    fun addNotification(id: Int, text: String, level: Int)

    /**
     * Adds a notification that is valid for a specific duration.
     * @param id The notification ID.
     * @param text The notification text.
     * @param level The urgency level.
     * @param validMinutes The duration in minutes for which the notification is valid.
     */
    fun addNotificationValidFor(id: Int, text: String, level: Int, validMinutes: Int)

    /**
     * Adds a notification with a custom sound.
     * @param id The notification ID.
     * @param text The notification text.
     * @param level The urgency level.
     * @param soundId The raw resource ID of the sound to play.
     */
    fun addNotificationWithSound(id: Int, text: String, level: Int, @RawRes soundId: Int?)

    /**
     * Adds a notification that is valid until a specific time.
     * @param id The notification ID.
     * @param date The timestamp of the notification.
     * @param text The notification text.
     * @param level The urgency level.
     * @param validTo The timestamp until which the notification is valid.
     */
    fun addNotificationValidTo(id: Int, date: Long, text: String, level: Int, validTo: Long)

    /**
     * Adds a notification based on a Nightscout alarm object.
     * @param nsAlarm The alarm object containing notification details.
     */
    fun addNotificationWithAction(nsAlarm: NSAlarm)

    /**
     * Adds a notification with a custom action button.
     * @param id The notification ID.
     * @param text The notification text.
     * @param level The urgency level.
     * @param buttonText The string resource for the action button's text.
     * @param action The action to perform when the button is clicked.
     * @param validityCheck An optional lambda to check if the notification is still valid.
     * @param soundId Optional raw resource ID for a custom sound.
     * @param date The timestamp of the notification.
     * @param validTo The timestamp until which the notification is valid.
     */
    fun addNotificationWithAction(id: Int, text: String, level: Int, @StringRes buttonText: Int, action: Runnable, validityCheck: (() -> Boolean)?, @RawRes soundId: Int? = null, date: Long = System.currentTimeMillis(), validTo: Long = 0)

    /**
     * Add notification that shows dialog after clicking button
     * @param id if of notification
     * @param text text of notification
     * @param level urgency level of notification
     * @param buttonText label of button
     * @param title Dialog title
     * @param message Dialog body
     */
    fun addNotificationWithDialogResponse(id: Int, text: String, level: Int, @StringRes buttonText: Int, title: String, message: String, validityCheck: (() -> Boolean)?)

    /**
     * Add notification that executes [Runnable] after clicking button
     * @param id if of notification
     * @param text text of notification
     * @param level urgency level of notification
     * @param actionButtonId label of button
     * @param action Runnable to be run
     */
    fun addNotification(id: Int, text: String, level: Int, @StringRes actionButtonId: Int, action: Runnable, validityCheck: (() -> Boolean)?)

    /**
     * Shows a toast message and also posts a notification.
     * @param ctx The context.
     * @param string The message to display.
     * @param soundID The raw resource ID of a sound to play with the notification.
     */
    fun showToastAndNotification(ctx: Context, string: String, @RawRes soundID: Int)

    /**
     * Starts a repeating alarm sound.
     * @param sound The raw resource ID of the sound to play.
     * @param reason A string describing why the alarm is being started.
     */
    fun startAlarm(@RawRes sound: Int, reason: String)

    /**
     * Stops any currently playing alarm.
     * @param reason A string describing why the alarm is being stopped.
     */
    fun stopAlarm(reason: String)

    /** *******************************************************************************
     * Displays a simple alert dialog with a title, a message, and an OK button.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param onFinish The action to perform when the OK button is clicked or the dialog is dismissed. Run in UI thread.
     */
    fun showOkDialog(context: Context, title: String, message: String, onFinish: (() -> Unit)? = null)

    /** @see showOkDialog */
    fun showOkDialog(context: Context, @StringRes title: Int, @StringRes message: Int, onFinish: (() -> Unit)? = null)

    /**
     * Displays a confirmation dialog with a title, a message, and OK/Cancel buttons.
     *
     * @param context The host activity.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param ok The action to perform when the OK button is clicked. Run in UI thread.
     * @param cancel The action to perform when the Cancel button is clicked or the dialog is dismissed. Run in UI thread.
     * @param icon Add icon if providec
     */
    fun showOkCancelDialog(context: Context, @StringRes title: Int = R.string.confirmation, @StringRes message: Int, ok: (() -> Unit)?, cancel: (() -> Unit)? = null, @DrawableRes icon: Int? = null)

    /** @see showOkCancelDialog */
    fun showOkCancelDialog(context: Context, title: String = context.getString(R.string.confirmation), message: String, ok: (() -> Unit)?, cancel: (() -> Unit)? = null, @DrawableRes icon: Int? = null)

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
    fun showOkCancelDialog(context: Context, title: String = context.getString(R.string.confirmation), message: String, secondMessage: String, ok: (() -> Unit)?, cancel: (() -> Unit)? = null, @DrawableRes icon: Int? = null)

    /**
     * Displays a dialog with a title, a message, and Yes/No/Cancel buttons.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param yes The action to perform when the Yes button is clicked. Run in UI thread.
     * @param no The action to perform when the No button is clicked. The dialog is dismissed on cancel. Run in UI thread.
     */
    fun showYesNoCancel(context: Context, @StringRes title: Int, @StringRes message: Int, yes: (() -> Unit)?, no: (() -> Unit)? = null)

    /** @see showYesNoCancel */
    fun showYesNoCancel(context: Context, title: String, message: String, yes: (() -> Unit)?, no: (() -> Unit)? = null)

    /**
     * Displays a error dialog with a title, a message, a warning icon, and Dismiss/optional positive button.
     *
     * @param context The context to use for displaying the dialog.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog. HTML formatted text is accepted.
     * @param positiveButton The resource ID for the positive button text, or -1 if no positive button.
     * @param ok The action to perform when the positive button is clicked. Run in UI thread.
     * @param cancel The action to perform when the Dismiss button is clicked or the dialog is dismissed. Run in UI thread.
     */
    fun showError(context: Context, title: String, message: String, @StringRes positiveButton: Int? = null, ok: (() -> Unit)? = null, cancel: (() -> Unit)? = null)
}
