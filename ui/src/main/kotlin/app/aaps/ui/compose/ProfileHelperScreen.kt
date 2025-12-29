package app.aaps.ui.compose

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.aaps.core.data.model.GlucoseUnit
import app.aaps.core.interfaces.profile.Profile
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.objects.profile.ProfileSealed
import app.aaps.core.ui.compose.NumberInputRow
import app.aaps.ui.R
import app.aaps.ui.viewmodels.ProfileHelperViewModel

/**
 * Enumeration of available profile calculation/source types.
 */
enum class ProfileType {

    MOTOL_DEFAULT,
    DPV_DEFAULT,
    CURRENT,
    AVAILABLE_PROFILE,
    PROFILE_SWITCH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHelperScreen(
    viewModel: ProfileHelperViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfileTypeMenu0 by remember { mutableStateOf(false) }
    var showProfileTypeMenu1 by remember { mutableStateOf(false) }

    val profileTypes = remember {
        mutableStateListOf(
            ProfileType.MOTOL_DEFAULT,
            ProfileType.CURRENT
        )
    }
    val ages = remember { mutableStateListOf(15, 15) }
    val weights = remember { mutableStateListOf(0.0, 0.0) }
    val tdds = remember { mutableStateListOf(0.0, 0.0) }
    val pcts = remember { mutableStateListOf(32.0, 32.0) }
    val profileIndices = remember { mutableStateListOf(0, 0) }
    val profileSwitchIndices = remember { mutableStateListOf(0, 0) }

    // Check if comparison data is valid (both profiles have sufficient data)
    val isCompareTabValid = run {
        val profile0Valid = when (profileTypes[0]) {
            ProfileType.MOTOL_DEFAULT, ProfileType.DPV_DEFAULT -> tdds[0] > 0 || weights[0] > 0
            else                                               -> true
        }
        val profile1Valid = when (profileTypes[1]) {
            ProfileType.MOTOL_DEFAULT, ProfileType.DPV_DEFAULT -> tdds[1] > 0 || weights[1] > 0
            else                                               -> true
        }
        profile0Valid && profile1Valid
    }

    // Determine which profile index to use for Clone action
    val cloneIndex = when {
        selectedTab == 2 -> {
            // On comparison tab, prefer profile 0 if it's MOTOL/DPV, otherwise profile 1
            if (profileTypes[0] == ProfileType.MOTOL_DEFAULT || profileTypes[0] == ProfileType.DPV_DEFAULT) 0 else 1
        }

        else             -> selectedTab
    }

    // Determine if Clone button should be shown in top bar (visible on all tabs when comparison is available)
    val showCloneAction = isCompareTabValid && when (selectedTab) {
        2    -> profileTypes[0] == ProfileType.MOTOL_DEFAULT || profileTypes[0] == ProfileType.DPV_DEFAULT ||
            profileTypes[1] == ProfileType.MOTOL_DEFAULT || profileTypes[1] == ProfileType.DPV_DEFAULT

        else -> profileTypes[selectedTab] == ProfileType.MOTOL_DEFAULT || profileTypes[selectedTab] == ProfileType.DPV_DEFAULT
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_profile_helper)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(app.aaps.core.ui.R.string.back))
                    }
                },
                actions = {
                    if (showCloneAction) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.copyToLocal(
                                    context,
                                    ages[cloneIndex],
                                    tdds[cloneIndex],
                                    weights[cloneIndex],
                                    pcts[cloneIndex],
                                    profileTypes[cloneIndex]
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(app.aaps.core.objects.R.drawable.ic_clone_48),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.clone_label))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Surface(tonalElevation = 2.dp) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(getProfileTypeDisplayName(profileTypes[0]))
                                    IconButton(
                                        onClick = { showProfileTypeMenu0 = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select profile type",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showProfileTypeMenu0,
                                    onDismissRequest = { showProfileTypeMenu0 = false }
                                ) {
                                    listOf(
                                        ProfileType.MOTOL_DEFAULT to stringResource(R.string.motol_default_profile),
                                        ProfileType.DPV_DEFAULT to stringResource(R.string.dpv_default_profile),
                                        ProfileType.CURRENT to stringResource(R.string.current_profile),
                                        ProfileType.AVAILABLE_PROFILE to stringResource(R.string.available_profile),
                                        ProfileType.PROFILE_SWITCH to stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)
                                    ).forEach { (type, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                profileTypes[0] = type
                                                showProfileTypeMenu0 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(getProfileTypeDisplayName(profileTypes[1]))
                                    IconButton(
                                        onClick = { showProfileTypeMenu1 = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select profile type",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showProfileTypeMenu1,
                                    onDismissRequest = { showProfileTypeMenu1 = false }
                                ) {
                                    listOf(
                                        ProfileType.MOTOL_DEFAULT to stringResource(R.string.motol_default_profile),
                                        ProfileType.DPV_DEFAULT to stringResource(R.string.dpv_default_profile),
                                        ProfileType.CURRENT to stringResource(R.string.current_profile),
                                        ProfileType.AVAILABLE_PROFILE to stringResource(R.string.available_profile),
                                        ProfileType.PROFILE_SWITCH to stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)
                                    ).forEach { (type, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                profileTypes[1] = type
                                                showProfileTypeMenu1 = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        enabled = isCompareTabValid,
                        text = {
                            Text(
                                text = stringResource(R.string.comparation),
                                color = if (!isCompareTabValid) MaterialTheme.colorScheme.error else LocalContentColor.current
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 2) {
                val profile0 = viewModel.getProfile(ages[0], tdds[0], weights[0], pcts[0] / 100.0, profileTypes[0], profileIndices[0], profileSwitchIndices[0])
                val profile1 = viewModel.getProfile(ages[1], tdds[1], weights[1], pcts[1] / 100.0, profileTypes[1], profileIndices[1], profileSwitchIndices[1])

                if (profile0 != null && profile1 != null) {
                    val name0 = viewModel.getProfileName(ages[0], tdds[0], weights[0], pcts[0] / 100.0, profileTypes[0], profileIndices[0], profileSwitchIndices[0])
                    val name1 = viewModel.getProfileName(ages[1], tdds[1], weights[1], pcts[1] / 100.0, profileTypes[1], profileIndices[1], profileSwitchIndices[1])

                    val sealed1 = ProfileSealed.Pure(profile0, null)
                    val sealed2 = ProfileSealed.Pure(profile1, null)

                    ProfileCompareContent(
                        profile1 = sealed1,
                        profile2 = sealed2,
                        unitsText = viewModel.getUnits().asText,
                        formatDia = { java.text.DecimalFormat("0.00").format(it) },
                        shortHourUnit = viewModel.rh.gs(app.aaps.core.interfaces.R.string.shorthour),
                        icsRows = buildIcRows(sealed1, sealed2, viewModel.dateUtil),
                        icUnits = viewModel.rh.gs(app.aaps.core.ui.R.string.profile_carbs_per_unit),
                        isfsRows = buildIsfRows(sealed1, sealed2, viewModel.profileUtil, viewModel.dateUtil),
                        isfUnits = "${viewModel.getUnits().asText} ${viewModel.rh.gs(app.aaps.core.ui.R.string.profile_per_unit)}",
                        basalsRows = buildBasalRows(sealed1, sealed2, viewModel.dateUtil),
                        basalUnits = viewModel.rh.gs(app.aaps.core.ui.R.string.profile_ins_units_per_hour),
                        targetsRows = buildTargetRows(sealed1, sealed2, viewModel.dateUtil, viewModel.profileUtil),
                        targetUnits = viewModel.getUnits().asText,
                        profileName1 = name0,
                        profileName2 = name1
                    )
                } else {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = stringResource(app.aaps.core.ui.R.string.no_profile_set),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    when (profileTypes[selectedTab]) {
                        ProfileType.MOTOL_DEFAULT, ProfileType.DPV_DEFAULT -> {
                            DefaultProfileContent(
                                age = ages[selectedTab],
                                onAgeChange = { ages[selectedTab] = it },
                                weight = weights[selectedTab],
                                onWeightChange = { weights[selectedTab] = it },
                                tdd = tdds[selectedTab],
                                onTddChange = { tdds[selectedTab] = it },
                                pct = pcts[selectedTab],
                                onPctChange = { pcts[selectedTab] = it },
                                showPct = profileTypes[selectedTab] == ProfileType.DPV_DEFAULT,
                                showWeight = tdds[selectedTab] == 0.0,
                                showTdd = weights[selectedTab] == 0.0,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        ProfileType.CURRENT                                -> {
                            CurrentProfileContent(profileName = state.currentProfileName, modifier = Modifier.padding(16.dp))
                        }

                        ProfileType.AVAILABLE_PROFILE                      -> {
                            AvailableProfileContent(
                                profiles = state.availableProfiles.map { it.toString() },
                                selectedIndex = profileIndices[selectedTab],
                                onProfileSelected = { profileIndices[selectedTab] = it },
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        ProfileType.PROFILE_SWITCH                         -> {
                            ProfileSwitchContent(
                                profileSwitches = state.profileSwitches.map { it.originalCustomizedName },
                                selectedIndex = profileSwitchIndices[selectedTab],
                                onProfileSwitchSelected = { profileSwitchIndices[selectedTab] = it },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                if (profileTypes[selectedTab] == ProfileType.MOTOL_DEFAULT || profileTypes[selectedTab] == ProfileType.DPV_DEFAULT) {
                    Spacer(modifier = Modifier.height(16.dp))

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            when {
                                state.isLoadingStats       -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = stringResource(app.aaps.core.ui.R.string.loading),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                state.tddStatsData != null -> {
                                    state.tddStatsData?.let { data ->
                                        TddStatsCompose(tddStatsData = data, dateUtil = viewModel.dateUtil)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun getProfileTypeDisplayName(type: ProfileType): String {
    return when (type) {
        ProfileType.MOTOL_DEFAULT     -> stringResource(R.string.motol_default_profile)
        ProfileType.DPV_DEFAULT       -> stringResource(R.string.dpv_default_profile)
        ProfileType.CURRENT           -> stringResource(R.string.current_profile)
        ProfileType.AVAILABLE_PROFILE -> stringResource(R.string.available_profile)
        ProfileType.PROFILE_SWITCH    -> stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)
    }
}

@Composable
fun DefaultProfileContent(
    age: Int, onAgeChange: (Int) -> Unit,
    weight: Double, onWeightChange: (Double) -> Unit,
    tdd: Double, onTddChange: (Double) -> Unit,
    pct: Double, onPctChange: (Double) -> Unit,
    showPct: Boolean, showWeight: Boolean, showTdd: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.profile_parameters), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
        NumberInputRow(label = stringResource(R.string.age), value = age.toDouble(), onValueChange = { onAgeChange(it.toInt()) }, minValue = 1.0, maxValue = 99.0, step = 1.0)
        if (showTdd) NumberInputRow(label = stringResource(app.aaps.core.ui.R.string.tdd_total), value = tdd, onValueChange = onTddChange, minValue = 0.0, maxValue = 200.0, step = 1.0)
        if (showWeight) NumberInputRow(label = stringResource(R.string.weight_label), value = weight, onValueChange = onWeightChange, minValue = 0.0, maxValue = 150.0, step = 1.0)
        if (showPct) NumberInputRow(label = stringResource(R.string.basal_pct_from_tdd_label), value = pct, onValueChange = onPctChange, minValue = 32.0, maxValue = 37.0, step = 1.0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentProfileContent(profileName: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = stringResource(R.string.active_profile), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        OutlinedTextField(
            value = profileName, onValueChange = {}, readOnly = true, enabled = false,
            label = { Text(stringResource(R.string.current_profile)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableProfileContent(profiles: List<String>, selectedIndex: Int, onProfileSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProfile = if (profiles.isNotEmpty() && selectedIndex < profiles.size) profiles[selectedIndex] else ""

    Column(modifier = modifier) {
        Text(text = stringResource(R.string.available_profiles), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedProfile, onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.selected_profile)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                profiles.forEachIndexed { index, profile ->
                    DropdownMenuItem(text = { Text(profile) }, onClick = { onProfileSelected(index); expanded = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitchContent(profileSwitches: List<String>, selectedIndex: Int, onProfileSwitchSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSwitch = if (profileSwitches.isNotEmpty() && selectedIndex < profileSwitches.size) profileSwitches[selectedIndex] else ""

    Column(modifier = modifier) {
        Text(text = stringResource(R.string.profile_switches), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedSwitch, onValueChange = {}, readOnly = true,
                label = { Text(stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                profileSwitches.forEachIndexed { index, profileSwitch ->
                    DropdownMenuItem(text = { Text(profileSwitch) }, onClick = { onProfileSwitchSelected(index); expanded = false })
                }
            }
        }
    }
}

private fun buildBasalRows(profile1: Profile, profile2: Profile, dateUtil: DateUtil): List<ProfileCompareRow> {
    var prev1 = -1.0
    var prev2 = -1.0
    val rows = mutableListOf<ProfileCompareRow>()
    val formatter = java.text.DecimalFormat("0.00")
    for (hour in 0..23) {
        val val1 = profile1.getBasalTimeFromMidnight(hour * 60 * 60)
        val val2 = profile2.getBasalTimeFromMidnight(hour * 60 * 60)
        if (val1 != prev1 || val2 != prev2) rows.add(ProfileCompareRow(time = dateUtil.formatHHMM(hour * 60 * 60), value1 = formatter.format(val1), value2 = formatter.format(val2)))
        prev1 = val1; prev2 = val2
    }
    rows.add(ProfileCompareRow(time = "∑", value1 = formatter.format(profile1.baseBasalSum()), value2 = formatter.format(profile2.baseBasalSum())))
    return rows
}

private fun buildIcRows(profile1: Profile, profile2: Profile, dateUtil: DateUtil): List<ProfileCompareRow> {
    var prev1 = -1.0
    var prev2 = -1.0
    val rows = mutableListOf<ProfileCompareRow>()
    val formatter = java.text.DecimalFormat("0.0")
    for (hour in 0..23) {
        val val1 = profile1.getIcTimeFromMidnight(hour * 60 * 60)
        val val2 = profile2.getIcTimeFromMidnight(hour * 60 * 60)
        if (val1 != prev1 || val2 != prev2) rows.add(ProfileCompareRow(time = dateUtil.formatHHMM(hour * 60 * 60), value1 = formatter.format(val1), value2 = formatter.format(val2)))
        prev1 = val1; prev2 = val2
    }
    return rows
}

private fun buildIsfRows(profile1: Profile, profile2: Profile, profileUtil: ProfileUtil, dateUtil: DateUtil): List<ProfileCompareRow> {
    var prev1 = -1.0
    var prev2 = -1.0
    val rows = mutableListOf<ProfileCompareRow>()
    val formatter = java.text.DecimalFormat("0.0")
    val units = profile1.units
    for (hour in 0..23) {
        val val1 = profileUtil.fromMgdlToUnits(profile1.getIsfMgdlTimeFromMidnight(hour * 60 * 60), units)
        val val2 = profileUtil.fromMgdlToUnits(profile2.getIsfMgdlTimeFromMidnight(hour * 60 * 60), units)
        if (val1 != prev1 || val2 != prev2) rows.add(ProfileCompareRow(time = dateUtil.formatHHMM(hour * 60 * 60), value1 = formatter.format(val1), value2 = formatter.format(val2)))
        prev1 = val1; prev2 = val2
    }
    return rows
}

private fun buildTargetRows(profile1: Profile, profile2: Profile, dateUtil: DateUtil, profileUtil: ProfileUtil): List<ProfileCompareRow> {
    var prev1l = -1.0
    var prev1h = -1.0
    var prev2l = -1.0
    var prev2h = -1.0
    val rows = mutableListOf<ProfileCompareRow>()
    val units = profile1.units
    val formatter = if (units == GlucoseUnit.MMOL) java.text.DecimalFormat("0.0") else java.text.DecimalFormat("0")
    for (hour in 0..23) {
        val val1l = profileUtil.fromMgdlToUnits(profile1.getTargetLowMgdlTimeFromMidnight(hour * 60 * 60), units)
        val val1h = profileUtil.fromMgdlToUnits(profile1.getTargetHighMgdlTimeFromMidnight(hour * 60 * 60), units)
        val val2l = profileUtil.fromMgdlToUnits(profile2.getTargetLowMgdlTimeFromMidnight(hour * 60 * 60), units)
        val val2h = profileUtil.fromMgdlToUnits(profile2.getTargetHighMgdlTimeFromMidnight(hour * 60 * 60), units)
        if (val1l != prev1l || val1h != prev1h || val2l != prev2l || val2h != prev2h) rows.add(
            ProfileCompareRow(
                time = dateUtil.formatHHMM(hour * 60 * 60),
                value1 = "${formatter.format(val1l)} - ${formatter.format(val1h)}",
                value2 = "${formatter.format(val2l)} - ${formatter.format(val2h)}"
            )
        )
        prev1l = val1l; prev1h = val1h; prev2l = val2l; prev2h = val2h
    }
    return rows
}
