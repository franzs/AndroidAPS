package app.aaps.ui.viewmodels

/**
 * Constants used across treatment ViewModels
 */
object TreatmentConstants {

    /** Default time range for treatment history in days */
    const val TREATMENT_HISTORY_DAYS = 30L

    /** Time range for UserEntry filtered view in days */
    const val USER_ENTRY_FILTERED_DAYS = 30L

    /** Time range for UserEntry unfiltered view in days */
    const val USER_ENTRY_UNFILTERED_DAYS = 3L

    /** Debounce duration for RxJava events in seconds */
    const val EVENT_DEBOUNCE_SECONDS = 1L
}
