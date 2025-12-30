package app.aaps.compose.actions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.aaps.core.interfaces.pump.actions.CustomAction

/**
 * Material 3 Expressive Actions Screen
 *
 * Design philosophy:
 * - Status cards with progress indicators for visual health overview
 * - Tile buttons (icon on top, text below) in 2-column grid layout
 * - Consistent sizing matching original ActionsFragment layout
 * - Color-coded status (green=ok, amber=warning, red=critical)
 */
@Composable
fun ActionsScreen(
    viewModel: ActionsViewModel,
    onProfileSwitchClick: () -> Unit,
    onTempTargetClick: () -> Unit,
    onTempBasalClick: () -> Unit,
    onExtendedBolusClick: () -> Unit,
    onFillClick: () -> Unit,
    onHistoryBrowserClick: () -> Unit,
    onTddStatsClick: () -> Unit,
    onBgCheckClick: () -> Unit,
    onSensorInsertClick: () -> Unit,
    onBatteryChangeClick: () -> Unit,
    onNoteClick: () -> Unit,
    onExerciseClick: () -> Unit,
    onQuestionClick: () -> Unit,
    onAnnouncementClick: () -> Unit,
    onSiteRotationClick: () -> Unit,
    onError: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Section
        StatusSection(
            sensorStatus = state.sensorStatus,
            insulinStatus = state.insulinStatus,
            cannulaStatus = state.cannulaStatus,
            batteryStatus = state.batteryStatus
        )

        // Quick Actions Section
        QuickActionsSection(
            showProfileSwitch = state.showProfileSwitch,
            showTempTarget = state.showTempTarget,
            showTempBasal = state.showTempBasal,
            showCancelTempBasal = state.showCancelTempBasal,
            showExtendedBolus = state.showExtendedBolus,
            showCancelExtendedBolus = state.showCancelExtendedBolus,
            cancelTempBasalText = state.cancelTempBasalText,
            cancelExtendedBolusText = state.cancelExtendedBolusText,
            onProfileSwitchClick = onProfileSwitchClick,
            onTempTargetClick = onTempTargetClick,
            onTempBasalClick = onTempBasalClick,
            onCancelTempBasalClick = {
                viewModel.cancelTempBasal { success, comment ->
                    if (!success) {
                        onError(
                            comment,
                            "Temp basal delivery error"
                        )
                    }
                }
            },
            onExtendedBolusClick = onExtendedBolusClick,
            onCancelExtendedBolusClick = {
                viewModel.cancelExtendedBolus { success, comment ->
                    if (!success) {
                        onError(
                            comment,
                            "Extended bolus delivery error"
                        )
                    }
                }
            }
        )

        // Careportal Section
        CareportalSection(
            showFill = state.showFill,
            showPumpBatteryChange = state.showPumpBatteryChange,
            onBgCheckClick = onBgCheckClick,
            onSensorInsertClick = onSensorInsertClick,
            onFillClick = onFillClick,
            onBatteryChangeClick = onBatteryChangeClick,
            onNoteClick = onNoteClick,
            onExerciseClick = onExerciseClick,
            onQuestionClick = onQuestionClick,
            onAnnouncementClick = onAnnouncementClick
        )

        // Tools Section
        ToolsSection(
            showHistoryBrowser = state.showHistoryBrowser,
            showTddStats = state.showTddStats,
            onSiteRotationClick = onSiteRotationClick,
            onHistoryBrowserClick = onHistoryBrowserClick,
            onTddStatsClick = onTddStatsClick
        )

        // Custom Pump Actions
        if (state.customActions.isNotEmpty()) {
            CustomActionsSection(
                customActions = state.customActions,
                onActionClick = { viewModel.executeCustomAction(it.customActionType) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatusSection(
    sensorStatus: StatusItem?,
    insulinStatus: StatusItem?,
    cannulaStatus: StatusItem?,
    batteryStatus: StatusItem?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(app.aaps.plugins.main.R.string.status),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            sensorStatus?.let { StatusRow(item = it) }
            if (sensorStatus != null && insulinStatus != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            insulinStatus?.let { StatusRow(item = it) }
            if (insulinStatus != null && cannulaStatus != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            cannulaStatus?.let { StatusRow(item = it) }
            if (cannulaStatus != null && batteryStatus != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            batteryStatus?.let { StatusRow(item = it) }
        }
    }
}

@Composable
private fun StatusRow(item: StatusItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Icon(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Label
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Age with vertical progress
        StatusValueWithProgress(
            value = item.age,
            valueColor = item.ageColor,
            progress = item.agePercent,
            progressColor = item.ageColor
        )

        // Level with vertical progress (if available)
        if (item.level != null) {
            StatusValueWithProgress(
                value = item.level,
                valueColor = item.levelColor,
                progress = item.levelPercent,
                progressColor = item.levelColor
            )
        }
    }
}

@Composable
private fun StatusValueWithProgress(
    value: String,
    valueColor: Color,
    progress: Float,
    progressColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (progress >= 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(56.dp)
                    .height(6.dp),
                color = progressColor.takeIf { it != Color.Unspecified }
                    ?: MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    showProfileSwitch: Boolean,
    showTempTarget: Boolean,
    showTempBasal: Boolean,
    showCancelTempBasal: Boolean,
    showExtendedBolus: Boolean,
    showCancelExtendedBolus: Boolean,
    cancelTempBasalText: String,
    cancelExtendedBolusText: String,
    onProfileSwitchClick: () -> Unit,
    onTempTargetClick: () -> Unit,
    onTempBasalClick: () -> Unit,
    onCancelTempBasalClick: () -> Unit,
    onExtendedBolusClick: () -> Unit,
    onCancelExtendedBolusClick: () -> Unit
) {
    val hasAnyAction = showProfileSwitch || showTempTarget || showTempBasal ||
        showCancelTempBasal || showExtendedBolus || showCancelExtendedBolus

    AnimatedVisibility(
        visible = hasAnyAction,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(app.aaps.plugins.main.R.string.actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Row 1: Profile Switch | Temp Target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showProfileSwitch) {
                        TileButton(
                            text = stringResource(app.aaps.core.ui.R.string.careportal_profileswitch),
                            iconRes = app.aaps.core.ui.R.drawable.ic_actions_profileswitch,
                            onClick = onProfileSwitchClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (showTempTarget) {
                        TileButton(
                            text = stringResource(app.aaps.core.ui.R.string.temporary_target),
                            iconRes = app.aaps.core.objects.R.drawable.ic_temptarget_high,
                            onClick = onTempTargetClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Row 2: TempBasal/Cancel | ExtendedBolus/Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left cell: Temp Basal or Cancel Temp Basal
                    when {
                        showCancelTempBasal -> TileButton(
                            text = cancelTempBasalText,
                            iconRes = app.aaps.plugins.main.R.drawable.ic_cancel_basal,
                            onClick = onCancelTempBasalClick,
                            modifier = Modifier.weight(1f),
                            isCancel = true
                        )

                        showTempBasal       -> TileButton(
                            text = stringResource(app.aaps.plugins.main.R.string.tempbasal_button),
                            iconRes = app.aaps.core.objects.R.drawable.ic_actions_start_temp_basal,
                            onClick = onTempBasalClick,
                            modifier = Modifier.weight(1f)
                        )

                        else                -> Spacer(modifier = Modifier.weight(1f))
                    }

                    // Right cell: Extended Bolus or Cancel Extended Bolus
                    when {
                        showCancelExtendedBolus -> TileButton(
                            text = cancelExtendedBolusText,
                            iconRes = app.aaps.plugins.main.R.drawable.ic_actions_cancel_extended_bolus,
                            onClick = onCancelExtendedBolusClick,
                            modifier = Modifier.weight(1f),
                            isCancel = true
                        )

                        showExtendedBolus       -> TileButton(
                            text = stringResource(app.aaps.plugins.main.R.string.extended_bolus_button),
                            iconRes = app.aaps.core.objects.R.drawable.ic_actions_start_extended_bolus,
                            onClick = onExtendedBolusClick,
                            modifier = Modifier.weight(1f)
                        )

                        else                    -> Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TileButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCancel: Boolean = false
) {
    val containerColor = if (isCancel)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (isCancel)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurface
    val iconColor = if (isCancel)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.primary

    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.labelLarge.lineHeight
            )
        }
    }
}

@Composable
private fun CareportalSection(
    showFill: Boolean,
    showPumpBatteryChange: Boolean,
    onBgCheckClick: () -> Unit,
    onSensorInsertClick: () -> Unit,
    onFillClick: () -> Unit,
    onBatteryChangeClick: () -> Unit,
    onNoteClick: () -> Unit,
    onExerciseClick: () -> Unit,
    onQuestionClick: () -> Unit,
    onAnnouncementClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(app.aaps.core.ui.R.string.careportal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Row 1: BG Check, Sensor Insert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TileButton(
                    text = stringResource(app.aaps.core.ui.R.string.careportal_bgcheck),
                    iconRes = app.aaps.core.objects.R.drawable.ic_cp_bgcheck,
                    onClick = onBgCheckClick,
                    modifier = Modifier.weight(1f)
                )
                TileButton(
                    text = stringResource(app.aaps.core.ui.R.string.cgm_sensor_insert),
                    iconRes = app.aaps.core.objects.R.drawable.ic_cp_cgm_insert,
                    onClick = onSensorInsertClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Fill, Battery Change (conditional)
            if (showFill || showPumpBatteryChange) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showFill) {
                        TileButton(
                            text = stringResource(app.aaps.core.ui.R.string.prime_fill),
                            iconRes = app.aaps.core.objects.R.drawable.ic_cp_pump_cannula,
                            onClick = onFillClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (showPumpBatteryChange) {
                        TileButton(
                            text = stringResource(app.aaps.core.ui.R.string.pump_battery_change),
                            iconRes = app.aaps.core.objects.R.drawable.ic_cp_pump_battery,
                            onClick = onBatteryChangeClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Row 3: Note, Exercise
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TileButton(
                    text = stringResource(app.aaps.core.ui.R.string.careportal_note),
                    iconRes = app.aaps.core.objects.R.drawable.ic_cp_note,
                    onClick = onNoteClick,
                    modifier = Modifier.weight(1f)
                )
                TileButton(
                    text = stringResource(app.aaps.core.ui.R.string.careportal_exercise),
                    iconRes = app.aaps.core.objects.R.drawable.ic_cp_exercise,
                    onClick = onExerciseClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: Question, Announcement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TileButton(
                    text = stringResource(app.aaps.core.ui.R.string.careportal_question),
                    iconRes = app.aaps.core.objects.R.drawable.ic_cp_question,
                    onClick = onQuestionClick,
                    modifier = Modifier.weight(1f)
                )
                TileButton(
                    text = stringResource(app.aaps.core.ui.R.string.careportal_announcement),
                    iconRes = app.aaps.core.objects.R.drawable.ic_cp_announcement,
                    onClick = onAnnouncementClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ToolsSection(
    showHistoryBrowser: Boolean,
    showTddStats: Boolean,
    onSiteRotationClick: () -> Unit,
    onHistoryBrowserClick: () -> Unit,
    onTddStatsClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(app.aaps.plugins.main.R.string.tools),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Row 1: Site Rotation, History Browser
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TileButton(
                    text = stringResource(app.aaps.plugins.main.R.string.site_rotation),
                    iconRes = app.aaps.core.ui.R.drawable.ic_site_rotation,
                    onClick = onSiteRotationClick,
                    modifier = Modifier.weight(1f)
                )
                if (showHistoryBrowser) {
                    TileButton(
                        text = stringResource(app.aaps.plugins.main.R.string.nav_history_browser),
                        iconRes = app.aaps.core.ui.R.drawable.ic_pump_history,
                        onClick = onHistoryBrowserClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Row 2: TDD Stats
            if (showTddStats) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TileButton(
                        text = stringResource(app.aaps.core.ui.R.string.tdd_short),
                        iconRes = app.aaps.core.objects.R.drawable.ic_cp_stats,
                        onClick = onTddStatsClick,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CustomActionsSection(
    customActions: List<CustomAction>,
    onActionClick: (CustomAction) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(app.aaps.plugins.main.R.string.pump_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Display custom actions in pairs (2 per row)
            customActions.chunked(2).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowActions.forEach { action ->
                        TileButton(
                            text = stringResource(action.name),
                            iconRes = action.iconResourceId,
                            onClick = { onActionClick(action) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add spacer if odd number of items
                    if (rowActions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
