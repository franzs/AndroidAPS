package app.aaps.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aaps.core.data.model.UE
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.maintenance.ImportExportPrefs
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.userEntry.UserEntryPresentationHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.Translator
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.ui.R
import app.aaps.ui.compose.components.ErrorSnackbar
import app.aaps.ui.viewmodels.UserEntryViewModel

/**
 * Composable screen displaying user entry log with optional loop records filtering.
 *
 * @param viewModel ViewModel managing state and business logic
 * @param userEntryPresentationHelper Helper for formatting user entry display
 * @param translator Translator for action names
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param importExportPrefs Import/export preferences helper
 * @param uel User entry logger
 * @param setToolbarConfig Lambda to set toolbar configuration
 * @param onNavigateBack Lambda to handle back navigation
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserEntryScreen(
    viewModel: UserEntryViewModel,
    userEntryPresentationHelper: UserEntryPresentationHelper,
    translator: Translator,
    uiInteraction: UiInteraction,
    importExportPrefs: ImportExportPrefs,
    uel: UserEntryLogger,
    setToolbarConfig: (ToolbarConfig) -> Unit,
    onNavigateBack: () -> Unit = { }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Update toolbar configuration
    LaunchedEffect(uiState.showLoop) {
        setToolbarConfig(
            TreatmentScreenToolbar(
                isRemovingMode = false,
                selectedCount = 0,
                onExitRemovingMode = { },
                onNavigateBack = onNavigateBack,
                onDelete = { },
                rh = viewModel.rh,
                showLoop = uiState.showLoop,
                onToggleLoop = { viewModel.toggleLoop() },
                menuItems = listOf(
                    MenuItemData(
                        label = viewModel.rh.gs(app.aaps.core.ui.R.string.ue_export_to_csv),
                        onClick = {
                            uiInteraction.showOkCancelDialog(
                                context = context,
                                title = viewModel.rh.gs(app.aaps.core.ui.R.string.confirm),
                                message = viewModel.rh.gs(app.aaps.core.ui.R.string.ue_export_to_csv) + "?",
                                ok = {
                                    uel.log(Action.EXPORT_CSV, Sources.Treatments)
                                    importExportPrefs.exportUserEntriesCsv(context)
                                }
                            )
                        }
                    )
                )
            )
        )
    }

    AapsTheme {
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.userEntries.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_records_available),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(50.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    // Group items by day for sticky headers (optimized with derivedStateOf)
                    val groupedByDay by remember {
                        derivedStateOf {
                            uiState.userEntries.groupBy { ue ->
                                val timestamp = ue.timestamp
                                viewModel.dateUtil.dateString(timestamp)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        groupedByDay.forEach { (dateString, itemsForDay) ->
                            stickyHeader(key = dateString) {
                                Text(
                                    text = viewModel.dateUtil.dateStringRelative(itemsForDay.first().timestamp, viewModel.rh),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(
                                items = itemsForDay,
                                key = { item -> item.id }
                            ) { ue ->
                                Box(modifier = Modifier.animateItem()) {
                                    UserEntryItem(
                                        userEntry = ue,
                                        userEntryPresentationHelper = userEntryPresentationHelper,
                                        translator = translator,
                                        rh = viewModel.rh,
                                        dateUtil = viewModel.dateUtil
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error display
            ErrorSnackbar(
                error = uiState.error,
                onDismiss = { viewModel.clearError() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun UserEntryItem(
    userEntry: UE,
    userEntryPresentationHelper: UserEntryPresentationHelper,
    translator: Translator,
    rh: ResourceHelper,
    dateUtil: DateUtil
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(1.dp)
        ) {
            // Main content row: Time, Action, Source Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 3.dp, end = 8.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = dateUtil.timeStringWithSeconds(userEntry.timestamp),
                    fontSize = 14.sp
                )

                // Action - Compose Text with AnnotatedString
                Text(
                    text = actionToAnnotatedString(
                        action = userEntry.action,
                        translator = translator
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    fontSize = 14.sp
                )

                // Source Icon
                Image(
                    painter = painterResource(id = userEntryPresentationHelper.iconId(userEntry.source)),
                    contentDescription = "${stringResource(app.aaps.core.ui.R.string.ue_source)}: ${userEntry.source}",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
            }

            // Values row - only show if not empty
            val valuesText = userEntryPresentationHelper.listToPresentationString(userEntry.values)
            if (valuesText.isNotEmpty()) {
                Text(
                    text = valuesText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 8.dp, top = 2.dp, bottom = 1.dp),
                    fontSize = 12.sp
                )
            }

            // Notes row - only show if not empty
            if (userEntry.note.isNotEmpty()) {
                Text(
                    text = userEntry.note,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 8.dp, top = 1.dp, bottom = 2.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Map Action.ColorGroup to theme colors
 */
@Composable
private fun Action.ColorGroup.toElementColor(): Color = when (this) {
    Action.ColorGroup.InsulinTreatment -> AapsTheme.elementColors.insulin
    Action.ColorGroup.BasalTreatment   -> AapsTheme.elementColors.tempBasal
    Action.ColorGroup.CarbTreatment    -> AapsTheme.elementColors.carbs
    Action.ColorGroup.TT               -> AapsTheme.elementColors.tempTarget
    Action.ColorGroup.Profile          -> AapsTheme.elementColors.profileSwitch
    Action.ColorGroup.Loop             -> AapsTheme.elementColors.loop
    Action.ColorGroup.Careportal       -> AapsTheme.elementColors.careportal
    Action.ColorGroup.Pump             -> AapsTheme.elementColors.pump
    Action.ColorGroup.Aaps             -> AapsTheme.elementColors.aaps
    Action.ColorGroup.RunningMode      -> AapsTheme.elementColors.runningMode
}

/**
 * Convert an Action to a Compose AnnotatedString with colored text
 */
@Composable
private fun actionToAnnotatedString(
    action: Action,
    translator: Translator
): AnnotatedString = when (action) {
    Action.TREATMENT -> buildAnnotatedString {
        withStyle(style = SpanStyle(color = Action.BOLUS.colorGroup.toElementColor())) {
            append(translator.translate(Action.BOLUS))
        }
        append(" + ")
        withStyle(style = SpanStyle(color = Action.CARBS.colorGroup.toElementColor())) {
            append(translator.translate(Action.CARBS))
        }
    }

    else             -> buildAnnotatedString {
        withStyle(style = SpanStyle(color = action.colorGroup.toElementColor())) {
            append(translator.translate(action))
        }
    }
}
