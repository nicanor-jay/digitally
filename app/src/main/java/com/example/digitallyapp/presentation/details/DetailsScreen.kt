package com.example.digitallyapp.presentation.details

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitallyapp.AppViewModelProvider
import com.example.digitallyapp.DigitallyTopAppBar
import com.example.digitallyapp.R
import com.example.digitallyapp.navigation.NavigationDestination
import com.example.digitallyapp.presentation.details.composables.CounterDayDetails
import com.example.digitallyapp.presentation.details.composables.CounterHistory
import com.example.digitallyapp.presentation.details.composables.CounterOverview
import com.example.digitallyapp.presentation.details.composables.EditEntryDialog
import com.example.digitallyapp.presentation.details.composables.EditNoteDialog
import com.example.digitallyapp.presentation.home.composables.DeleteConfirmationDialog

object DetailsDestination : NavigationDestination {
    override val route = "Counter_details"
    override val titleRes = R.string.counter_detail_title
    const val CounterIdArg = "CounterId"
    val routeWithArgs = "$route/{$CounterIdArg}"
}

@Composable
fun DetailsScreen(
    navigateToCounterEdit: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.counterDetailsUiState.collectAsState()

    val dynamicThemeColor =
        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary

    viewModel.updateDynamicThemeColor(dynamicThemeColor)
    val coroutineScope = rememberCoroutineScope()

    Log.d("CounterDetailsScreen", "Currently viewed counter's ID: ${uiState.counter?.id}")

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                DigitallyTopAppBar(title = "${uiState.counter?.emojiCon ?: ""} ${uiState.counter?.name ?: ""}".trim(),
                    canNavigateBack = true,
                    navigateUp = navigateBack,
                    actions = {
                        IconButton(onClick = {
                            uiState.counter?.id?.toInt()?.let { navigateToCounterEdit(it) } ?: 0
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_counter_title),
                            )
                        }
                        Box {
                            IconButton(onClick = { viewModel.toggleDropdownMenu() }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Toggle dropdown"
                                )
                            }
                            DropdownMenu(expanded = uiState.expanded,
                                onDismissRequest = {
                                    viewModel.toggleDropdownMenu()
                                }) {
                                DropdownMenuItem(text = { Text("Delete") },
                                    onClick = { viewModel.toggleDeleteConfirmation() })
                            }
                        }
                    })
            }, modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                if (uiState.selectedIndex == -1) {
                    return@Column
                }

                CounterOverview(
                    overviewStats = uiState.overviewStats
                )
                CounterHistory(
                    counterHistory = uiState.counterHistoryDetails,
                    selectedIndex = uiState.selectedIndex,
                    updateSelectedIndex = viewModel::updateSelectedIndex,
                    dateFormatPreference = uiState.dateFormatPreference,
                )
                CounterDayDetails(
                    currentEntry = uiState.counterHistoryDetails[uiState.selectedIndex],
                    currentNote = uiState.noteUiState.currentNote,
                    toggleIsNoteEditDialogOpen = viewModel::toggleIsNoteEditDialogOpen,
                    toggleIsEntryEditDialogOpen = viewModel::toggleIsEntryDialogOpen
                )

                if (uiState.noteUiState.isNoteEditDialogOpen) {
                    EditNoteDialog(
                        viewModel::saveNote,
                        viewModel::toggleIsNoteEditDialogOpen,
                        noteText = uiState.noteUiState.editingNoteText,
                        onValueChange = viewModel::updateNoteText,
                        isAddingOrEditing = uiState.noteUiState.isAddingOrEditingNote
                    )
                }
                if (uiState.entryUiState.isEntryEditDialogOpen) {
                    EditEntryDialog(
                        onEditConfirm = viewModel::updateCountEntry,
                        onEditReset = viewModel::resetToOriginalValue,
                        onEditCancel = viewModel::toggleIsEntryDialogOpen,
                        entryDetails = uiState.entryUiState.entryDetails,
                        updateUiState = viewModel::updateEntryUiState,
                        originalCount = uiState.entryUiState.originalCount
                    )
                }
                if (uiState.deleteConfirmation) {
                    DeleteConfirmationDialog(
                        onDeleteConfirm = {
                            viewModel.toggleDeleteConfirmation()
                            navigateBack()
                            viewModel.toggleDropdownMenu()
                            viewModel.deleteCounter()

                        },
                        onDeleteCancel = { viewModel.toggleDeleteConfirmation() },
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                    )
                }
            }
        }
    }
}

