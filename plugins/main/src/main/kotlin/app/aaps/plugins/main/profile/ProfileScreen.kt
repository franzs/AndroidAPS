package app.aaps.plugins.main.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.aaps.core.graph.BasalProfileGraphCompose
import app.aaps.core.graph.IcProfileGraphCompose
import app.aaps.core.graph.IsfProfileGraphCompose
import app.aaps.core.graph.TargetBgProfileGraphCompose
import app.aaps.core.objects.profile.ProfileSealed
import app.aaps.core.ui.compose.OkCancelDialog
import app.aaps.core.ui.compose.OkDialog
import app.aaps.core.ui.compose.SliderWithButtons
import app.aaps.core.ui.compose.ValueInputDialog
import app.aaps.plugins.main.R
import app.aaps.plugins.main.profile.ui.TargetValueList
import app.aaps.plugins.main.profile.ui.TimeValueList
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.localprofile)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                        )
                    }
                },
                actions = {
                    if (state.isEdited) {
                        // Reset button
                        FilledTonalIconButton(
                            onClick = { viewModel.resetProfile() },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.reset)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        // Save button
                        FilledTonalIconButton(
                            onClick = { viewModel.saveProfile() },
                            enabled = state.isValid,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.38f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.save)
                            )
                        }
                    } else if (state.isValid) {
                        // Activate profile button (when not edited and valid)
                        FilledTonalButton(
                            onClick = { /* Open profile switch dialog */ },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(app.aaps.core.ui.R.string.activate_profile))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        if (state.isLocked) {
            // Locked state - show unlock button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalButton(onClick = { /* Unlock handled by activity */ }) {
                    Text(stringResource(app.aaps.core.ui.R.string.unlock_settings))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Profile header with editable name and profile switcher
                ProfileHeader(
                    profiles = state.profiles,
                    currentIndex = state.currentProfileIndex,
                    profileName = state.currentProfile?.name ?: "",
                    onProfileSelect = { viewModel.selectProfile(it) },
                    onForceProfileSelect = { viewModel.forceSelectProfile(it) },
                    onProfileNameChange = { viewModel.updateProfileName(it) },
                    onAddProfile = { viewModel.addNewProfile() },
                    onCloneProfile = { viewModel.cloneProfile() },
                    onRemoveProfile = { viewModel.removeCurrentProfile() },
                    isEdited = state.isEdited
                )

                Spacer(Modifier.height(8.dp))

                // Units display
                Text(
                    text = "${stringResource(R.string.units_colon)} ${state.units}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Tab layout
                PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text(stringResource(R.string.dia_short)) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = { Text(stringResource(app.aaps.core.ui.R.string.ic_short)) }
                    )
                    Tab(
                        selected = state.selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        text = { Text(stringResource(app.aaps.core.ui.R.string.isf_short)) }
                    )
                    Tab(
                        selected = state.selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        text = { Text(stringResource(R.string.basal_short)) }
                    )
                    Tab(
                        selected = state.selectedTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        text = { Text(stringResource(R.string.target_short)) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Tab content
                state.currentProfile?.let { profile ->
                    when (state.selectedTab) {
                        0 -> DiaContent(
                            dia = profile.dia,
                            onDiaChange = { viewModel.updateDia(it) },
                            minDia = state.diaMin,
                            maxDia = state.diaMax
                        )

                        1 -> IcContent(
                            viewModel = viewModel,
                            profile = profile,
                            state = state,
                            supportsDynamic = state.supportsDynamicIc
                        )

                        2 -> IsfContent(
                            viewModel = viewModel,
                            profile = profile,
                            state = state,
                            supportsDynamic = state.supportsDynamicIsf
                        )

                        3 -> BasalContent(
                            viewModel = viewModel,
                            profile = profile,
                            state = state
                        )

                        4 -> TargetContent(
                            viewModel = viewModel,
                            profile = profile,
                            state = state
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileHeader(
    profiles: List<String>,
    currentIndex: Int,
    profileName: String,
    onProfileSelect: (Int) -> Unit,
    onForceProfileSelect: (Int) -> Unit,
    onProfileNameChange: (String) -> Unit,
    onAddProfile: () -> Unit,
    onCloneProfile: () -> Unit,
    onRemoveProfile: () -> Unit,
    isEdited: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(profileName) { mutableStateOf(profileName) }
    val focusRequester = remember { FocusRequester() }

    // Dialog states
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showSwitchConfirmation by remember { mutableStateOf<Int?>(null) }
    var showEditedWarning by remember { mutableStateOf<String?>(null) } // "add" or "clone"

    // Request focus when entering edit mode
    LaunchedEffect(isEditingName) {
        if (isEditingName) {
            focusRequester.requestFocus()
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        OkCancelDialog(
            title = stringResource(R.string.delete_current_profile),
            message = profileName,
            onConfirm = {
                onRemoveProfile()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    // Switch profile confirmation (when edited)
    showSwitchConfirmation?.let { targetIndex ->
        OkCancelDialog(
            message = stringResource(R.string.do_you_want_switch_profile),
            onConfirm = {
                onForceProfileSelect(targetIndex)
                showSwitchConfirmation = null
            },
            onDismiss = { showSwitchConfirmation = null }
        )
    }

    // Warning when trying to add/clone while edited
    if (showEditedWarning != null) {
        OkDialog(
            title = "",
            message = stringResource(R.string.save_or_reset_changes_first),
            onDismiss = { showEditedWarning = null }
        )
    }

    if (isEditingName) {
        // Full width editing mode
        OutlinedTextField(
            value = editedName,
            onValueChange = { editedName = it },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            trailingIcon = {
                Row {
                    // Cancel button
                    IconButton(onClick = {
                        editedName = profileName // Reset to original
                        isEditingName = false
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Confirm button
                    IconButton(onClick = {
                        onProfileNameChange(editedName)
                        isEditingName = false
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.ok),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile name with edit icon
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isEditingName = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profileName.ifEmpty { stringResource(R.string.profile_name) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_profile_name),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            // Profile switcher dropdown
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.switch_profile),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    profiles.forEachIndexed { index, profile ->
                        DropdownMenuItem(
                            text = { Text(profile) },
                            onClick = {
                                expanded = false
                                if (index != currentIndex) {
                                    if (isEdited) {
                                        showSwitchConfirmation = index
                                    } else {
                                        onProfileSelect(index)
                                    }
                                }
                            },
                            leadingIcon = if (index == currentIndex) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Add profile
            IconButton(onClick = {
                if (isEdited) {
                    showEditedWarning = "add"
                } else {
                    onAddProfile()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.a11y_add_new_profile),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Clone profile
            IconButton(onClick = {
                if (isEdited) {
                    showEditedWarning = "clone"
                } else {
                    onCloneProfile()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.a11y_clone_profile),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Remove profile
            IconButton(onClick = { showDeleteConfirmation = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.a11y_delete_current_profile),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DiaContent(
    dia: Double,
    onDiaChange: (Double) -> Unit,
    minDia: Double,
    maxDia: Double
) {
    var showDialog by remember { mutableStateOf(false) }
    val valueFormat = remember { DecimalFormat("0.0") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(app.aaps.core.ui.R.string.dia_long_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(app.aaps.core.ui.R.string.dia),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${valueFormat.format(dia)} h",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showDialog = true }
                )
            }

            SliderWithButtons(
                value = dia,
                onValueChange = onDiaChange,
                valueRange = minDia..maxDia,
                step = 0.1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDialog) {
        ValueInputDialog(
            currentValue = dia,
            valueRange = minDia..maxDia,
            step = 0.1,
            label = stringResource(app.aaps.core.ui.R.string.dia),
            unitLabel = "h",
            valueFormat = valueFormat,
            onValueConfirm = onDiaChange,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun IcContent(
    viewModel: ProfileViewModel,
    profile: SingleProfileState,
    state: ProfileUiState,
    supportsDynamic: Boolean
) {
    Column {
        if (supportsDynamic) {
            Text(
                text = stringResource(R.string.ic_dynamic_label_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            TimeValueList(
                title = stringResource(app.aaps.core.ui.R.string.ic_long_label),
                entries = profile.ic,
                onEntryChange = { index, entry -> viewModel.updateIcEntry(index, entry) },
                onAddEntry = { index -> viewModel.addIcEntry(index) },
                onRemoveEntry = { index -> viewModel.removeIcEntry(index) },
                minValue = state.icMin,
                maxValue = state.icMax,
                step = 0.1,
                valueFormat = DecimalFormat("0.0"),
                unitLabel = stringResource(app.aaps.core.ui.R.string.profile_carbs_per_unit),
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Graph
        viewModel.getEditedProfile()?.let { pureProfile ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                IcProfileGraphCompose(
                    profile1 = ProfileSealed.Pure(pureProfile, null),
                    profile1Name = profile.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun IsfContent(
    viewModel: ProfileViewModel,
    profile: SingleProfileState,
    state: ProfileUiState,
    supportsDynamic: Boolean
) {
    Column {
        if (supportsDynamic) {
            Text(
                text = stringResource(R.string.isf_dynamic_label_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            TimeValueList(
                title = stringResource(app.aaps.core.ui.R.string.isf_long_label),
                entries = profile.isf,
                onEntryChange = { index, entry -> viewModel.updateIsfEntry(index, entry) },
                onAddEntry = { index -> viewModel.addIsfEntry(index) },
                onRemoveEntry = { index -> viewModel.removeIsfEntry(index) },
                minValue = state.isfMin,
                maxValue = state.isfMax,
                step = if (profile.mgdl) 1.0 else 0.1,
                valueFormat = if (profile.mgdl) DecimalFormat("0") else DecimalFormat("0.0"),
                unitLabel = "${state.units}/${stringResource(app.aaps.core.ui.R.string.insulin_unit_shortname)}",
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Graph
        viewModel.getEditedProfile()?.let { pureProfile ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                IsfProfileGraphCompose(
                    profile1 = ProfileSealed.Pure(pureProfile, null),
                    profile1Name = profile.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BasalContent(
    viewModel: ProfileViewModel,
    profile: SingleProfileState,
    state: ProfileUiState
) {
    Column {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(app.aaps.core.ui.R.string.basal_long_label)} [${stringResource(app.aaps.core.ui.R.string.profile_ins_units_per_hour)}]",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "∑ ${DecimalFormat("0.00").format(viewModel.getBasalSum())} ${stringResource(app.aaps.core.ui.R.string.insulin_unit_shortname)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(8.dp))

                TimeValueList(
                    title = "",
                    entries = profile.basal,
                    onEntryChange = { index, entry -> viewModel.updateBasalEntry(index, entry) },
                    onAddEntry = { index -> viewModel.addBasalEntry(index) },
                    onRemoveEntry = { index -> viewModel.removeBasalEntry(index) },
                    minValue = state.basalMin,
                    maxValue = state.basalMax,
                    step = 0.01,
                    valueFormat = DecimalFormat("0.00"),
                    unitLabel = ""
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Graph
        viewModel.getEditedProfile()?.let { pureProfile ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                BasalProfileGraphCompose(
                    profile1 = ProfileSealed.Pure(pureProfile, null),
                    profile1Name = profile.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TargetContent(
    viewModel: ProfileViewModel,
    profile: SingleProfileState,
    state: ProfileUiState
) {
    Column {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            TargetValueList(
                title = stringResource(app.aaps.core.ui.R.string.target_long_label),
                lowEntries = profile.targetLow,
                highEntries = profile.targetHigh,
                onEntryChange = { index, low, high ->
                    viewModel.updateTargetEntry(index, low, high)
                },
                onAddEntry = { index -> viewModel.addTargetEntry(index) },
                onRemoveEntry = { index -> viewModel.removeTargetEntry(index) },
                minValue = state.targetMin,
                maxValue = state.targetMax,
                step = if (profile.mgdl) 1.0 else 0.1,
                valueFormat = if (profile.mgdl) DecimalFormat("0") else DecimalFormat("0.0"),
                unitLabel = state.units,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Graph
        viewModel.getEditedProfile()?.let { pureProfile ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                TargetBgProfileGraphCompose(
                    profile1 = ProfileSealed.Pure(pureProfile, null),
                    profile1Name = profile.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                )
            }
        }
    }
}

