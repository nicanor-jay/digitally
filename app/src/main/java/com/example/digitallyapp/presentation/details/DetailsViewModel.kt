package com.example.digitallyapp.presentation.details

import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.math.MathUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.Counter
import com.example.digitallyapp.data.Note
import com.example.digitallyapp.data.preferences.PreferencesManager
import com.example.digitallyapp.data.repository.CountersRepository
import com.example.digitallyapp.ui.theme.DefaultPurpleGradient
import com.example.digitallyapp.ui.theme.EmptyGray
import com.example.digitallyapp.ui.theme.SurpassedTarget
import com.example.digitallyapp.utils.DateComponent
import com.example.digitallyapp.utils.DateFormats
import com.example.digitallyapp.utils.ResetFrequency
import com.example.digitallyapp.utils.formatDate
import com.example.digitallyapp.utils.getDateComponent
import com.example.digitallyapp.utils.getDateFormatString
import com.example.digitallyapp.utils.getMonthName
import com.example.digitallyapp.utils.isDifferentWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.round

class DetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val countersRepository: CountersRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val counterId: Int =
        checkNotNull(savedStateHandle[DetailsDestination.CounterIdArg])


    private val _counterDetailsUiState = MutableStateFlow(DetailsUiState())
    val counterDetailsUiState: StateFlow<DetailsUiState> = _counterDetailsUiState

    private val _scrollState = MutableStateFlow<LazyGridState?>(null)
    val scrollState: StateFlow<LazyGridState?> = _scrollState

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    private val selectedIndex: StateFlow<Int?> = _selectedIndex

    // Mutable state variable to hold the dynamic theme color
    private val _dynamicThemeColor = MutableStateFlow<Color?>(null)

    private var isDynamicColorEnabled: Boolean = false
    private var dateFormatPreference: String = getDateFormatString(DateFormats.DAY_MONTH_YEAR)


    init {
        Log.d("CounterDetailsViewModel", "INIT BLOCK")
        viewModelScope.launch {
            isDynamicColorEnabled = preferencesManager.dynamicColorPreferenceFlow.first()
            dateFormatPreference = preferencesManager.dateFormatPreference.first()
            Log.d("CounterDetailsViewModel", "isDynamicColorEnabled : $isDynamicColorEnabled")
        }

        viewModelScope.launch {
            _counterDetailsUiState.value = DetailsUiState(loading = true)
            combine(
                countersRepository.getCounterFlowById(counterId.toLong()),
                countersRepository.getCounterHistoryByIdFlow(
                    counterId.toLong(),
                    dateFormatPreference
                ),
                countersRepository.getAllNotesByCounterId(counterId.toLong())
            ) { counter, history, notes ->
                if (counter != null && history.isNotEmpty()) {
                    val processedHistory = withContext(Dispatchers.Default) {
                        processHistoricDetailsList(
                            history,
                            notes,
                            counter.resetFrequency,
                        )
                    }

                    val overviewStats = withContext(Dispatchers.Default) {
                        processOverviewValues(
                            processedHistory,
                            counter.resetFrequency,
                            counter.target != null
                        )
                    }

                    val selectedIndex = selectedIndex.value ?: processedHistory.lastIndex

                    val currentNote = countersRepository.getNoteByCounterAndDate(
                        counterId = counterId.toLong(),
                        date = processedHistory.getOrNull(selectedIndex)!!.dateTime
                    ) ?: Note(
                        counterId = counterId.toLong(),
                        dateTime = processedHistory[selectedIndex].dateTime
                    )

                    val noteUiState = NoteUiState(
                        currentNote = currentNote,
                        isAddingOrEditingNote = currentNote.text == null
                    )

                    initialiseEntryUiState(
                        counterId,
                        processedHistory[selectedIndex].dateTime,
                        processedHistory[selectedIndex].count,
                        processedHistory[selectedIndex].target
                    )

                    DetailsUiState(
                        counter = counter,
                        overviewStats = overviewStats,
                        counterHistoryDetails = processedHistory,
                        loading = false,
                        dateFormatPreference = dateFormatPreference,
                        selectedIndex = selectedIndex,
                        noteUiState = noteUiState
                    )
                } else {
                    // Counter is null, return a default state
                    DetailsUiState(loading = false)
                }
            }.collect { uiState ->
                _counterDetailsUiState.value = uiState
            }

            Log.d(
                "DetailsViewModel",
                " ${counterDetailsUiState.value.counterHistoryDetails}"
            )
        }

        viewModelScope.launch {
            selectedIndex.collect { index ->
                if (index == null) {
                    return@collect
                }
                Log.d("DetailsViewModel", "New selected index: $index")
                Log.d(
                    "DetailsViewModel",
                    counterDetailsUiState.value.counterHistoryDetails[index].label.toString()
                )
                // Update ui state with new selected index and current note
                _counterDetailsUiState.value =
                    counterDetailsUiState.value.copy(
                        selectedIndex = index
                    )
                fetchAndUpdateCurrentNote()

                initialiseEntryUiState(
                    counterId,
                    counterDetailsUiState.value.counterHistoryDetails[index].dateTime,
                    counterDetailsUiState.value.counterHistoryDetails[index].count,
                    counterDetailsUiState.value.counterHistoryDetails[index].target
                )

            }
        }
    }

    private fun initialiseEntryUiState(counterId: Int, date: Long, count: Int, target: Int?) {
        Log.d("CounterDetailsViewModel", "counterId: $counterId")
        Log.d("CounterDetailsViewModel", "date: $date")
        Log.d("CounterDetailsViewModel", "count: $count")
        Log.d("CounterDetailsViewModel", "target: $target")
        viewModelScope.launch {
            val existingCountEntry =
                countersRepository.getCountEntryByCounterIdAndDate(counterId.toLong(), date)
            val initCount = existingCountEntry?.editedCount ?: existingCountEntry?.count ?: count

            _counterDetailsUiState.value = counterDetailsUiState.value.copy(
                entryUiState = EntryUiState(
                    entryDetails = EntryDetails(
                        count = initCount
                    ),
                    countEntryExists = existingCountEntry != null,
                    originalCount = existingCountEntry?.count ?: initCount,
                    counterId = counterId,
                    date = date,
                    count = count,
                    target = target ?: -1
                )
            )
        }
    }

    fun updateDynamicThemeColor(color: Color) {
        _dynamicThemeColor.value = color
    }

    fun toggleDropdownMenu() {
        _counterDetailsUiState.value =
            _counterDetailsUiState.value.copy(expanded = !_counterDetailsUiState.value.expanded)
    }

    fun toggleDeleteConfirmation() {
        _counterDetailsUiState.value =
            _counterDetailsUiState.value.copy(deleteConfirmation = !_counterDetailsUiState.value.deleteConfirmation)
    }

    private fun processHistoricDetailsList(
        historicDetails: List<CountEntryWithFormattedDate>,
        notes: List<Note>,
        resetFrequency: ResetFrequency
    ): List<ProcessedHistoricCountEntry> {
        val exhaustiveList = generateHistoricDetailsList(historicDetails, resetFrequency)
        val offset = getOffset(exhaustiveList.first().dateTime)
        Log.d("CounterDetailsViewModel", "Offset Value: $offset")

        val processedList = mutableListOf<ProcessedHistoricCountEntry>()
        var previousMonth = getDateComponent(
            exhaustiveList.firstOrNull()?.formattedDate ?: "",
            dateFormatPreference,
            DateComponent.MONTH
        )

        if (exhaustiveList.isEmpty()) {
            return processedList
        }

        // Enter first month label into list
        val firstDetail = exhaustiveList[0]
        var monthName = getMonthName(firstDetail.formattedDate, dateFormatPreference)
        processedList.add(
            ProcessedHistoricCountEntry(
                label = monthName
            )
        )
        // add empty spacer boxes for initial offset
        repeat(offset) {
            processedList.add(
                ProcessedHistoricCountEntry()
            )
        }

        // Tracking prev count for counters with reset-freqs of none. This grays out days where
        // counter hasnt changedvalue
        var prevCount = 0
        var prevColor: Color = Color.Transparent
        val colorPalette = if (isDynamicColorEnabled) {
            Log.d("CounterDetailsViewModel", "isDynamicColorEnabled iS TRUE")

            val dynamicColor = _dynamicThemeColor.value
                ?: Color(0xFF9E9AA1) // Default color if dynamic color is null
            Log.d("CounterDetailsViewModel", "dynamicColor $dynamicColor")

            generateTints(dynamicColor).reversed()
        } else {
            DefaultPurpleGradient
        }

        exhaustiveList.forEachIndexed { index, detail ->
            if ((index + offset) % 7 == 0 && index > 0) {
                val lastItem = exhaustiveList.getOrNull(index + 7)
                val currentMonth = getDateComponent(
                    lastItem?.formattedDate ?: "PLACEHOLDER/DATE/MISSING",
                    dateFormatPreference,
                    DateComponent.MONTH
                )
                val monthChanged = currentMonth != previousMonth

                if (monthChanged) {
                    monthName =
                        getMonthName(
                            lastItem?.formattedDate ?: "PLACEHOLDER/DATE/MISSING",
                            dateFormatPreference
                        )
                    processedList.add(
                        // Insert Month Header
                        ProcessedHistoricCountEntry(
                            label = monthName
                        )
                    )
                    previousMonth = currentMonth
                } else {
                    processedList.add(
                        // Empty Spacer CountEntry for "header" row
                        ProcessedHistoricCountEntry(
                        )
                    )
                }
            }

            val processedDetail: ProcessedHistoricCountEntry
            val hasNote = notes.any {
                formatDate(it.dateTime, dateFormatPreference) == formatDate(
                    detail.dateTime,
                    dateFormatPreference
                )
            }
            val countValue = detail.editedCount ?: detail.count
            val color = when {
                resetFrequency == ResetFrequency.DAILY -> determineBackgroundColor(
                    detail, colorPalette
                )

                countValue != prevCount && countValue != 0 -> determineBackgroundColor(
                    detail,
                    colorPalette
                )

                resetFrequency == ResetFrequency.WEEKLY && index > 1 && exhaustiveList.size > 1 && isDifferentWeek(
                    exhaustiveList[index - 1].dateTime,
                    detail.dateTime
                ) -> determineBackgroundColor(detail, colorPalette)

                countValue == 0 && prevCount > 0 && resetFrequency != ResetFrequency.WEEKLY -> Color(
                    0xFF7140c9
                )

                else -> Color(0xFF9E9AA1)
            }
            val borderColor = when {
                resetFrequency == ResetFrequency.WEEKLY && countValue != prevCount && countValue != 0 ->
                    determineBackgroundColor(detail, colorPalette)

                else -> prevColor
            }

            processedDetail = ProcessedHistoricCountEntry(
                originalId = detail.originalId,
                label = detail.formattedDate,
                dateTime = detail.dateTime,
                color = color,
                hasNote = hasNote,
                count = detail.editedCount ?: detail.count,
                target = detail.target,
                isBordered = resetFrequency == ResetFrequency.WEEKLY && prevCount == countValue && countValue != 0,
                borderColor = borderColor
            ).also {
                prevCount = detail.editedCount ?: detail.count
                prevColor = borderColor
            }

            processedList.add(processedDetail)
        }

        return processedList
    }

    private fun generateHistoricDetailsList(
        existingDataList: List<CountEntryWithFormattedDate>,
        resetFrequency: ResetFrequency
    ): List<CountEntryWithFormattedDate> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val detailsList = mutableListOf<CountEntryWithFormattedDate>()

        // Find the earliest and latest dates in existingDataList

        val dateCreated = countersRepository.getCounterById(counterId.toLong())!!.dateCreated

        val latestDate = getEndDate()

        // Create Calendar objects for the earliest and latest dates
        val earliestCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        earliestCalendar.timeInMillis = dateCreated

        val latestCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        latestCalendar.timeInMillis = latestDate

        // Calculate the difference between the earliest and latest dates in months
        var timeDifferenceInMonths = 0
        while (earliestCalendar.before(latestCalendar)) {
            earliestCalendar.add(Calendar.MONTH, 1)
            timeDifferenceInMonths++
        }

        // CounterHistory calendar 'padding'
        if (timeDifferenceInMonths < 2) {
            // If the difference is less than two months, subtract one month from the earliest date as calendar padding
            calendar.timeInMillis = dateCreated
            calendar.add(Calendar.MONTH, -2)
        } else {
            if (existingDataList.firstOrNull()?.dateTime!! < dateCreated) {
                calendar.timeInMillis = existingDataList.firstOrNull()?.dateTime!!
            } else {
                calendar.timeInMillis = dateCreated
            }
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        var prevValue: Int? = null
        var prevTarget: Int? = null

        while (calendar.timeInMillis < getEndDate()) {
            val dateTime = calendar.timeInMillis
            val formattedDate = formatDate(calendar.timeInMillis, dateFormatPreference)

            val existingData = existingDataList.find { it.formattedDate == formattedDate }

            if (existingData != null) {
                val editedCount = existingData.editedCount
                prevValue = when {
                    editedCount != null -> editedCount
                    existingData.count != 0 -> existingData.count
                    else -> prevValue
                }
                prevTarget = existingData.target
                detailsList.add(existingData)
            } else {
                val count = when (resetFrequency) {
                    ResetFrequency.NONE -> prevValue ?: 0
                    ResetFrequency.DAILY -> 0 // Reset the count to 0 on a new day for DAILY resetFrequency
                    ResetFrequency.WEEKLY -> {
                        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                            prevValue = 0
                            0 // Reset the count to 0 on a new week starting on Monday for WEEKLY resetFrequency
                        } else {
                            prevValue
                                ?: 0 // Carry forward previous value if it exists, otherwise set to 0
                        }
                    }
                }
                val target = prevTarget
                // set originalId as null since it does not originally exist
                val details =
                    CountEntryWithFormattedDate(
                        null,
                        formattedDate,
                        dateTime,
                        count,
                        target,
                        null
                    )
                detailsList.add(details)
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return detailsList
    }


    private fun getOffset(epochTimeMillis: Long): Int {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = epochTimeMillis
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        var offset =
            dayOfWeek - 1 - 1 // Monday = 2 (-2), and one item already in list (-1) so -3 in total

        if (offset < 0) {
            offset += 7 // Wrap around to the correct row
        }

        return offset
    }

    private fun getEndDate(): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun generateTints(color: Color): List<Color> {
        Log.d("CounterDetailsViewModel", "Generating Tints")

        val numColors = 5
        // Setting min/max saturation value (HSV). Reducing saturation causes "fading" out which
        // is what we want
        val minSVal = 0.2f
        val maxSVal = 0.85f

        // Extract individual color components (HSV) for both colors
        val targetLighterColor = FloatArray(3)
        val saturatedInputColor = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), targetLighterColor)
        android.graphics.Color.colorToHSV(color.toArgb(), saturatedInputColor)

        //Setting the input colour saturation to max val, and the target color saturation
        targetLighterColor[1] = minSVal
        saturatedInputColor[1] = maxSVal
        targetLighterColor[2] = 1f

        // Calculate step size for each channel per color
        val sStep = (saturatedInputColor[1] - targetLighterColor[1]) / (numColors)
        val vStep = (saturatedInputColor[2] - targetLighterColor[2]) / (numColors)

        // Generate gradient palette and store in a mutable list
        val palette = mutableListOf<Color>()
        for (i in 0 until numColors) {
            // Interpolate and clamp HSV values for current step
            val s = targetLighterColor[1] + sStep * i
            val v = targetLighterColor[2] + vStep * i
            palette.add(
                Color(
                    android.graphics.Color.HSVToColor(
                        floatArrayOf(
                            targetLighterColor[0],
                            MathUtils.clamp(s, minSVal, maxSVal),
                            MathUtils.clamp(v, 0f, 1f),
                        )
                    )
                )
            )
        }
        Log.d("CounterDetailsViewModel", "Palette Size : ${palette.size}")
        Log.d("CounterDetailsViewModel", "Palette S Step : $sStep")
        Log.d("CounterDetailsViewModel", "Palette V Step : $vStep")

        return palette
    }

    private fun determineBackgroundColor(
        item: CountEntryWithFormattedDate,
        colorsList: List<Color>
    ): Color {
        val count = item.editedCount ?: item.count
        val target = item.target

        return if (target == null) {
            if (count == 0) {
                EmptyGray
            } else {
                colorsList[1]
            }
        } else {

            val result = count.toFloat() / target.toFloat()
            when {
                result == 0.0f -> EmptyGray
                result < 0.25f -> colorsList[4]
                result < 0.5f -> colorsList[3]
                result < 0.75f -> colorsList[2]
                result < 1.0f -> colorsList[1]
                result == 1.0f -> colorsList[0]
//                    else -> Color(0xFF7140c9)
                else -> SurpassedTarget
            }
        }
    }

    private fun processOverviewValues(
        processedHistory: List<ProcessedHistoricCountEntry>,
        resetFrequency: ResetFrequency?,
        hasTarget: Boolean
    ): List<OverviewStats> {
        val overviewStatsList = mutableListOf<OverviewStats>()

        resetFrequency?.let { frequency ->
            when (frequency) {
                ResetFrequency.NONE -> {
                    val lastCount = processedHistory.lastOrNull()?.count ?: 0
                    overviewStatsList.add(OverviewStats("$lastCount", "Total Count"))

                    if (hasTarget) {
                        val target = processedHistory.lastOrNull()?.target ?: 0
                        val percentage =
                            if (target != 0) (lastCount.toFloat() / target.toFloat()) * 100 else 0f
                        overviewStatsList.add(
                            OverviewStats(
                                "${round(percentage).toInt()}%",
                                "% of target"
                            )
                        )
                    } else {
                        // Handle case without target
                    }
                }

                ResetFrequency.DAILY -> {
                    val totalCount = processedHistory.sumOf { it.count }
                    overviewStatsList.add(OverviewStats("$totalCount", "Total Count"))

                    val averageCountPerDay =
                        calculateAverageCountPerDay(processedHistory, totalCount)
                    overviewStatsList.add(
                        OverviewStats(
                            "%.1f".format(averageCountPerDay),
                            "Avg per day"
                        )
                    )

                    if (hasTarget && processedHistory.isNotEmpty() && processedHistory.indexOfFirst { it.count > 0 } > 0) {
                        val longestStreak = calculateDailyLongestStreak(
                            processedHistory,
                            processedHistory.indexOfFirst { it.count > 0 })
                        overviewStatsList.add(OverviewStats("$longestStreak", "Longest Streak"))
                    } else {
                        // Handle case without target
                    }
                }

                ResetFrequency.WEEKLY -> {
                    val totalWeeklyCount = calculateTotalCountWeekly(processedHistory)
                    overviewStatsList.add(OverviewStats("$totalWeeklyCount", "Total Count"))

                    val averageWeeklyCount = calculateAverageCountWeekly(processedHistory)
                    overviewStatsList.add(
                        OverviewStats(
                            "%.1f".format(averageWeeklyCount),
                            "Avg per week"
                        )
                    )

                    if (hasTarget) {
                        val longestWeeklyStreak = calculateWeeklyLongestStreak(processedHistory)
                        overviewStatsList.add(
                            OverviewStats(
                                "$longestWeeklyStreak",
                                "Longest Streak"
                            )
                        )
                    } else {
                        // Handle case without target
                    }
                }
            }
        }
        return overviewStatsList
    }

    private fun calculateAverageCountPerDay(
        processedHistory: List<ProcessedHistoricCountEntry>,
        totalCount: Int
    ): Float {
        val firstNonZeroCount = processedHistory.firstOrNull { it.count != 0 }
        val averageCount = if (firstNonZeroCount != null) {
            var index = processedHistory.indexOf(firstNonZeroCount)
            var daysFromStart = 0

            while (index <= processedHistory.lastIndex) {
                if (processedHistory[index].color != Color.Transparent) {
                    daysFromStart++
                }
                index++
            }

            Log.d("CounterDetailsViewModel", index.toString())
            Log.d("CounterDetailsViewModel", daysFromStart.toString())
            if (daysFromStart > 0) totalCount.toFloat() / daysFromStart else 0f
        } else {
            totalCount.toFloat()
        }

        return averageCount
    }

    private fun calculateDailyLongestStreak(
        processedHistory: List<ProcessedHistoricCountEntry>,
        startIndex: Int
    ): Int {
        var longestStreak = 0
        var currentStreak = 0

        for (i in startIndex until processedHistory.size) {
            val item = processedHistory[i]

            //Skip invisible header boxes so streaks can continue over weeks
            if (item.label != null) {
                if ((item.color != Color.Transparent) && (item.target != null) && (item.count >= item.target)) {
                    // Increase the current streak
                    currentStreak++

                    // Update the longest streak if necessary
                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak
                    }
                } else {
                    // Reset the current streak if the conditions are not met
                    currentStreak = 0
                }
            }
        }

        return longestStreak
    }

    private fun calculateTotalCountWeekly(processedHistory: List<ProcessedHistoricCountEntry>): Int {
        var totalCountWeekly = 0
        var currentIndex = 7 // Start with the 7th index, which corresponds to the first Sunday

        if (currentIndex < processedHistory.size) {
            // Initialize totalCountWeekly with the count from the 7th index (first Sunday)
            totalCountWeekly += processedHistory[currentIndex].count
        }

        while (currentIndex + 8 < processedHistory.size) {
            // Add the count from the next Sunday
            currentIndex += 8
            totalCountWeekly += processedHistory[currentIndex].count
        }

        // Handle the incomplete week (last week)
        if (currentIndex < processedHistory.lastIndex) {
            totalCountWeekly += processedHistory[processedHistory.lastIndex].count
        }

        return totalCountWeekly
    }

    private fun calculateAverageCountWeekly(processedHistory: List<ProcessedHistoricCountEntry>): Double {
        var totalCountWeekly = 0
        var numberOfWeeks = 0
        var currentIndex = 7 // Start with the 7th index, which corresponds to the first Sunday
        var hasStartedCounting = false

        while (currentIndex < processedHistory.size) {
            val count = processedHistory[currentIndex].count
            if (count > 0) {
                totalCountWeekly += count
                numberOfWeeks++
                hasStartedCounting = true
            } else {
                if (hasStartedCounting) {
                    numberOfWeeks++
                }
            }
            currentIndex += 8 // Increment index by 8 for each iteration
        }

        // Handle the incomplete week (last week)
        if (currentIndex > processedHistory.lastIndex) {
            totalCountWeekly += processedHistory[processedHistory.lastIndex].count
            numberOfWeeks++
        }

        Log.d("CounterDetailsViewModel", "Number of weeks: $numberOfWeeks")
        Log.d("CounterDetailsViewModel", "Total Count: $totalCountWeekly")

        // Calculate the average count per week
        return if (numberOfWeeks > 0) {
            totalCountWeekly.toDouble() / numberOfWeeks
        } else {
            0.0
        }
    }

    private fun calculateWeeklyLongestStreak(processedHistory: List<ProcessedHistoricCountEntry>): Int {
        var longestStreak = 0
        var currentStreak = 0
        var currentIndex = 7

        // Find the first valid entry with a target greater than 0
        while (currentIndex < processedHistory.size && processedHistory[currentIndex].target == null) {
            currentIndex += 8 // Move to the next Sunday
        }

        // Calculate streak via every 8th element for majority of data
        while (currentIndex + 8 < processedHistory.size) {
            currentIndex += 8 // Move to the next Sunday
            val target = processedHistory[currentIndex].target

            if (target != null) {
                if (processedHistory[currentIndex].count >= target) {
                    // Increase the current streak if the target is met
                    currentStreak++
                } else {
                    // Reset the current streak as the target was not met
                    currentStreak = 0
                }

                // Update the longest streak if necessary
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
            }
        }

        // Check if the currentIndex exceeds the size of the list, handle incomplete weeks
        if (currentIndex < processedHistory.lastIndex) {
            val lastTarget = processedHistory.last().target
            if (lastTarget != null && processedHistory.last().count >= lastTarget) {
                currentStreak++

                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
            }
        }
        return longestStreak
    }

    //Update Selected Index
    fun updateSelectedIndex(index: Int) {
        _selectedIndex.value = index
    }

    //Fetch & Update UI to show current note, upon changing of selected index
    private fun fetchAndUpdateCurrentNote() {
        val currentState = _counterDetailsUiState.value
        val selectedNoteIndex = currentState.selectedIndex
        val selectedCounterHistoryDetails =
            currentState.counterHistoryDetails.getOrNull(selectedNoteIndex)

        val currentNoteState = _counterDetailsUiState.value.noteUiState

        if (selectedCounterHistoryDetails != null) {
            viewModelScope.launch {
                val note = countersRepository.getNoteByCounterAndDate(
                    counterId = counterId.toLong(),
                    date = selectedCounterHistoryDetails.dateTime
                )
                val updatedNote =
                    note ?: Note(counterId.toLong(), selectedCounterHistoryDetails.dateTime, null)
                val updatedState =
                    currentNoteState.copy(
                        currentNote = updatedNote,
                    )
                _counterDetailsUiState.value =
                    counterDetailsUiState.value.copy(noteUiState = updatedState)

                updateNoteText(note?.text ?: "")
                updateIsAddingOrEditingNote(note?.text == null)

                Log.d("CounterDetailsScreen", "Current Note Text: ${note?.text}")
            }
        }
    }

    fun toggleIsNoteEditDialogOpen() {
        // Toggle visibility of note edit dialog
        val currentNoteUiState = counterDetailsUiState.value.noteUiState
        _counterDetailsUiState.value =
            counterDetailsUiState.value.copy(
                noteUiState = currentNoteUiState.copy(
                    isNoteEditDialogOpen = !currentNoteUiState.isNoteEditDialogOpen
                )
            )
        // Clear editingNoteText
        val updatedNoteUiState = counterDetailsUiState.value.noteUiState
        if (updatedNoteUiState.currentNote?.text != updatedNoteUiState.editingNoteText) {
            _counterDetailsUiState.value =
                counterDetailsUiState.value.copy(
                    noteUiState = updatedNoteUiState.copy(
                        editingNoteText = updatedNoteUiState.currentNote?.text ?: ""
                    ),
                )
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            val currentNoteState = counterDetailsUiState.value.noteUiState
            if (currentNoteState.currentNote!!.text == null) {
                Log.d("CounterDetailsViewModel", "Note Inserted")
                withContext(Dispatchers.IO) {
                    countersRepository.insertNote(
                        currentNoteState.currentNote.copy(
                            text = currentNoteState.editingNoteText
                        )
                    )

                }
            } else if (currentNoteState.editingNoteText != "") {
                Log.d("CounterDetailsViewModel", "Existing Note Updated")
                countersRepository.updateNote(
                    currentNoteState.currentNote.counterId,
                    currentNoteState.currentNote.dateTime,
                    currentNoteState.editingNoteText
                )
            } else {
                Log.d("CounterDetailsViewModel", "Note Deleted")
                countersRepository.deleteNote(
                    currentNoteState.currentNote.counterId,
                    currentNoteState.currentNote.dateTime,
                )
            }
        }
    }

    fun updateNoteText(text: String) {
        val currentNoteState = counterDetailsUiState.value.noteUiState
        _counterDetailsUiState.value = counterDetailsUiState.value.copy(
            noteUiState = currentNoteState.copy(
                editingNoteText = text
            )
        )
    }

    private fun updateIsAddingOrEditingNote(bool: Boolean) {
        val currentNoteState = counterDetailsUiState.value.noteUiState
        _counterDetailsUiState.value = counterDetailsUiState.value.copy(
            noteUiState = currentNoteState.copy(
                isAddingOrEditingNote = bool
            )
        )
    }

    fun resetToOriginalValue() {
        val currentEntryUiState = counterDetailsUiState.value.entryUiState
        _counterDetailsUiState.value = counterDetailsUiState.value.copy(
            entryUiState = currentEntryUiState.copy(
                entryDetails = EntryDetails(currentEntryUiState.originalCount)
            )
        )
    }

    fun updateCountEntry() {
        viewModelScope.launch {
            val currentUiState = counterDetailsUiState.value.entryUiState
            if (currentUiState.countEntryExists) {
                //if original count == 0 and count text field == null/empty, this means that the entry can be deleted.
                if (currentUiState.originalCount == 0 && currentUiState.entryDetails.count == 0
                ) {
                    withContext(Dispatchers.IO) {
                        countersRepository.deleteCountEntryByIdAndDate(
                            counterId.toLong(),
                            currentUiState.date
                        )
                    }

                } else if (currentUiState.originalCount == currentUiState.entryDetails.count) {
                    withContext(Dispatchers.IO) {
                        countersRepository.updateCountEntry(
                            counterId.toLong(),
                            currentUiState.date,
                            null
                        )
                    }
                } else {
                    //update the most recent count entry's "edited count/target" fields
                    withContext(Dispatchers.IO) {
                        countersRepository.updateCountEntry(
                            counterId.toLong(),
                            currentUiState.date,
                            currentUiState.entryDetails.count
                        )
                    }
                }
            } else {
                //insert a count entry, with the new values in "edited count/target" fields
                withContext(Dispatchers.IO) {
                    countersRepository.insertCountEntry(
                        CountEntry(
                            counterId = counterId.toLong(),
                            dateTime = currentUiState.date,
                            count = 0,
                            target = if (currentUiState.target == -1) {
                                null
                            } else {
                                currentUiState.target
                            },
                            editedCount = currentUiState.entryDetails.count
                        )
                    )
                }
            }
        }
    }

    fun toggleIsEntryDialogOpen() {
        val currentEntryUiState = counterDetailsUiState.value.entryUiState
        _counterDetailsUiState.value = counterDetailsUiState.value.copy(
            entryUiState =
            currentEntryUiState.copy(
                isEntryEditDialogOpen = !currentEntryUiState.isEntryEditDialogOpen
            )
        )
        val updatedEntryUiState = _counterDetailsUiState.value.entryUiState
        if (updatedEntryUiState.entryDetails.count != updatedEntryUiState.count) {
            _counterDetailsUiState.value =
                counterDetailsUiState.value.copy(
                    entryUiState = updatedEntryUiState.copy(
                        entryDetails = EntryDetails(updatedEntryUiState.count)
                    ),
                )
        }
    }

    fun updateEntryUiState(entryDetails: EntryDetails) {
        val currentEntryUiState = counterDetailsUiState.value.entryUiState
        _counterDetailsUiState.value = counterDetailsUiState.value.copy(
            entryUiState =
            currentEntryUiState.copy(
                entryDetails = entryDetails,
                countEntryExists = currentEntryUiState.countEntryExists
            )
        )
    }

    fun deleteCounter() {
        viewModelScope.launch {
            countersRepository.deleteCounter(counterDetailsUiState.value.counter!!)
        }
    }
}

data class DetailsUiState(
    val counter: Counter? = null,
    val overviewStats: List<OverviewStats> = emptyList(),
    val dateFormatPreference: String = "dd/MM/yyyy",
    val selectedIndex: Int = -1,
    val loading: Boolean = true,
    val counterHistoryDetails: List<ProcessedHistoricCountEntry> = emptyList(),
    val noteUiState: NoteUiState = NoteUiState(),
    val entryUiState: EntryUiState = EntryUiState(),
    val expanded: Boolean = false,
    val deleteConfirmation: Boolean = false,
)

data class NoteUiState(
    val currentNote: Note? = null,
    val isNoteEditDialogOpen: Boolean = false,
    val isAddingOrEditingNote: Boolean = false,
    val editingNoteText: String = ""
)

data class EntryUiState(
    val entryDetails: EntryDetails = EntryDetails(),
    val countEntryExists: Boolean = false,
    val originalCount: Int? = null,
    val counterId: Int = -1,
    val date: Long = 0,
    val count: Int = 0,
    val target: Int = -1,
    val isEntryEditDialogOpen: Boolean = false
)

data class EntryDetails(
    val count: Int? = 0
)

data class OverviewStats(
    val value: String,
    val text: String
) {
    constructor(average: Double, label: String) : this(
        when {
            average == average.toInt().toDouble() -> average.toInt()
                .toString() // Check if it's a whole number
            else -> "%.2f".format(average) // If not a whole number, format to 2 decimal places
        },
        label
    )
}


data class CountEntryWithFormattedDate(
    val originalId: Int? = null,
    val formattedDate: String,
    val dateTime: Long = 0,
    val count: Int = 0,
    val target: Int? = null,
    val editedCount: Int? = null
)

// Class holding processed count entry for UI
data class ProcessedHistoricCountEntry(
    val originalId: Int? = null,
    val label: String? = null,
    val dateTime: Long = 0,
    val color: Color = Color.Transparent,
    val hasNote: Boolean = false,
    val count: Int = 0,
    val target: Int? = null,
    val isBordered: Boolean = false,
    val borderColor: Color = Color.Transparent
)