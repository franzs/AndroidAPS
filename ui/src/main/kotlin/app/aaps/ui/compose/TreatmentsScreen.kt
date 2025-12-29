package app.aaps.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.ToolbarConfig
import app.aaps.core.ui.compose.icons.Carbs
import app.aaps.core.ui.compose.icons.Careportal
import app.aaps.core.ui.compose.icons.ExtendedBolus
import app.aaps.core.ui.compose.icons.ProfileSwitch
import app.aaps.core.ui.compose.icons.RunningMode
import app.aaps.core.ui.compose.icons.TempBasal
import app.aaps.core.ui.compose.icons.TempTarget
import app.aaps.core.ui.compose.icons.UserEntry
import app.aaps.ui.R
import app.aaps.ui.viewmodels.TreatmentsViewModel
import kotlinx.coroutines.launch

/**
 * Composable screen displaying treatments with tab navigation.
 * Uses Jetpack Compose for all content including each treatment type.
 *
 * @param viewModel ViewModel containing all dependencies and child ViewModels
 * @param onNavigateBack Callback when back navigation is requested
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TreatmentsScreen(
    viewModel: TreatmentsViewModel,
    onNavigateBack: () -> Unit
) {
    val showExtendedBolusTab = viewModel.showExtendedBolusTab()
    val iconColors = AapsTheme.elementColors
    var toolbarConfig by remember {
        mutableStateOf(
            ToolbarConfig(
                title = "",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                        )
                    }
                },
                actions = { }
            )
        )
    }

    // Track which page should be allowed to set toolbar
    var allowedToolbarPage by remember { mutableStateOf(0) }

    // Define tabs with their icons and content
    val tabs = remember(showExtendedBolusTab) {
        var currentIndex = 0
        buildList {
            val pageIndex0 = currentIndex++
            add(
                TreatmentTab(
                    icon = Carbs,
                    titleRes = R.string.carbs_and_bolus,
                    colorGetter = { iconColors.carbs },
                    content = {
                        BolusCarbsScreen(
                            viewModel = viewModel.bolusCarbsViewModel,
                            activePlugin = viewModel.activePlugin,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex0) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
            if (showExtendedBolusTab) {
                val pageIndex1 = currentIndex++
                add(
                    TreatmentTab(
                        icon = ExtendedBolus,
                        titleRes = app.aaps.core.ui.R.string.extended_bolus,
                        colorGetter = { iconColors.extendedBolus },
                        content = {
                            ExtendedBolusScreen(
                                viewModel = viewModel.extendedBolusViewModel,
                                profileFunction = viewModel.profileFunction,
                                activeInsulin = viewModel.activePlugin.activeInsulin,
                                setToolbarConfig = { config ->
                                    if (allowedToolbarPage == pageIndex1) toolbarConfig = config
                                },
                                onNavigateBack = onNavigateBack
                            )
                        }
                    )
                )
            }
            val pageIndex2 = currentIndex++
            add(
                TreatmentTab(
                    icon = TempBasal,
                    titleRes = app.aaps.core.ui.R.string.tempbasal_label,
                    colorGetter = { iconColors.tempBasal },
                    content = {
                        TempBasalScreen(
                            viewModel = viewModel.tempBasalViewModel,
                            profileFunction = viewModel.profileFunction,
                            activePlugin = viewModel.activePlugin,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex2) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
            val pageIndex3 = currentIndex++
            add(
                TreatmentTab(
                    icon = TempTarget,
                    titleRes = app.aaps.core.ui.R.string.temporary_target,
                    colorGetter = { iconColors.tempTarget },
                    content = {
                        TempTargetScreen(
                            viewModel = viewModel.tempTargetViewModel,
                            profileUtil = viewModel.profileUtil,
                            translator = viewModel.translator,
                            decimalFormatter = viewModel.decimalFormatter,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex3) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
            val pageIndex4 = currentIndex++
            add(
                TreatmentTab(
                    icon = ProfileSwitch,
                    titleRes = app.aaps.core.ui.R.string.careportal_profileswitch,
                    colorGetter = { iconColors.profileSwitch },
                    content = {
                        ProfileSwitchScreen(
                            viewModel = viewModel.profileSwitchViewModel,
                            activePlugin = viewModel.activePlugin,
                            decimalFormatter = viewModel.decimalFormatter,
                            uel = viewModel.uel,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex4) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
            val pageIndex5 = currentIndex++
            add(
                TreatmentTab(
                    icon = Careportal,
                    titleRes = app.aaps.core.ui.R.string.careportal,
                    colorGetter = { iconColors.careportal },
                    content = {
                        CareportalScreen(
                            viewModel = viewModel.careportalViewModel,
                            persistenceLayer = viewModel.persistenceLayer,
                            profileUtil = viewModel.profileUtil,
                            translator = viewModel.translator,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex5) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
            val pageIndex6 = currentIndex++
            add(
                TreatmentTab(
                    icon = RunningMode,
                    titleRes = app.aaps.core.ui.R.string.running_mode,
                    colorGetter = { iconColors.runningMode },
                    content = {
                        RunningModeScreen(
                            viewModel = viewModel.runningModeViewModel,
                            translator = viewModel.translator,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex6) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
            val pageIndex7 = currentIndex++
            add(
                TreatmentTab(
                    icon = UserEntry,
                    titleRes = R.string.user_entry,
                    colorGetter = { iconColors.userEntry },
                    content = {
                        UserEntryScreen(
                            viewModel = viewModel.userEntryViewModel,
                            userEntryPresentationHelper = viewModel.userEntryPresentationHelper,
                            translator = viewModel.translator,
                            importExportPrefs = viewModel.importExportPrefs,
                            uel = viewModel.uel,
                            setToolbarConfig = { config ->
                                if (allowedToolbarPage == pageIndex7) toolbarConfig = config
                            },
                            onNavigateBack = onNavigateBack
                        )
                    }
                )
            )
        }
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    // Force toolbar update when page changes and settles
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            // Update which page is allowed to set toolbar
            allowedToolbarPage = pagerState.currentPage
            // Force the page to update its toolbar by triggering a composition
            // (incrementing this will cause screens to see a new key and recompose)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(toolbarConfig.title) },
                navigationIcon = { toolbarConfig.navigationIcon() },
                actions = { toolbarConfig.actions(this) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes),
                                tint = tab.colorGetter(),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(stringResource(tab.titleRes))
                        }
                    )
                }
            }

            // Pager with treatment screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 0  // Only compose the current page
            ) { page ->
                // Force recomposition when allowedToolbarPage changes
                androidx.compose.runtime.key(allowedToolbarPage) {
                    tabs[page].content()
                }
            }
        }
    }
}

/**
 * Data class representing a treatment tab.
 *
 * @param icon The ImageVector icon for the tab
 * @param titleRes The string resource ID for the tab title
 * @param colorGetter Lambda function that returns the color for the tab icon from theme
 * @param content Composable content to display when this tab is selected
 */
private data class TreatmentTab(
    val icon: ImageVector,
    val titleRes: Int,
    val colorGetter: () -> Color,
    val content: @Composable () -> Unit
)
