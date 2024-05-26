package com.example.digitallyapp.presentation.counter

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.emoji2.emojipicker.EmojiPickerView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.Counter
import com.example.digitallyapp.data.repository.CountersRepository
import com.example.digitallyapp.utils.ResetFrequency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

class CounterAddEditViewModel(
    savedStateHandle: SavedStateHandle, private val countersRepository: CountersRepository
) : ViewModel() {
    private val counterId: Int = savedStateHandle[CounterEditDestination.CounterIdArg] ?: -1

    private val _counterUiState = MutableStateFlow(CounterUiState())
    val counterUiState: StateFlow<CounterUiState> = _counterUiState

    private val userAction by mutableStateOf(if (counterId == -1) UserAction.NEW_COUNTER else UserAction.EDITING)

    val dropdownOptions = ResetFrequency.entries.map { it.label }

    private var existingCounter = Counter()

    init {

        viewModelScope.launch {
            if (userAction == UserAction.EDITING) {
                Log.d("CounterEditViewModel", counterId.toString())
                Log.d("CounterEditViewModel", "Updating Existing Counter")
                existingCounter = withContext(Dispatchers.IO) {
                    countersRepository.getCounterById(counterId.toLong()) ?: existingCounter
                }
                _counterUiState.value =
                    counterUiState.value.copy(
                        counterDetails = existingCounter.toEntryFormCounterDetails(),
                        isAddingNewCounter = false
                    )

            } else {
                Log.d("CounterAddEditViewModel", "New Counter with ID: ${counterId.toString()}")

                _counterUiState.value =
                    counterUiState.value.copy(
                        isAddingNewCounter = true
                    )
            }
        }
    }

    fun onDropdownItemSelected(resetFreq: String) {
        _counterUiState.value =
            counterUiState.value.copy(
                counterDetails = counterUiState.value.counterDetails.copy(
                    resetFrequency = resetFreq
                )
            )
    }

    private fun validateInput(uiState: EntryFormCounterDetails = counterUiState.value.counterDetails): Boolean {
        Log.d("NewCounterViewModel.kt emoji length", uiState.emojiCon.length.toString())
        return with(uiState) {
            name.isNotBlank() || target != uiState.target || resetFrequency != uiState.resetFrequency || emojiCon !=
                    uiState.emojiCon
        }
    }

    fun toggleIsDropdownOpen() {
        // Only toggle dropdown if adding a new counter
        if (counterUiState.value.isAddingNewCounter) {
            _counterUiState.value =
                counterUiState.value.copy(isDropdownOpen = !counterUiState.value.isDropdownOpen)
            Log.d("NewCounterViewModel", "Dropdown Visibility Toggled")
        }
    }

    fun toggleEmojiPicker() {
        _counterUiState.value = counterUiState.value.copy(
            isEmojiPickerShown = !counterUiState.value.isEmojiPickerShown
        )
    }

    fun initializeEmojiPicker(context: Context) {
        // Create or retrieve EmojiPickerView when toggling the visibility
        if (counterUiState.value.emojiPickerView == null) {
            Log.d("CounterAddEditViewModel.kt", "Initializing EmojiPicker")
            viewModelScope.launch {
                _counterUiState.value =
                    counterUiState.value.copy(emojiPickerView = EmojiPickerView(context).apply {
                        setOnEmojiPickedListener { item ->
                            updateUiState(counterUiState.value.counterDetails.copy(emojiCon = item.emoji))
                        }
                    })

            }
        } else {
            Log.d("CounterAddEditViewModel.kt", "Retrieving EmojiPicker")
        }
    }

    fun updateUiState(entryFormCounterDetails: EntryFormCounterDetails) {
        _counterUiState.value = CounterUiState(
            counterDetails = entryFormCounterDetails,
            isEntryValid = validateInput(entryFormCounterDetails),
            isAddingNewCounter = counterUiState.value.isAddingNewCounter
        )
    }

    fun saveCounter() {
        viewModelScope.launch {
            if (userAction == UserAction.NEW_COUNTER) {
                val dateCreated = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
                withContext(Dispatchers.IO) {
                    val counterDetailsToInsert =
                        counterUiState.value.counterDetails.copy(dateCreated = dateCreated)
                    val insertedId: Long =
                        countersRepository.insertCounter(counterDetailsToInsert.toCounter())
                    //Initialise new counter with first entry
                    countersRepository.insertCountEntry(
                        CountEntry(
                            counterId = insertedId,
                            dateTime = dateCreated,
                            target = counterUiState.value.counterDetails.target
                        )
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    countersRepository.updateCounter(counterUiState.value.counterDetails.toCounter())
                    if (counterUiState.value.counterDetails.target != existingCounter.target) {
                        countersRepository.updateRecentCountEntryTarget(
                            existingCounter.id,
                            counterUiState.value.counterDetails.target
                        )
                    }
                }
            }
        }
    }
}

data class CounterUiState(
    val counterDetails: EntryFormCounterDetails = EntryFormCounterDetails(),
    val isEntryValid: Boolean = false,
    val isDropdownOpen: Boolean = false,
    val isAddingNewCounter: Boolean = false,
    val isEmojiPickerShown: Boolean = false,
    val emojiPickerView: EmojiPickerView? = null
)

data class EntryFormCounterDetails(
    val id: Long = 0,
    val name: String = "",
    val emojiCon: String = "",
    val currentCount: Int = 0,
    val resetFrequency: String = "None",
    val target: Int? = null,
    val dateCreated: Long = 0
)

fun EntryFormCounterDetails.toCounter(): Counter = Counter(
    id = id,
    name = name,
    emojiCon = emojiCon,
    resetFrequency = ResetFrequency.fromString(resetFrequency),
    target = target,
    dateCreated = dateCreated
)

fun Counter.toEntryFormCounterDetails(): EntryFormCounterDetails = EntryFormCounterDetails(
    id = id,
    name = name ?: "",
    emojiCon = emojiCon ?: "",
    resetFrequency = resetFrequency.label,
    target = target,
    dateCreated = dateCreated
)

enum class UserAction {
    EDITING, NEW_COUNTER
}