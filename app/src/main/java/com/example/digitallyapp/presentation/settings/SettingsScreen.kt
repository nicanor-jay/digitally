package com.example.digitallyapp.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitallyapp.AppViewModelProvider
import com.example.digitallyapp.DigitallyTopAppBar
import com.example.digitallyapp.R
import com.example.digitallyapp.navigation.NavigationDestination
import com.example.digitallyapp.presentation.settings.composables.DateFormatRadioSelectionDialog
import com.example.digitallyapp.presentation.settings.composables.SettingsDialogItem
import com.example.digitallyapp.presentation.settings.composables.SettingsLabel
import com.example.digitallyapp.presentation.settings.composables.SettingsSwitchItem
import com.example.digitallyapp.ui.theme.DigitallyAppTheme
import com.example.digitallyapp.utils.getDateFormatString

object SettingsScreenDestination : NavigationDestination {
    override val route = "Settings"
    override val titleRes = R.string.settings
}

@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    Scaffold(
        modifier = modifier,
        topBar = {
            DigitallyTopAppBar(
                title = stringResource(R.string.settings),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)

        ) {
            SettingsLabel(
                text = stringResource(R.string.general)
            )
            SettingsSwitchItem(
                text = stringResource(R.string.setting_dynamic_colour_label),
                description = stringResource(R.string.setting_dynamic_colour_description),
                switchState = settingsUiState.dynamicColorPreference,
                onSwitchChange = {
                    viewModel.updateDynamicColorPreference(it)
                }
            )
            SettingsDialogItem(
                text = stringResource(R.string.setting_date_format_label),
                description = getDateFormatString(settingsUiState.dateFormatPreference),
                toggleDialog = { viewModel.toggleDialog(SettingsDialogs.DATE_FORMAT_DIALOG) }
            )
//            HorizontalDivider(Modifier.padding(vertical = 16.dp))
//            SettingsLabel(
//                text = "Interface"
//            )
            SettingsSwitchItem(
                text = stringResource(R.string.setting_show_targets_under_count_label),
                description = stringResource(R.string.setting_show_targets_description),
                switchState = settingsUiState.showTargetsPreference,
                onSwitchChange = {
                    viewModel.updateShowTargetPreference(it)
                }
            )
            SettingsSwitchItem(
                text = (stringResource(R.string.setting_confetti_label)),
                description = stringResource(R.string.setting_confetti_description),
                switchState = settingsUiState.showConfettiPreference,
                onSwitchChange = {
                    viewModel.updateConfettiPreference(it)
                }
            )
            if (settingsUiState.isDateFormatDialogShown) {
                DateFormatRadioSelectionDialog(
                    onDismissRequest = { viewModel.toggleDialog(SettingsDialogs.DATE_FORMAT_DIALOG) },
                    selectedFormat = settingsUiState.dateFormatPreference,
                    updateDateFormatPreference = viewModel::updateDateFormatPreference
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DigitallyAppTheme {
        SettingsScreen(navigateBack = {},
            onNavigateUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsLabelPreview() {
    SettingsLabel("Theme")
}

@Preview(showBackground = true)
@Composable
fun SettingsSwitchItemPreview() {
    SettingsSwitchItem(
        "Toggle Dynamic Color Preference",
        description = "Changes the order of days within the history calendar of each counter",
        true,
        {}
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsDialogItemPreview() {
    SettingsDialogItem(
        text = "First day of the week",
        description = "Changes the order of days within the history calendar of each counter",
        toggleDialog = {}
    )
}
