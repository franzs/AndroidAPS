package app.aaps.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.objects.extensions.getCustomizedName
import app.aaps.core.objects.profile.ProfileSealed
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.icons.Ns
import app.aaps.core.ui.compose.icons.Pump
import app.aaps.ui.R
import app.aaps.ui.compose.components.ErrorSnackbar
import app.aaps.ui.viewmodels.ProfileSwitchViewModel

/**
 * Composable screen displaying profile switches with delete and show hidden functionality.
 *
 * @param viewModel ViewModel managing state and business logic
 * @param activePlugin Active plugin for profile source
 * @param decimalFormatter Formatter for decimal values
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param uel User entry logger
 * @param rxBus RxBus for local profile changes
 * @param setToolbarConfig Callback to set the toolbar configuration
 * @param onNavigateBack Callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileSwitchScreen(
    viewModel: ProfileSwitchViewModel,
    activePlugin: app.aaps.core.interfaces.plugin.ActivePlugin,
    decimalFormatter: DecimalFormatter,
    uiInteraction: UiInteraction,
    uel: UserEntryLogger,
    setToolbarConfig: (ToolbarConfig) -> Unit,
    onNavigateBack: () -> Unit = { }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentlyActiveProfile = remember(uiState.profileSwitches) {
        viewModel.getActiveProfile()
    }

    // Update toolbar configuration whenever state changes
    LaunchedEffect(uiState.isRemovingMode, uiState.selectedItems.size, uiState.showInvalidated) {
        setToolbarConfig(
            viewModel.getToolbarConfig(
                onNavigateBack = onNavigateBack,
                onDeleteClick = {
                    if (uiState.selectedItems.isNotEmpty()) {
                        val confirmationMessage = viewModel.getDeleteConfirmationMessage()
                        uiInteraction.showOkCancelDialog(
                            context = context,
                            title = viewModel.rh.gs(app.aaps.core.ui.R.string.removerecord),
                            message = confirmationMessage,
                            ok = { viewModel.deleteSelected() }
                        )
                    }
                }
            )
        )
    }

    AapsTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            TreatmentContentContainer(
                isLoading = uiState.isLoading,
                isEmpty = uiState.profileSwitches.isEmpty()
            ) {
                val haptic = LocalHapticFeedback.current

                TreatmentLazyColumn(
                    items = uiState.profileSwitches,
                    getTimestamp = { it.timestamp },
                    getItemKey = { it.id },
                    dateUtil = viewModel.dateUtil,
                    rh = viewModel.rh,
                    itemContent = { profileSwitch ->
                        ProfileSwitchItem(
                            profileSwitch = profileSwitch,
                            isActive = profileSwitch.id == currentlyActiveProfile?.id,
                            isFuture = profileSwitch.timestamp > viewModel.dateUtil.now(),
                            isRemovingMode = uiState.isRemovingMode,
                            isSelected = profileSwitch in uiState.selectedItems,
                            onClick = {
                                if (uiState.isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
                                    // Haptic feedback for selection toggle
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Toggle selection
                                    viewModel.toggleSelection(profileSwitch)
                                }
                            },
                            onLongPress = {
                                if (profileSwitch is ProfileSealed.PS && profileSwitch.isValid && !uiState.isRemovingMode) {
                                    // Haptic feedback for selection mode entry
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Enter selection mode and select this item
                                    viewModel.enterSelectionMode(profileSwitch)
                                }
                            },
                            onClone = { ps ->
                                val profileName = ps.value.getCustomizedName(decimalFormatter)
                                val timestamp = ps.value.timestamp
                                val timestampStr = viewModel.dateUtil.dateAndTimeString(timestamp)

                                uiInteraction.showOkCancelDialog(
                                    context = context,
                                    title = viewModel.rh.gs(app.aaps.core.ui.R.string.careportal_profileswitch),
                                    message = "${viewModel.rh.gs(app.aaps.core.ui.R.string.copytolocalprofile)}\n$profileName\n$timestampStr",
                                    ok = {
                                        uel.log(
                                            action = Action.PROFILE_SWITCH_CLONED,
                                            source = Sources.Treatments,
                                            note = "$profileName ${timestampStr.replace(".", "_")}",
                                            listValues = listOf(
                                                ValueWithUnit.Timestamp(timestamp),
                                                ValueWithUnit.SimpleString(ps.value.profileName)
                                            )
                                        )
                                        val nonCustomized = ps.convertToNonCustomizedProfile(viewModel.dateUtil)
                                        activePlugin.activeProfileSource.addProfile(
                                            activePlugin.activeProfileSource.copyFrom(
                                                nonCustomized,
                                                "$profileName ${timestampStr.replace(".", "_")}"
                                            )
                                        )
                                    }
                                )
                            },
                            rh = viewModel.rh,
                            dateUtil = viewModel.dateUtil,
                            decimalFormatter = decimalFormatter
                        )
                    }
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileSwitchItem(
    profileSwitch: ProfileSealed,
    isActive: Boolean,
    isFuture: Boolean,
    isRemovingMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onClone: (ProfileSealed.PS) -> Unit,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    decimalFormatter: DecimalFormatter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        // Single row with all info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time, profile name, duration, percentage, timeshift - all in one compact format
            val profileName = when (profileSwitch) {
                is ProfileSealed.PS -> profileSwitch.value.getCustomizedName(decimalFormatter)
                is ProfileSealed.EPS -> profileSwitch.value.originalCustomizedName
                else -> profileSwitch.profileName
            }

            Text(
                text = buildString {
                    // Time
                    append(dateUtil.timeString(profileSwitch.timestamp))
                    append(" ")
                    // Profile name
                    append(profileName)
                    // Duration
                    if (profileSwitch.duration != null && profileSwitch.duration != 0L) {
                        append(" ")
                        append(T.msecs(profileSwitch.duration ?: 0L).mins().toInt())
                        append(rh.gs(R.string.unit_minute_short))
                    }
                },
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 14.sp,
                color = when {
                    isActive -> Color(AapsTheme.generalColors.activeInsulinText.value)
                    isFuture -> Color(AapsTheme.generalColors.futureRecord.value)
                    else     -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Spacer
            Box(modifier = Modifier.weight(1f))

            // Invalid indicator
            if (!profileSwitch.isValid) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(app.aaps.core.ui.R.string.invalid),
                    modifier = Modifier
                        .size(21.dp)
                        .padding(start = 5.dp),
                    tint = Color(AapsTheme.generalColors.invalidatedRecord.value)
                )
            }

            // Clone button - only for PS
            if (!isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
                Text(
                    text = stringResource(R.string.clone_label),
                    modifier = Modifier
                        .clickable {
                            onClone(profileSwitch)
                        }
                        .padding(start = 5.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }

            // Pump indicator - only for EPS (all EPS, not just those with pumpId)
            if (profileSwitch is ProfileSealed.EPS) {
                Icon(
                    imageVector = Pump,
                    contentDescription = stringResource(app.aaps.core.ui.R.string.pump_history),
                    modifier = Modifier
                        .size(21.dp)
                        .padding(start = 5.dp)
                )
            }

            // NS indicator - for both EPS and PS
            if (profileSwitch.ids?.nightscoutId != null) {
                Icon(
                    imageVector = Ns,
                    contentDescription = stringResource(app.aaps.core.ui.R.string.ns),
                    modifier = Modifier
                        .size(21.dp)
                        .padding(start = 5.dp)
                )
            }

            // Checkbox for removal
            if (isRemovingMode && profileSwitch is ProfileSealed.PS && profileSwitch.isValid) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
