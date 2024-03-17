package com.example.digitallyapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallyapp.data.preferences.PreferencesManager
import com.example.digitallyapp.utils.DateFormats
import com.example.digitallyapp.utils.getDateFormatEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    private val _settingsUiState = MutableStateFlow(SettingsUiState())
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState

    init {
        viewModelScope.launch {
            preferencesManager.dynamicColorPreferenceFlow.collect { pref ->
                _settingsUiState.value = settingsUiState.value.copy(
                    dynamicColorPreference = pref,
                )
            }
        }
        viewModelScope.launch {
            preferencesManager.showTargetPreferencesFlow.collect { pref ->
                _settingsUiState.value = settingsUiState.value.copy(
                    showTargetsPreference = pref,
                )
            }
        }
        viewModelScope.launch {
            preferencesManager.dateFormatPreference.collect { pref ->
                _settingsUiState.value = settingsUiState.value.copy(
                    dateFormatPreference = getDateFormatEnum(pref),
                )
            }
        }

        viewModelScope.launch {
            preferencesManager.confettiPreference.collect { confettiBool ->
                _settingsUiState.value = _settingsUiState.value.copy(
                    showConfettiPreference = confettiBool
                )
            }
        }
    }

    fun updateDynamicColorPreference(useDynamicColor: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDynamicColorPreference(useDynamicColor)
        }
    }

    fun updateShowTargetPreference(showTarget: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateShowTargetPreference(showTarget)
        }
    }

    fun updateDateFormatPreference(dateFormat: DateFormats) {
        viewModelScope.launch {
            preferencesManager.updateDateFormatPreference(dateFormat)
        }
    }

    fun updateConfettiPreference(bool: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateConfettiPreference(bool)
        }
    }

    fun toggleDialog(settingDialog: SettingsDialogs) {
        when (settingDialog) {
            SettingsDialogs.DATE_FORMAT_DIALOG -> _settingsUiState.value =
                settingsUiState.value.copy(
                    isDateFormatDialogShown = !settingsUiState.value.isDateFormatDialogShown,
                )

            SettingsDialogs.FIRST_WEEK_DAY_DIALOG -> _settingsUiState.value =
                settingsUiState.value.copy(
                    isFirstWeekDayDialogShown = !settingsUiState.value.isFirstWeekDayDialogShown,
                )
        }
    }
}

data class SettingsUiState(
    val dynamicColorPreference: Boolean = false,
    val showTargetsPreference: Boolean = false,
    val dateFormatPreference: DateFormats = DateFormats.DAY_MONTH_YEAR,
    val isDateFormatDialogShown: Boolean = false,
    val isFirstWeekDayDialogShown: Boolean = false,
    val showConfettiPreference: Boolean = true
)

enum class SettingsDialogs {
    DATE_FORMAT_DIALOG,
    FIRST_WEEK_DAY_DIALOG
}
