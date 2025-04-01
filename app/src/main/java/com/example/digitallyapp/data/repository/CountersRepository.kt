package com.example.digitallyapp.data.repository

import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.Counter
import com.example.digitallyapp.data.Note
import com.example.digitallyapp.presentation.details.CountEntryWithFormattedDate
import com.example.digitallyapp.presentation.home.CounterWithCurrentCount
import kotlinx.coroutines.flow.Flow

interface CountersRepository {
    // Counter functions

    fun getAllCountersWithCurrentCountStream(): Flow<List<CounterWithCurrentCount>>

    suspend fun archiveCounterById(id: Long, bool: Int)

    fun getCounterById(id: Long): Counter?

    suspend fun getCounterFlowById(id: Long): Flow<Counter?>

    suspend fun insertCounter(counter: Counter): Long

    suspend fun updateCounter(counter: Counter)

    suspend fun deleteCounter(counter: Counter)

    suspend fun deleteCounterById(id: Long)

    fun getAllCounters(): List<Counter>?

    // Count Entry functions

    suspend fun updateCountEntry(countEntry: CountEntry)

    suspend fun updateCountEntry(itemId: Long, date: Long, editedCount: Int?)
    suspend fun insertCountEntry(countEntry: CountEntry)

    suspend fun deleteCountEntry(countEntry: CountEntry)

    suspend fun deleteCountEntryByIdAndDate(counterId: Long, dateTime: Long)

    fun getMostRecentCountEntry(counterId: Long): CountEntry?

    suspend fun getCountEntryByCounterIdAndDate(counterId: Long, datetime: Long): CountEntry?

    fun cleanupDummyCounts()

    fun getAllWeeklyCountEntries(): List<CountEntry>?

    fun getCounterHistoryByIdFlow(
        counterId: Long,
        dateFormat: String
    ): Flow<List<CountEntryWithFormattedDate>>

    // Notes section
    fun getAllNotesByCounterId(
        counterId: Long
    ): Flow<List<Note>>

    fun insertNote(note: Note): Long

    suspend fun getNoteByCounterAndDate(counterId: Long, date: Long): Note?

    suspend fun updateNote(counterId: Long, date: Long, text: String)
    suspend fun deleteNote(counterId: Long, date: Long)
    suspend fun updateRecentCountEntryTarget(counterId: Long, target: Int?)
}