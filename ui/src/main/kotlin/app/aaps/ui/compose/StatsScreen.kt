package app.aaps.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.aaps.core.data.configuration.Constants
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.stats.DexcomTIR
import app.aaps.core.interfaces.stats.DexcomTirCalculator
import app.aaps.core.interfaces.stats.TddCalculator
import app.aaps.core.interfaces.stats.TirCalculator
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.ui.R
import app.aaps.ui.activityMonitor.ActivityMonitor
import app.aaps.ui.activityMonitor.ActivityStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Composable screen displaying statistics including TDD, TIR, Dexcom TIR, and Activity Monitor.
 * Uses pure Material3 design with Cards and standard typography.
 *
 * @param tddCalculator Calculator for Total Daily Dose statistics
 * @param tirCalculator Calculator for Time In Range statistics
 * @param dexcomTirCalculator Calculator for Dexcom Time In Range statistics
 * @param activityMonitor Monitor for activity statistics
 * @param persistenceLayer Database layer for clearing cached data
 * @param rh Resource helper for string resources
 * @param uiInteraction UI interaction helper for showing dialogs
 * @param uel User entry logger for logging actions
 * @param dateUtil Date utility for formatting dates
 * @param profileUtil Profile utility for unit conversion
 * @param onNavigateBack Callback when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    tddCalculator: TddCalculator,
    tirCalculator: TirCalculator,
    dexcomTirCalculator: DexcomTirCalculator,
    activityMonitor: ActivityMonitor,
    persistenceLayer: PersistenceLayer,
    rh: ResourceHelper,
    uiInteraction: UiInteraction,
    uel: UserEntryLogger,
    dateUtil: DateUtil,
    profileUtil: ProfileUtil,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tddStatsData by remember { mutableStateOf<TddStatsData?>(null) }
    var tirStatsData by remember { mutableStateOf<TirStatsData?>(null) }
    var dexcomTirData by remember { mutableStateOf<DexcomTIR?>(null) }
    var activityStatsData by remember { mutableStateOf<List<ActivityStats>?>(null) }

    var tddLoading by remember { mutableStateOf(true) }
    var tirLoading by remember { mutableStateOf(true) }
    var dexcomTirLoading by remember { mutableStateOf(true) }
    var activityLoading by remember { mutableStateOf(true) }

    var tddExpanded by remember { mutableStateOf(true) }
    var tirExpanded by remember { mutableStateOf(false) }
    var dexcomTirExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }

    var tddRefreshKey by remember { mutableStateOf(0) }

    // Load TDD stats
    LaunchedEffect(tddRefreshKey) {
        tddLoading = true
        tddStatsData = withContext(Dispatchers.IO) {
            val tdds = tddCalculator.calculate(7, allowMissingDays = true)
            val averageTdd = tddCalculator.averageTDD(tdds)
            val todayTdd = tddCalculator.calculateToday()
            TddStatsData(tdds = tdds, averageTdd = averageTdd, todayTdd = todayTdd)
        }
        tddLoading = false
    }

    // Load TIR stats
    LaunchedEffect(Unit) {
        tirLoading = true
        tirStatsData = withContext(Dispatchers.IO) {
            val lowTirMgdl = Constants.STATS_RANGE_LOW_MMOL * Constants.MMOLL_TO_MGDL
            val highTirMgdl = Constants.STATS_RANGE_HIGH_MMOL * Constants.MMOLL_TO_MGDL
            val lowTitMgdl = Constants.STATS_TARGET_LOW_MMOL * Constants.MMOLL_TO_MGDL
            val highTitMgdl = Constants.STATS_TARGET_HIGH_MMOL * Constants.MMOLL_TO_MGDL

            val tir7 = tirCalculator.calculate(7, lowTirMgdl, highTirMgdl)
            val tir30 = tirCalculator.calculate(30, lowTirMgdl, highTirMgdl)
            val tit7 = tirCalculator.calculate(7, lowTitMgdl, highTitMgdl)
            val tit30 = tirCalculator.calculate(30, lowTitMgdl, highTitMgdl)

            TirStatsData(
                tir7 = tir7,
                averageTir7 = tirCalculator.averageTIR(tir7),
                averageTir30 = tirCalculator.averageTIR(tir30),
                lowTirMgdl = lowTirMgdl,
                highTirMgdl = highTirMgdl,
                lowTitMgdl = lowTitMgdl,
                highTitMgdl = highTitMgdl,
                averageTit7 = tirCalculator.averageTIR(tit7),
                averageTit30 = tirCalculator.averageTIR(tit30)
            )
        }
        tirLoading = false
    }

    // Load Dexcom TIR stats
    LaunchedEffect(Unit) {
        dexcomTirLoading = true
        dexcomTirData = withContext(Dispatchers.IO) {
            dexcomTirCalculator.calculate()
        }
        dexcomTirLoading = false
    }

    // Load Activity stats
    LaunchedEffect(Unit) {
        activityLoading = true
        activityStatsData = withContext(Dispatchers.IO) {
            activityMonitor.getActivityStats()
        }
        activityLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // TDD Section
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tddExpanded = !tddExpanded }
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (tddExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (tddExpanded) "Collapse" else "Expand"
                            )
                            Text(
                                text = stringResource(app.aaps.core.ui.R.string.tdd),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        AnimatedVisibility(visible = tddExpanded) {
                            Crossfade(
                                targetState = tddLoading,
                                label = stringResource(app.aaps.core.ui.R.string.loading)
                            ) { isLoading ->
                                if (isLoading) {
                                    LoadingSection(
                                        title = stringResource(app.aaps.core.ui.R.string.tdd),
                                        message = stringResource(R.string.calculation_in_progress)
                                    )
                                } else {
                                    tddStatsData?.let { data ->
                                        TddStatsCompose(
                                            tddStatsData = data,
                                            dateUtil = dateUtil,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (tddExpanded && !tddLoading) {
                    val recalculateLabel = stringResource(R.string.recalculate)
                    SmallFloatingActionButton(
                        onClick = {
                            uiInteraction.showOkCancelDialog(
                                context = context,
                                message = rh.gs(R.string.do_you_want_recalculate_tdd_stats),
                                ok = {
                                    uel.log(Action.STAT_RESET, Sources.Stats)
                                    scope.launch {
                                        persistenceLayer.clearCachedTddData(0)
                                        tddRefreshKey++
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .semantics {
                                contentDescription = recalculateLabel
                            },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                }
            }

            // TIR Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tirExpanded = !tirExpanded }
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (tirExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (tirExpanded) "Collapse" else "Expand"
                        )
                        Text(
                            text = stringResource(app.aaps.core.ui.R.string.tir),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    AnimatedVisibility(visible = tirExpanded) {
                        Crossfade(
                            targetState = tirLoading,
                            label = stringResource(app.aaps.core.ui.R.string.loading)
                        ) { isLoading ->
                            if (isLoading) {
                                LoadingSection(
                                    title = stringResource(app.aaps.core.ui.R.string.tir),
                                    message = stringResource(R.string.calculation_in_progress)
                                )
                            } else {
                                tirStatsData?.let { data ->
                                    TirStatsCompose(
                                        tirStatsData = data,
                                        dateUtil = dateUtil,
                                        profileUtil = profileUtil,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dexcom TIR Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dexcomTirExpanded = !dexcomTirExpanded }
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (dexcomTirExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (dexcomTirExpanded) "Collapse" else "Expand"
                        )
                        Text(
                            text = stringResource(R.string.dexcom_tir),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    AnimatedVisibility(visible = dexcomTirExpanded) {
                        Crossfade(
                            targetState = dexcomTirLoading,
                            label = stringResource(app.aaps.core.ui.R.string.loading)
                        ) { isLoading ->
                            if (isLoading) {
                                LoadingSection(
                                    title = stringResource(R.string.dexcom_tir),
                                    message = stringResource(R.string.calculation_in_progress)
                                )
                            } else {
                                dexcomTirData?.let { data ->
                                    DexcomTirStatsCompose(
                                        dexcomTir = data,
                                        profileUtil = profileUtil,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Activity Section with Reset button
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activityExpanded = !activityExpanded }
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (activityExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (activityExpanded) "Collapse" else "Expand"
                            )
                            Text(
                                text = stringResource(R.string.activity_monitor),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        AnimatedVisibility(visible = activityExpanded) {
                            Crossfade(
                                targetState = activityLoading,
                                label = stringResource(app.aaps.core.ui.R.string.loading)
                            ) { isLoading ->
                                if (isLoading) {
                                    LoadingSection(
                                        title = stringResource(R.string.activity_monitor),
                                        message = stringResource(R.string.calculation_in_progress)
                                    )
                                } else {
                                    activityStatsData?.let { data ->
                                        ActivityStatsCompose(
                                            activityStats = data,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (activityExpanded && !activityLoading) {
                    val resetLabel = stringResource(app.aaps.core.ui.R.string.reset)
                    SmallFloatingActionButton(
                        onClick = {
                            uiInteraction.showOkCancelDialog(
                                context = context,
                                message = rh.gs(R.string.do_you_want_reset_stats),
                                ok = {
                                    uel.log(Action.STAT_RESET, Sources.Stats)
                                    scope.launch(Dispatchers.IO) {
                                        activityMonitor.reset()
                                        activityLoading = true
                                        activityStatsData = activityMonitor.getActivityStats()
                                        activityLoading = false
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .semantics {
                                contentDescription = resetLabel
                            },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
