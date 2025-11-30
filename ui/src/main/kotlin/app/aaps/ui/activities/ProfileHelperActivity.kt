package app.aaps.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.collection.LongSparseArray
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import app.aaps.core.data.aps.AverageTDD
import app.aaps.core.data.model.EPS
import app.aaps.core.data.model.GlucoseUnit
import app.aaps.core.data.model.TDD
import app.aaps.core.data.time.T
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.Profile
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.profile.PureProfile
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventLocalProfileChanged
import app.aaps.core.interfaces.stats.TddCalculator
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.fabric.FabricPrivacy
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.objects.profile.ProfileSealed
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.core.ui.compose.NumberInputRow
import app.aaps.ui.R
import app.aaps.ui.compose.ProfileCompareContent
import app.aaps.ui.compose.ProfileCompareRow
import app.aaps.ui.compose.TddStatsCompose
import app.aaps.ui.compose.TddStatsData
import app.aaps.ui.defaultProfile.DefaultProfile
import app.aaps.ui.defaultProfile.DefaultProfileDPV
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Activity for creating and comparing insulin profiles using various calculation methods.
 * Provides a three-tab interface for:
 *
 * **Tab 1 - Profile Input:**
 * - Select profile calculation method (Motol default, DPV default, Current, Available profiles, Profile switches)
 * - Input parameters: Age, Weight, TDD, Basal percentage
 * - View TDD statistics and history
 * - Copy calculated profiles to local storage
 *
 * **Tab 2 - Profile Comparison:**
 * - Compare two profiles side-by-side
 * - Shows comparison tables and graphs for IC, ISF, Basal, and Target values
 * - Colored legends distinguish between profiles
 *
 * **Tab 3 - TDD Statistics:**
 * - Displays historical TDD data in table and graph format
 * - Shows weighted and exponential averages
 *
 * The activity uses Jetpack Compose with Material 3 design and supports:
 * - Dynamic profile calculation based on user parameters
 * - Validation and error handling for profile generation
 * - Integration with local profile storage
 */
class ProfileHelperActivity : DaggerAppCompatActivity() {

    @Inject lateinit var preferences: Preferences
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var tddCalculator: TddCalculator
    @Inject lateinit var profileFunction: ProfileFunction
    @Inject lateinit var defaultProfile: DefaultProfile
    @Inject lateinit var defaultProfileDPV: DefaultProfileDPV
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var fabricPrivacy: FabricPrivacy
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var profileUtil: ProfileUtil

    /**
     * Enumeration of available profile calculation/source types.
     *
     * - MOTOL_DEFAULT: Calculates profile using Motol algorithm (age, TDD or weight based)
     * - DPV_DEFAULT: Calculates profile using DPV algorithm (age, TDD, basal percentage)
     * - CURRENT: Uses the currently active profile
     * - AVAILABLE_PROFILE: Selects from saved profiles in local storage
     * - PROFILE_SWITCH: Selects from recent profile switches (last 2 months)
     */
    enum class ProfileType {
        MOTOL_DEFAULT,
        DPV_DEFAULT,
        CURRENT,
        AVAILABLE_PROFILE,
        PROFILE_SWITCH
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalPreferences provides preferences,
                LocalRxBus provides rxBus
            ) {
                AapsTheme {
                    ProfileHelperScreen(
                        onBackClick = { finish() },
                        onCopyToLocal = { age, tdd, weight, pct, profileType ->
                            val profile = if (profileType == ProfileType.MOTOL_DEFAULT)
                                defaultProfile.profile(age, tdd, weight, profileFunction.getUnits())
                            else
                                defaultProfileDPV.profile(age, tdd, pct / 100.0, profileFunction.getUnits())

                            profile?.let {
                                uiInteraction.showOkCancelDialog(
                                    context = this,
                                    title = rh.gs(app.aaps.core.ui.R.string.careportal_profileswitch),
                                    message = rh.gs(app.aaps.core.ui.R.string.copytolocalprofile),
                                    ok = {
                                        activePlugin.activeProfileSource.addProfile(
                                            activePlugin.activeProfileSource.copyFrom(
                                                it,
                                                "DefaultProfile " + dateUtil.dateAndTimeAndSecondsString(dateUtil.now()).replace(".", "/")
                                            )
                                        )
                                        rxBus.send(EventLocalProfileChanged())
                                    }
                                )
                            }
                        },
                        getProfile = { age, tdd, weight, basalPct, profileType, profileIndex, profileSwitchIndex ->
                            try {
                                when (profileType) {
                                    ProfileType.MOTOL_DEFAULT     -> defaultProfile.profile(age, tdd, weight, profileFunction.getUnits())
                                    ProfileType.DPV_DEFAULT       -> defaultProfileDPV.profile(age, tdd, basalPct, profileFunction.getUnits())
                                    ProfileType.CURRENT           -> profileFunction.getProfile()?.convertToNonCustomizedProfile(dateUtil)

                                    ProfileType.AVAILABLE_PROFILE -> {
                                        val list = activePlugin.activeProfileSource.profile?.getProfileList()
                                        if (list != null && profileIndex < list.size)
                                            activePlugin.activeProfileSource.profile?.getSpecificProfile(list[profileIndex].toString())
                                        else null
                                    }

                                    ProfileType.PROFILE_SWITCH    -> {
                                        val switches = persistenceLayer.getEffectiveProfileSwitchesFromTime(
                                            dateUtil.now() - T.months(2).msecs(),
                                            true
                                        ).blockingGet()
                                        if (profileSwitchIndex < switches.size)
                                            ProfileSealed.EPS(value = switches[profileSwitchIndex], activePlugin = null)
                                                .convertToNonCustomizedProfile(dateUtil)
                                        else null
                                    }
                                }
                            } catch (_: Exception) {
                                null
                            }
                        },
                        getProfileName = { age, tdd, weight, basalPct, profileType, profileIndex, profileSwitchIndex ->
                            when (profileType) {
                                ProfileType.MOTOL_DEFAULT     -> if (tdd > 0) rh.gs(R.string.format_with_tdd, age.toDouble(), tdd)
                                else rh.gs(R.string.format_with_weight, age.toDouble(), weight)

                                ProfileType.DPV_DEFAULT       -> rh.gs(R.string.format_with_tdd_and_pct, age.toDouble(), tdd, (basalPct * 100).toInt())
                                ProfileType.CURRENT           -> profileFunction.getProfileName()

                                ProfileType.AVAILABLE_PROFILE -> {
                                    val list = activePlugin.activeProfileSource.profile?.getProfileList()
                                    if (list != null && profileIndex < list.size) list[profileIndex].toString() else ""
                                }

                                ProfileType.PROFILE_SWITCH    -> {
                                    val switches = persistenceLayer.getEffectiveProfileSwitchesFromTime(
                                        dateUtil.now() - T.months(2).msecs(),
                                        true
                                    ).blockingGet()
                                    if (profileSwitchIndex < switches.size) switches[profileSwitchIndex].originalCustomizedName else ""
                                }
                            }
                        },
                        loadTddStats = { onStatsLoaded ->
                            lifecycleScope.launch {
                                try {
                                    val data = withContext(Dispatchers.IO) {
                                        val tdds = tddCalculator.calculate(7, allowMissingDays = true)
                                        val averageTdd = tddCalculator.averageTDD(tdds)
                                        val todayTdd = tddCalculator.calculateToday()
                                        TddStatsData(tdds = tdds, averageTdd = averageTdd, todayTdd = todayTdd)
                                    }
                                    onStatsLoaded(data)
                                } catch (e: Exception) {
                                    fabricPrivacy.logException(e)
                                }
                            }
                        },
                        dateUtil = dateUtil,
                        currentProfile = profileFunction.getProfileName(),
                        availableProfiles = activePlugin.activeProfileSource.profile?.getProfileList() ?: ArrayList(),
                        profileSwitches = persistenceLayer.getEffectiveProfileSwitchesFromTime(
                            dateUtil.now() - T.months(2).msecs(),
                            true
                        ).blockingGet(),
                        profileFunction = profileFunction,
                        rh = rh,
                        profileUtil = profileUtil
                    )
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Main composable screen for ProfileHelperActivity with three-tab interface.
 * Manages state for profile selection, calculation parameters, and tab navigation.
 *
 * **Tab Structure:**
 * - Tab 0: Profile 1 input and TDD statistics
 * - Tab 1: Profile 2 input (for comparison)
 * - Tab 2: Comparison view with tables and graphs
 * - Tab 3: TDD statistics view
 *
 * **State Management:**
 * - Maintains separate state for two profiles (Profile 1 and Profile 2)
 * - Each profile has: type, age, weight, TDD, basal %, profile/switch index
 * - TDD statistics are loaded asynchronously on screen creation
 *
 * @param onBackClick Callback to finish activity
 * @param onCopyToLocal Callback to copy profile to local storage (age, TDD, weight, basalPct, profileType)
 * @param getProfile Lambda to retrieve/calculate profile based on parameters (returns PureProfile or null if error)
 * @param getProfileName Lambda to get display name for profile based on type and parameters
 * @param loadTddStats Lambda to asynchronously load TDD statistics (passes TddStatsData to callback)
 * @param dateUtil Date formatting utility
 * @param currentProfile Name of currently active profile
 * @param availableProfiles List of available saved profile names for dropdown
 * @param profileSwitches List of recent profile switches for dropdown
 * @param profileFunction Service for profile operations
 * @param profileUtil Utility for profile unit conversions
 * @param rh Resource helper for string formatting
 */
@Composable
fun ProfileHelperScreen(
    onBackClick: () -> Unit,
    onCopyToLocal: (Int, Double, Double, Double, ProfileHelperActivity.ProfileType) -> Unit,
    getProfile: (Int, Double, Double, Double, ProfileHelperActivity.ProfileType, Int, Int) -> PureProfile?,
    getProfileName: (Int, Double, Double, Double, ProfileHelperActivity.ProfileType, Int, Int) -> String,
    loadTddStats: ((TddStatsData?) -> Unit) -> Unit,
    dateUtil: DateUtil,
    currentProfile: String,
    availableProfiles: List<CharSequence>,
    profileSwitches: List<EPS>,
    profileFunction: ProfileFunction,
    profileUtil: ProfileUtil,
    rh: ResourceHelper
) {
    // UI state for tab selection and loading indicators
    var selectedTab by remember { mutableIntStateOf(0) }
    var tddStatsData by remember { mutableStateOf<TddStatsData?>(null) }
    var isLoadingStats by remember { mutableStateOf(true) }
    var showProfileTypeMenu0 by remember { mutableStateOf(false) }
    var showProfileTypeMenu1 by remember { mutableStateOf(false) }

    // Profile state maintained for two profiles (index 0 = Profile 1, index 1 = Profile 2)
    // Each list stores parameters for both profiles to enable comparison
    val profileTypes = remember {
        mutableStateListOf(
            ProfileHelperActivity.ProfileType.MOTOL_DEFAULT,
            ProfileHelperActivity.ProfileType.CURRENT
        )
    }
    val ages = remember { mutableStateListOf(15, 15) }
    val weights = remember { mutableStateListOf(0.0, 0.0) }
    val tdds = remember { mutableStateListOf(0.0, 0.0) }
    val pcts = remember { mutableStateListOf(32.0, 32.0) }  // Basal percentage for DPV calculations
    val profileIndices = remember { mutableStateListOf(0, 0) }  // Selected index for AVAILABLE_PROFILE type
    val profileSwitchIndices = remember { mutableStateListOf(0, 0) }  // Selected index for PROFILE_SWITCH type

    LaunchedEffect(Unit) {
        loadTddStats { data ->
            tddStatsData = data
            isLoadingStats = false
        }
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
            // Tabs
            Surface(
                tonalElevation = 2.dp
            ) {
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
                                        ProfileHelperActivity.ProfileType.MOTOL_DEFAULT to stringResource(R.string.motol_default_profile),
                                        ProfileHelperActivity.ProfileType.DPV_DEFAULT to stringResource(R.string.dpv_default_profile),
                                        ProfileHelperActivity.ProfileType.CURRENT to stringResource(R.string.current_profile),
                                        ProfileHelperActivity.ProfileType.AVAILABLE_PROFILE to stringResource(R.string.available_profile),
                                        ProfileHelperActivity.ProfileType.PROFILE_SWITCH to stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)
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
                                        ProfileHelperActivity.ProfileType.MOTOL_DEFAULT to stringResource(R.string.motol_default_profile),
                                        ProfileHelperActivity.ProfileType.DPV_DEFAULT to stringResource(R.string.dpv_default_profile),
                                        ProfileHelperActivity.ProfileType.CURRENT to stringResource(R.string.current_profile),
                                        ProfileHelperActivity.ProfileType.AVAILABLE_PROFILE to stringResource(R.string.available_profile),
                                        ProfileHelperActivity.ProfileType.PROFILE_SWITCH to stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)
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
                    val isCompareTabValid = run {
                        val profile0Valid = when (profileTypes[0]) {
                            ProfileHelperActivity.ProfileType.MOTOL_DEFAULT,
                            ProfileHelperActivity.ProfileType.DPV_DEFAULT -> tdds[0] > 0 || weights[0] > 0

                            else                                          -> true
                        }
                        val profile1Valid = when (profileTypes[1]) {
                            ProfileHelperActivity.ProfileType.MOTOL_DEFAULT,
                            ProfileHelperActivity.ProfileType.DPV_DEFAULT -> tdds[1] > 0 || weights[1] > 0

                            else                                          -> true
                        }
                        profile0Valid && profile1Valid
                    }
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

            // Content based on selected tab
            if (selectedTab == 2) {
                // Comparison tab
                val profile0 = getProfile(
                    ages[0],
                    tdds[0],
                    weights[0],
                    pcts[0] / 100.0,
                    profileTypes[0],
                    profileIndices[0],
                    profileSwitchIndices[0]
                )
                val profile1 = getProfile(
                    ages[1],
                    tdds[1],
                    weights[1],
                    pcts[1] / 100.0,
                    profileTypes[1],
                    profileIndices[1],
                    profileSwitchIndices[1]
                )

                if (profile0 != null && profile1 != null) {
                    val name0 = getProfileName(
                        ages[0],
                        tdds[0],
                        weights[0],
                        pcts[0] / 100.0,
                        profileTypes[0],
                        profileIndices[0],
                        profileSwitchIndices[0]
                    )
                    val name1 = getProfileName(
                        ages[1],
                        tdds[1],
                        weights[1],
                        pcts[1] / 100.0,
                        profileTypes[1],
                        profileIndices[1],
                        profileSwitchIndices[1]
                    )

                    val sealed1 = ProfileSealed.Pure(profile0, null)
                    val sealed2 = ProfileSealed.Pure(profile1, null)

                    ProfileCompareContent(
                        profile1 = sealed1,
                        profile2 = sealed2,
                        unitsText = profileFunction.getUnits().asText,
                        formatDia = { java.text.DecimalFormat("0.00").format(it) },
                        shortHourUnit = rh.gs(app.aaps.core.interfaces.R.string.shorthour),
                        icsRows = buildIcRows(sealed1, sealed2, dateUtil),
                        icUnits = rh.gs(app.aaps.core.ui.R.string.profile_carbs_per_unit),
                        isfsRows = buildIsfRows(sealed1, sealed2, profileUtil, dateUtil),
                        isfUnits = "${profileFunction.getUnits().asText} ${rh.gs(app.aaps.core.ui.R.string.profile_per_unit)}",
                        basalsRows = buildBasalRows(sealed1, sealed2, dateUtil),
                        basalUnits = rh.gs(app.aaps.core.ui.R.string.profile_ins_units_per_hour),
                        targetsRows = buildTargetRows(sealed1, sealed2, dateUtil, profileUtil),
                        targetUnits = profileFunction.getUnits().asText,
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
                // Configuration tabs (0 and 1)
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    when (profileTypes[selectedTab]) {
                        ProfileHelperActivity.ProfileType.MOTOL_DEFAULT,
                        ProfileHelperActivity.ProfileType.DPV_DEFAULT       -> {
                            DefaultProfileContent(
                                age = ages[selectedTab],
                                onAgeChange = { ages[selectedTab] = it },
                                weight = weights[selectedTab],
                                onWeightChange = { weights[selectedTab] = it },
                                tdd = tdds[selectedTab],
                                onTddChange = { tdds[selectedTab] = it },
                                pct = pcts[selectedTab],
                                onPctChange = { pcts[selectedTab] = it },
                                showPct = profileTypes[selectedTab] == ProfileHelperActivity.ProfileType.DPV_DEFAULT,
                                showWeight = tdds[selectedTab] == 0.0,
                                showTdd = weights[selectedTab] == 0.0,
                                tddStatsData = tddStatsData,
                                isLoadingStats = isLoadingStats,
                                dateUtil = dateUtil,
                                onCopyToLocal = {
                                    onCopyToLocal(
                                        ages[selectedTab],
                                        tdds[selectedTab],
                                        weights[selectedTab],
                                        pcts[selectedTab],
                                        profileTypes[selectedTab]
                                    )
                                },
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        ProfileHelperActivity.ProfileType.CURRENT           -> {
                            CurrentProfileContent(
                                profileName = currentProfile,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        ProfileHelperActivity.ProfileType.AVAILABLE_PROFILE -> {
                            AvailableProfileContent(
                                profiles = availableProfiles.map { it.toString() },
                                selectedIndex = profileIndices[selectedTab],
                                onProfileSelected = { profileIndices[selectedTab] = it },
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        ProfileHelperActivity.ProfileType.PROFILE_SWITCH    -> {
                            ProfileSwitchContent(
                                profileSwitches = profileSwitches.map { it.originalCustomizedName },
                                selectedIndex = profileSwitchIndices[selectedTab],
                                onProfileSwitchSelected = { profileSwitchIndices[selectedTab] = it },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Returns the localized display name for a ProfileType.
 * Used in dropdown menus and profile selection UI.
 *
 * @param type The ProfileType to get display name for
 * @return Localized string resource for the profile type
 */
@Composable
fun getProfileTypeDisplayName(type: ProfileHelperActivity.ProfileType): String {
    return when (type) {
        ProfileHelperActivity.ProfileType.MOTOL_DEFAULT     -> stringResource(R.string.motol_default_profile)
        ProfileHelperActivity.ProfileType.DPV_DEFAULT       -> stringResource(R.string.dpv_default_profile)
        ProfileHelperActivity.ProfileType.CURRENT           -> stringResource(R.string.current_profile)
        ProfileHelperActivity.ProfileType.AVAILABLE_PROFILE -> stringResource(R.string.available_profile)
        ProfileHelperActivity.ProfileType.PROFILE_SWITCH    -> stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)
    }
}

/**
 * Composable for inputting parameters for default profile calculation (Motol or DPV algorithms).
 * Displays dynamic input fields based on the selected profile type:
 * - Age (always shown): Patient age in years (1-99)
 * - Weight (optional): Patient weight in kg, shown for Motol when TDD is not available
 * - TDD (optional): Total Daily Dose in units, shown for both Motol and DPV
 * - Basal % (optional): Basal percentage (0-100), shown for DPV algorithm
 *
 * Also displays TDD statistics table when available, and a "Copy to Local Profile" button.
 *
 * @param age Current age value
 * @param onAgeChange Callback when age changes
 * @param weight Current weight value in kg
 * @param onWeightChange Callback when weight changes
 * @param tdd Current TDD value in units
 * @param onTddChange Callback when TDD changes
 * @param pct Current basal percentage (0-100)
 * @param onPctChange Callback when basal percentage changes
 * @param showPct Whether to show basal percentage input (true for DPV algorithm)
 * @param showWeight Whether to show weight input (true for Motol when TDD = 0)
 * @param showTdd Whether to show TDD input
 * @param tddStatsData TDD statistics data for display (null if not loaded)
 * @param isLoadingStats Whether TDD statistics are currently loading
 * @param dateUtil Date utility for formatting
 * @param onCopyToLocal Callback when "Copy to Local Profile" button is clicked
 * @param modifier Modifier for the root Column
 */
@Composable
fun DefaultProfileContent(
    age: Int,
    onAgeChange: (Int) -> Unit,
    weight: Double,
    onWeightChange: (Double) -> Unit,
    tdd: Double,
    onTddChange: (Double) -> Unit,
    pct: Double,
    onPctChange: (Double) -> Unit,
    showPct: Boolean,
    showWeight: Boolean,
    showTdd: Boolean,
    tddStatsData: TddStatsData?,
    isLoadingStats: Boolean,
    dateUtil: DateUtil,
    onCopyToLocal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.profile_parameters),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        // Age
        NumberInputRow(
            label = stringResource(R.string.age),
            value = age.toDouble(),
            onValueChange = { onAgeChange(it.toInt()) },
            minValue = 1.0,
            maxValue = 99.0,
            step = 1.0
        )

        if (showTdd) {
            NumberInputRow(
                label = stringResource(app.aaps.core.ui.R.string.tdd_total),
                value = tdd,
                onValueChange = onTddChange,
                minValue = 0.0,
                maxValue = 200.0,
                step = 1.0
            )
        }

        if (showWeight) {
            NumberInputRow(
                label = stringResource(R.string.weight_label),
                value = weight,
                onValueChange = onWeightChange,
                minValue = 0.0,
                maxValue = 150.0,
                step = 1.0
            )
        }

        if (showPct) {
            NumberInputRow(
                label = stringResource(R.string.basal_pct_from_tdd_label),
                value = pct,
                onValueChange = onPctChange,
                minValue = 32.0,
                maxValue = 37.0,
                step = 1.0
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoadingStats       -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(app.aaps.core.ui.R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            tddStatsData != null -> {
                TddStatsCompose(
                    tddStatsData = tddStatsData,
                    dateUtil = dateUtil
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = onCopyToLocal,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                painter = painterResource(app.aaps.core.objects.R.drawable.ic_clone_48),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.clone_label))
        }
    }
}

/**
 * Composable for displaying the currently active profile information.
 * Shows a read-only card with the profile name and a tooltip explaining
 * that this displays the current active profile from the system.
 *
 * @param profileName Name of the currently active profile
 * @param modifier Modifier for the root Column
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentProfileContent(
    profileName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.active_profile),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = profileName,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(stringResource(R.string.current_profile)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Composable for selecting a profile from the list of available saved profiles.
 * Displays a dropdown menu with all profiles from local storage.
 * User can select one profile to view or compare.
 *
 * @param profiles List of available profile names from local storage
 * @param selectedIndex Currently selected profile index (0-based)
 * @param onProfileSelected Callback when a profile is selected (passes new index)
 * @param modifier Modifier for the root Column
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableProfileContent(
    profiles: List<String>,
    selectedIndex: Int,
    onProfileSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedProfile = if (profiles.isNotEmpty() && selectedIndex < profiles.size) profiles[selectedIndex] else ""

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.available_profiles),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedProfile,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.selected_profile)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                profiles.forEachIndexed { index, profile ->
                    DropdownMenuItem(
                        text = { Text(profile) },
                        onClick = {
                            onProfileSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Composable for selecting a profile from recent profile switches.
 * Displays a dropdown menu with effective profile switches from the last 2 months.
 * Shows customized names with percentage adjustments and timeshift information.
 *
 * @param profileSwitches List of profile switch names (original customized names from EPS)
 * @param selectedIndex Currently selected profile switch index (0-based)
 * @param onProfileSwitchSelected Callback when a profile switch is selected (passes new index)
 * @param modifier Modifier for the root Column
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitchContent(
    profileSwitches: List<String>,
    selectedIndex: Int,
    onProfileSwitchSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSwitch = if (profileSwitches.isNotEmpty() && selectedIndex < profileSwitches.size) profileSwitches[selectedIndex] else ""

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.profile_switches),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedSwitch,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(app.aaps.core.ui.R.string.careportal_profileswitch)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                profileSwitches.forEachIndexed { index, profileSwitch ->
                    DropdownMenuItem(
                        text = { Text(profileSwitch) },
                        onClick = {
                            onProfileSwitchSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Helper functions for profile comparison
private fun buildBasalRows(profile1: Profile, profile2: Profile, dateUtil: DateUtil): List<ProfileCompareRow> {
    var prev1 = -1.0
    var prev2 = -1.0
    val rows = mutableListOf<ProfileCompareRow>()
    val formatter = java.text.DecimalFormat("0.00")
    for (hour in 0..23) {
        val val1 = profile1.getBasalTimeFromMidnight(hour * 60 * 60)
        val val2 = profile2.getBasalTimeFromMidnight(hour * 60 * 60)
        if (val1 != prev1 || val2 != prev2) {
            rows.add(
                ProfileCompareRow(
                    time = dateUtil.formatHHMM(hour * 60 * 60),
                    value1 = formatter.format(val1),
                    value2 = formatter.format(val2)
                )
            )
        }
        prev1 = val1
        prev2 = val2
    }
    // Add summary row
    rows.add(
        ProfileCompareRow(
            time = "∑",
            value1 = formatter.format(profile1.baseBasalSum()),
            value2 = formatter.format(profile2.baseBasalSum())
        )
    )
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
        if (val1 != prev1 || val2 != prev2) {
            rows.add(
                ProfileCompareRow(
                    time = dateUtil.formatHHMM(hour * 60 * 60),
                    value1 = formatter.format(val1),
                    value2 = formatter.format(val2)
                )
            )
        }
        prev1 = val1
        prev2 = val2
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
        val val1Mgdl = profile1.getIsfMgdlTimeFromMidnight(hour * 60 * 60)
        val val2Mgdl = profile2.getIsfMgdlTimeFromMidnight(hour * 60 * 60)
        val val1 = profileUtil.fromMgdlToUnits(val1Mgdl, units)
        val val2 = profileUtil.fromMgdlToUnits(val2Mgdl, units)
        if (val1 != prev1 || val2 != prev2) {
            rows.add(
                ProfileCompareRow(
                    time = dateUtil.formatHHMM(hour * 60 * 60),
                    value1 = formatter.format(val1),
                    value2 = formatter.format(val2)
                )
            )
        }
        prev1 = val1
        prev2 = val2
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
        val val1lMgdl = profile1.getTargetLowMgdlTimeFromMidnight(hour * 60 * 60)
        val val1hMgdl = profile1.getTargetHighMgdlTimeFromMidnight(hour * 60 * 60)
        val val2lMgdl = profile2.getTargetLowMgdlTimeFromMidnight(hour * 60 * 60)
        val val2hMgdl = profile2.getTargetHighMgdlTimeFromMidnight(hour * 60 * 60)

        val val1l = profileUtil.fromMgdlToUnits(val1lMgdl, units)
        val val1h = profileUtil.fromMgdlToUnits(val1hMgdl, units)
        val val2l = profileUtil.fromMgdlToUnits(val2lMgdl, units)
        val val2h = profileUtil.fromMgdlToUnits(val2hMgdl, units)

        if (val1l != prev1l || val1h != prev1h || val2l != prev2l || val2h != prev2h) {
            rows.add(
                ProfileCompareRow(
                    time = dateUtil.formatHHMM(hour * 60 * 60),
                    value1 = "${formatter.format(val1l)} - ${formatter.format(val1h)}",
                    value2 = "${formatter.format(val2l)} - ${formatter.format(val2h)}"
                )
            )
        }
        prev1l = val1l
        prev1h = val1h
        prev2l = val2l
        prev2h = val2h
    }
    return rows
}
