package com.example.digitallyapp.presentation.counter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitallyapp.AppViewModelProvider
import com.example.digitallyapp.DigitallyTopAppBar
import com.example.digitallyapp.R
import com.example.digitallyapp.navigation.NavigationDestination
import com.example.digitallyapp.presentation.counter.composables.CounterInputForm

object CounterAddDestination : NavigationDestination {
    override val route = "Counter_Add_screen"
    override val titleRes = R.string.edit_counter_title
}

@Composable
fun CounterAddScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: CounterAddEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.counterUiState.collectAsState()

    LaunchedEffect(uiState.emojiPickerView) {
        viewModel.initializeEmojiPicker(context)
    }

    Scaffold(
        topBar = {
            DigitallyTopAppBar(
                title = if (uiState.isAddingNewCounter) "Add Counter" else "Edit Counter",
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                backOrExit = true
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
            CounterInputForm(
                modifier = androidx.compose.ui.Modifier,
                counterDetails = uiState.counterDetails,
                onValueChange = viewModel::updateUiState,
                isDropdownOpen = uiState.isDropdownOpen,
                toggleDropDown = viewModel::toggleIsDropdownOpen,
                toggleEmojiPicker = viewModel::toggleEmojiPicker,
                dropdownOptions = viewModel.dropdownOptions,
                onDropdownItemSelected = viewModel::onDropdownItemSelected,
                isAddingNewCounter = uiState.isAddingNewCounter,
                onClick = {
                    viewModel.saveCounter()
                    navigateBack()

                },
                saveEnabled = uiState.isEntryValid,
                showEmojiPicker = uiState.isEmojiPickerShown,
                emojiPickerView = uiState.emojiPickerView
            )
        }
    }
}