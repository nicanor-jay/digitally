package com.example.digitallyapp.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitallyapp.AppViewModelProvider
import com.example.digitallyapp.DigitallyTopAppBar
import com.example.digitallyapp.R
import com.example.digitallyapp.navigation.NavigationDestination
import com.example.digitallyapp.presentation.home.composables.CounterItemList
import com.example.digitallyapp.presentation.home.composables.DeleteConfirmationDialog

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    navigateToCounterAdd: () -> Unit,
    navigateToDetails: (Int) -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val homeUiState by viewModel.homeUiState.collectAsState()
    val listState = rememberLazyListState()

    val counterList = homeUiState.counterList
    val userSettingsPreferences = homeUiState.userSettingsPreferences
    val selectionState = homeUiState.selectionState
    val dropDownState = homeUiState.dropdownState

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DigitallyTopAppBar(
                title = if (selectionState.isSelecting) "" else stringResource(
                    HomeDestination.titleRes
                ),
                canNavigateBack = selectionState.isSelecting,
                scrollBehavior = scrollBehavior,
                navigateUp = { viewModel.toggleSelectingMode(false) },
                actions = {
                    // Default Behaviour
                    if (!selectionState.isSelecting) {
                        Box {
                            IconButton(onClick = { viewModel.toggleDropdownMenu(DropdownMenuType.Menu) }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu button"
                                )
                            }
                            DropdownMenu(expanded = dropDownState.isMenuDropdownExpanded,
                                onDismissRequest = { viewModel.toggleDropdownMenu(DropdownMenuType.Menu) }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.show_archived)) },
                                    onClick = {
                                        viewModel.updateViewArchivedCountersPreference(!userSettingsPreferences.isShowingArchived)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_remove_red_eye_24),
                                            contentDescription = ""
                                        )
                                    },
                                    trailingIcon = {
                                        Checkbox(checked = userSettingsPreferences.isShowingArchived,
                                            onCheckedChange = {
                                                viewModel.updateViewArchivedCountersPreference(it)
                                            })
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by)) },
                                    onClick = {
                                        viewModel.toggleDropdownMenu(DropdownMenuType.Menu)
                                        viewModel.toggleDropdownMenu(DropdownMenuType.Sort)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_sort_24),
                                            contentDescription = ""
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                                    }
                                )
                            }
                            DropdownMenu(expanded = dropDownState.isSortDropdownExpanded,
                                onDismissRequest = {
                                    viewModel.toggleDropdownMenu(DropdownMenuType.Sort)
                                }) {
                                SortDropdownValues.entries.forEach { enumValue ->
                                    DropdownMenuItem(
                                        text = { Text(getStringFromSortDropdownValue(enumValue)) },
                                        onClick = {
                                            viewModel.updateSortSelectionPreference(enumValue)
                                        },
                                        trailingIcon = {
                                            if (userSettingsPreferences.selectedSortOption == enumValue) {
                                                if (userSettingsPreferences.sortOrderDirection) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.baseline_arrow_downward_24),
                                                        contentDescription = stringResource(R.string.ascending_sort_icon)
                                                    )
                                                } else {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.baseline_arrow_upward_24),
                                                        contentDescription = stringResource(R.string.ascending_sort_icon)
                                                    )
                                                }
                                            }
                                        }

                                    )
                                }
                            }
                        }
                        IconButton(onClick = {
                            navigateToSettings()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    } else {
                        // In Selecting mode behaviour

                        Box {
                            IconButton(onClick = { viewModel.toggleDropdownMenu(DropdownMenuType.Selected) }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Toggle dropdown"
                                )
                            }
                            DropdownMenu(expanded = selectionState.isSelectedDropdownExpanded,
                                onDismissRequest = { viewModel.toggleDropdownMenu(DropdownMenuType.Selected) }) {

                                if (selectionState.isAllSameArchiveType && selectionState.selectedCounters.isNotEmpty()) {
                                    when (selectionState.selectedCounters.first().isArchived) {
                                        true -> DropdownMenuItem(text = { Text("Unarchive") },
                                            onClick = {
                                                viewModel.toggleArchivedCounters(
                                                    ArchiveAction.Unarchive
                                                )
                                            })

                                        false -> DropdownMenuItem(text = { Text("Archive") },
                                            onClick = {
                                                viewModel.toggleArchivedCounters(
                                                    ArchiveAction.Archive
                                                )
                                            })
                                    }
                                }
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = { viewModel.toggleDeleteConfirmation() })

                            }
                        }
                    }

                }
            )
        },
        floatingActionButton = {
            if (!selectionState.isSelecting) {
                AnimatedVisibility(
                    visible = listState.isScrollingUp(),
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    LargeFloatingActionButton(
                        onClick = {
                            navigateToCounterAdd()
                        },
                        shape = CircleShape,
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            stringResource(R.string.counter_entry_title),
                            modifier = Modifier.size(
                                36.dp
                            )
                        )

                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            CounterItemList(
                listState,
                counterList,
                { item -> viewModel.incrementCounter(item) },
                { item -> viewModel.decrementCounter(item) },
                navigateToDetails,
                viewModel::toggleSelectedItem,
                selectionState,
                userSettingsPreferences
            )
        }
        if (homeUiState.selectionState.deleteConfirmation) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    viewModel.toggleDeleteConfirmation()
                    viewModel.deleteCounters(selectionState.selectedCounters)
                },
                onDeleteCancel = { viewModel.toggleDeleteConfirmation() },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}