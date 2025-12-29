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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.aaps.ui.R
import app.aaps.ui.viewmodels.StatsViewModel

/**
 * Composable screen displaying statistics including TDD, TIR, Dexcom TIR, and Activity Monitor.
 * Uses pure Material3 design with Cards and standard typography.
 *
 * @param viewModel ViewModel containing all statistics state and business logic
 * @param onNavigateBack Callback when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

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
                                .clickable { viewModel.toggleTddExpanded() }
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.tddExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (state.tddExpanded) "Collapse" else "Expand"
                            )
                            Text(
                                text = stringResource(app.aaps.core.ui.R.string.tdd),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        AnimatedVisibility(visible = state.tddExpanded) {
                            Crossfade(
                                targetState = state.tddLoading,
                                label = stringResource(app.aaps.core.ui.R.string.loading)
                            ) { isLoading ->
                                if (isLoading) {
                                    LoadingSection(
                                        title = stringResource(app.aaps.core.ui.R.string.tdd),
                                        message = stringResource(R.string.calculation_in_progress)
                                    )
                                } else {
                                    state.tddStatsData?.let { data ->
                                        TddStatsCompose(
                                            tddStatsData = data,
                                            dateUtil = viewModel.dateUtil,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.tddExpanded && !state.tddLoading) {
                    val recalculateLabel = stringResource(R.string.recalculate)
                    SmallFloatingActionButton(
                        onClick = { viewModel.recalculateTdd(context) },
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
                            .clickable { viewModel.toggleTirExpanded() }
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.tirExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (state.tirExpanded) "Collapse" else "Expand"
                        )
                        Text(
                            text = stringResource(app.aaps.core.ui.R.string.tir),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    AnimatedVisibility(visible = state.tirExpanded) {
                        Crossfade(
                            targetState = state.tirLoading,
                            label = stringResource(app.aaps.core.ui.R.string.loading)
                        ) { isLoading ->
                            if (isLoading) {
                                LoadingSection(
                                    title = stringResource(app.aaps.core.ui.R.string.tir),
                                    message = stringResource(R.string.calculation_in_progress)
                                )
                            } else {
                                state.tirStatsData?.let { data ->
                                    TirStatsCompose(
                                        tirStatsData = data,
                                        dateUtil = viewModel.dateUtil,
                                        profileUtil = viewModel.profileUtil,
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
                            .clickable { viewModel.toggleDexcomTirExpanded() }
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.dexcomTirExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (state.dexcomTirExpanded) "Collapse" else "Expand"
                        )
                        Text(
                            text = stringResource(R.string.dexcom_tir),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    AnimatedVisibility(visible = state.dexcomTirExpanded) {
                        Crossfade(
                            targetState = state.dexcomTirLoading,
                            label = stringResource(app.aaps.core.ui.R.string.loading)
                        ) { isLoading ->
                            if (isLoading) {
                                LoadingSection(
                                    title = stringResource(R.string.dexcom_tir),
                                    message = stringResource(R.string.calculation_in_progress)
                                )
                            } else {
                                state.dexcomTirData?.let { data ->
                                    DexcomTirStatsCompose(
                                        dexcomTir = data,
                                        profileUtil = viewModel.profileUtil,
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
                                .clickable { viewModel.toggleActivityExpanded() }
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.activityExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (state.activityExpanded) "Collapse" else "Expand"
                            )
                            Text(
                                text = stringResource(R.string.activity_monitor),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        AnimatedVisibility(visible = state.activityExpanded) {
                            Crossfade(
                                targetState = state.activityLoading,
                                label = stringResource(app.aaps.core.ui.R.string.loading)
                            ) { isLoading ->
                                if (isLoading) {
                                    LoadingSection(
                                        title = stringResource(R.string.activity_monitor),
                                        message = stringResource(R.string.calculation_in_progress)
                                    )
                                } else {
                                    state.activityStatsData?.let { data ->
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

                if (state.activityExpanded && !state.activityLoading) {
                    val resetLabel = stringResource(app.aaps.core.ui.R.string.reset)
                    SmallFloatingActionButton(
                        onClick = { viewModel.resetActivityStats(context) },
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
