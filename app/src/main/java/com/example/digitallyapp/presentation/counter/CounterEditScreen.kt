package com.example.digitallyapp.presentation.counter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitallyapp.AppViewModelProvider
import com.example.digitallyapp.DigitallyTopAppBar
import com.example.digitallyapp.R
import com.example.digitallyapp.navigation.NavigationDestination
import com.example.digitallyapp.presentation.counter.composables.CounterInputForm
import com.example.digitallyapp.ui.theme.DigitallyAppTheme

object CounterEditDestination : NavigationDestination {
    override val route = "Counter_edit_screen"
    override val titleRes = R.string.edit_counter_title
    const val CounterIdArg = "CounterId"
    val routeWithArgs = "$route/{$CounterIdArg}"
}

@Composable
fun CounterEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
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
                title = if (uiState.isAddingNewCounter) stringResource(R.string.add_counter) else stringResource(
                    R.string.edit_counter
                ),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CounterInputForm(
                modifier = Modifier,
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


@Preview(showBackground = true)
@Composable
fun ItemEditScreenPreview() {
    DigitallyAppTheme {
        CounterEditScreen(navigateBack = { /*Do nothing*/ }, onNavigateUp = { /*Do nothing*/ })
    }
}
