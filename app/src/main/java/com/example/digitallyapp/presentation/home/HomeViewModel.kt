package com.example.digitallyapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.preferences.PreferencesManager
import com.example.digitallyapp.data.repository.CountersRepository
import com.example.digitallyapp.utils.ResetFrequency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

class HomeViewModel(
    private val preferencesManager: PreferencesManager,
    private val countersRepository: CountersRepository
) : ViewModel() {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState

    init {
        viewModelScope.launch {
            countersRepository.getAllCountersWithCurrentCountStream().collect { counters ->
                _homeUiState.value = homeUiState.value.copy(counterList = counters)
            }
        }
        viewModelScope.launch {
            // Collect the view archived preference
            preferencesManager.viewArchivedCountersPreferenceFlow.collect { viewArchived ->
                // Update the HomeUiState when view archived counters preference changes
                _homeUiState.value = _homeUiState.value.copy(
                    userSettingsPreferences = _homeUiState.value.userSettingsPreferences.copy(
                        isShowingArchived = viewArchived
                    )
                )
                Log.d("HomeViewModel", "viewArchivedCountersPreference $viewArchived")
            }
        }
        viewModelScope.launch {
            preferencesManager.sortTypePreferenceFlow.collect { selectedSort ->
                _homeUiState.value = _homeUiState.value.copy(
                    userSettingsPreferences = _homeUiState.value.userSettingsPreferences.copy(
                        selectedSortOption = getSortDropdownValueFromString(selectedSort)
                            ?: SortDropdownValues.DateCreated
                    )
                )
                Log.d("HomeViewModel", "sortTypePreferenceFlow $selectedSort")
            }
        }
        viewModelScope.launch {
            preferencesManager.sortOrderPreferenceFlow.collect { sortOrderBool ->
                _homeUiState.value = _homeUiState.value.copy(
                    userSettingsPreferences = _homeUiState.value.userSettingsPreferences.copy(
                        sortOrderDirection = sortOrderBool
                    )
                )
                Log.d("HomeViewModel", "sortOrderPreferenceFlow $sortOrderBool")
            }
        }
        viewModelScope.launch {
            preferencesManager.showTargetPreferencesFlow.collect { showTargetBool ->
                _homeUiState.value = _homeUiState.value.copy(
                    userSettingsPreferences = _homeUiState.value.userSettingsPreferences.copy(
                        showTargets = showTargetBool
                    )
                )
                Log.d("HomeViewModel", "showTargetPreferencesFlow $showTargetBool")
            }
        }
        viewModelScope.launch {
            preferencesManager.confettiPreference.collect { confettiBool ->
                _homeUiState.value = _homeUiState.value.copy(
                    userSettingsPreferences = _homeUiState.value.userSettingsPreferences.copy(
                        showConfetti = confettiBool
                    )
                )
                Log.d("HomeViewModel", "confettiPreference $confettiBool")
            }

        }
    }

    fun incrementCounter(counterWithCurrentCount: CounterWithCurrentCount) {
        Log.d("HomeViewModel", "Counter Increased")
        val updatedCountEntry =
            counterWithCurrentCount.copy(currentCount = counterWithCurrentCount.currentCount + 1)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                countersRepository.updateCountEntry(
                    CountEntry(
                        id = counterWithCurrentCount.countEntryId!!.toLong(),
                        counterId = counterWithCurrentCount.counterId,
                        count = updatedCountEntry.currentCount,
                        dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis,
                        target = updatedCountEntry.target,
                    )
                )
            }
        }
    }


    fun decrementCounter(counterWithCurrentCount: CounterWithCurrentCount) {
        Log.d("HomeViewModel", "Counter Decreased")
        val updatedCountEntry =
            counterWithCurrentCount.copy(currentCount = counterWithCurrentCount.currentCount - 1)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                countersRepository.updateCountEntry(
                    CountEntry(
                        id = counterWithCurrentCount.countEntryId!!.toLong(),
                        counterId = counterWithCurrentCount.counterId,
                        count = updatedCountEntry.currentCount,
                        dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis,
                        target = updatedCountEntry.target,
                    )
                )
            }
        }
    }

    fun toggleSelectingMode(boolean: Boolean) {
        val currentSelectionState = _homeUiState.value.selectionState
        _homeUiState.value = _homeUiState.value.copy(
            selectionState = when (boolean) {
                false -> SelectionState(false, emptyList()) // Back button pressed
                true -> SelectionState(
                    !currentSelectionState.isSelecting,
                    currentSelectionState.selectedCounters
                ) // Toggle based on current state
            }
        )
    }

    fun toggleSelectedItem(counter: CounterWithCurrentCount) {
        val selectionState = homeUiState.value.selectionState

        val updatedSelectedCounters =
            if (selectionState.selectedCounters.contains(counter)) {
                Log.d("HomeViewModel", "Removed ${counter.counterId} from selected counters")
                selectionState.selectedCounters - counter
            } else {
                Log.d("HomeViewModel", "Added ${counter.counterId} to selected counters")
                selectionState.selectedCounters + counter
            }

        val isAllSameArchiveType = if (updatedSelectedCounters.size > 1) {
            val firstItemArchivedStatus =
                updatedSelectedCounters.firstOrNull()?.isArchived ?: false
            updatedSelectedCounters.all { it.isArchived == firstItemArchivedStatus }
        } else {
            true // If only one item is selected, all items have the same status by default
        }

        _homeUiState.value = _homeUiState.value.copy(
            selectionState = SelectionState(
                selectedCounters = updatedSelectedCounters,
                isSelecting = updatedSelectedCounters.isNotEmpty(),
                isAllSameArchiveType = isAllSameArchiveType

            )
        )

        Log.d("HomeViewModel", "isSelecting: ${_homeUiState.value.selectionState.isSelecting}")
        Log.d(
            "HomeViewModel",
            "Selected counters size: ${_homeUiState.value.selectionState.selectedCounters.size}"
        )
        Log.d(
            "HomeViewModel",
            "Do all share archive status?: ${_homeUiState.value.selectionState.isAllSameArchiveType}"
        )
    }

    fun toggleDropdownMenu(menuType: DropdownMenuType) {
        _homeUiState.value = when (menuType) {
            DropdownMenuType.Selected -> {
                val currentSelectionState = _homeUiState.value.selectionState
                _homeUiState.value.copy(
                    selectionState = currentSelectionState.copy(
                        isSelectedDropdownExpanded = !currentSelectionState.isSelectedDropdownExpanded
                    )
                )
            }

            DropdownMenuType.Menu -> {
                val currentDropdownState = _homeUiState.value.dropdownState
                _homeUiState.value.copy(
                    dropdownState = currentDropdownState.copy(
                        isMenuDropdownExpanded = !currentDropdownState.isMenuDropdownExpanded
                    )
                )
            }

            DropdownMenuType.Sort -> {
                val currentDropdownState = _homeUiState.value.dropdownState
                _homeUiState.value.copy(
                    dropdownState = currentDropdownState.copy(
                        isSortDropdownExpanded = !currentDropdownState.isSortDropdownExpanded
                    )
                )
            }
        }
    }

    fun toggleDeleteConfirmation() {
        val currentSelectionState = _homeUiState.value.selectionState
        _homeUiState.value =
            homeUiState.value.copy(
                selectionState = currentSelectionState.copy(deleteConfirmation = !currentSelectionState.deleteConfirmation),
            )
    }

    fun deleteCounters(countersList: List<CounterWithCurrentCount>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for (counter in countersList) {
                    countersRepository.deleteCounterById(counter.counterId)
                }
            }
        }
        // Reset selected lis
        _homeUiState.value = homeUiState.value.copy(
            selectionState = homeUiState.value.selectionState.copy(
                selectedCounters = emptyList(),
                isSelecting = false,
                isSelectedDropdownExpanded = false,
            ),
        )
    }

    fun toggleArchivedCounters(mode: ArchiveAction) {
        viewModelScope.launch {
            when (mode) {
                ArchiveAction.Archive -> withContext(Dispatchers.IO) {
                    for (counter in _homeUiState.value.selectionState.selectedCounters) {
                        countersRepository.archiveCounterById(counter.counterId, 1)
                    }
                }

                ArchiveAction.Unarchive -> withContext(Dispatchers.IO) {
                    for (counter in _homeUiState.value.selectionState.selectedCounters) {
                        countersRepository.archiveCounterById(counter.counterId, 0)
                    }

                }
            }
            _homeUiState.value =
                homeUiState.value.copy(
                    selectionState = homeUiState.value.selectionState.copy(
                        isSelecting = false,
                        isSelectedDropdownExpanded = false,
                        selectedCounters = emptyList()
                    ),
                )
        }
    }

    fun updateViewArchivedCountersPreference(bool: Boolean) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Bool passed to update archive :$bool")
            preferencesManager.updateViewArchivedCountersPreference(bool)
        }
        _homeUiState.value =
            homeUiState.value.copy(
                userSettingsPreferences = homeUiState.value.userSettingsPreferences.copy(
                    isShowingArchived = bool
                )
            )
    }

    fun updateSortSelectionPreference(value: SortDropdownValues) {
        if (value != homeUiState.value.userSettingsPreferences.selectedSortOption) {
            viewModelScope.launch {
                preferencesManager.updateSortValuePreference(value)
            }
            _homeUiState.value =
                homeUiState.value.copy(
                    userSettingsPreferences = homeUiState.value.userSettingsPreferences.copy(
                        selectedSortOption = value
                    )
                )
        } else {
            updateSortOrderPreference(!homeUiState.value.userSettingsPreferences.sortOrderDirection)
        }
    }

    private fun updateSortOrderPreference(bool: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateSortOrderPreference(bool)
        }
        _homeUiState.value = _homeUiState.value.copy(
            userSettingsPreferences = homeUiState.value.userSettingsPreferences.copy(
                sortOrderDirection = bool
            ),
        )
    }
}

data class HomeUiState(
    val counterList: List<CounterWithCurrentCount> = emptyList(),
    val userSettingsPreferences: UserSettingsPreferences = UserSettingsPreferences(),
    val selectionState: SelectionState = SelectionState(),
    val dropdownState: DropdownState = DropdownState()
)

data class CounterWithCurrentCount(
    val counterId: Long = 0,
    val counterName: String = "",
    val counterEmoji: String = "",
    val resetFrequency: ResetFrequency = ResetFrequency.NONE,
    val target: Int? = 0,
    val countEntryId: Long? = null,
    val currentCount: Int = 0,
    val isArchived: Boolean = false
)

data class UserSettingsPreferences(
    val isShowingArchived: Boolean = false,
    val selectedSortOption: SortDropdownValues = SortDropdownValues.DateCreated,
    val sortOrderDirection: Boolean = true,
    val showTargets: Boolean = true,
    val showConfetti: Boolean = true
)

data class SelectionState(
    val isSelecting: Boolean = false,
    val selectedCounters: List<CounterWithCurrentCount> = emptyList(),
    val isSelectedDropdownExpanded: Boolean = false,
    val isAllSameArchiveType: Boolean = false,
    val deleteConfirmation: Boolean = false
)

data class DropdownState(
    val isMenuDropdownExpanded: Boolean = false,
    val isSortDropdownExpanded: Boolean = false,
)

enum class DropdownMenuType {
    Selected,
    Menu,
    Sort
}

enum class SortDropdownValues {
    DateCreated,
    Name,
    ResetFrequency
}

fun getStringFromSortDropdownValue(value: SortDropdownValues): String {
    return when (value) {
        SortDropdownValues.DateCreated -> "Date created"
        SortDropdownValues.Name -> "Name"
        SortDropdownValues.ResetFrequency -> "Reset frequency"
    }
}

fun getSortDropdownValueFromString(value: String): SortDropdownValues? {
    return when (value) {
        "Date created" -> SortDropdownValues.DateCreated
        "Name" -> SortDropdownValues.Name
        "Reset frequency" -> SortDropdownValues.ResetFrequency
        else -> null
    }
}

enum class ArchiveAction {
    Archive,
    Unarchive
}