package com.example.digitallyapp.data.local

import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.Counter
import com.example.digitallyapp.data.Note
import com.example.digitallyapp.data.repository.CountersRepository
import com.example.digitallyapp.presentation.details.CountEntryWithFormattedDate
import com.example.digitallyapp.presentation.home.CounterWithCurrentCount
import kotlinx.coroutines.flow.Flow

class OfflineCountersRepository(private val counterDao: CountersDao) : CountersRepository {
    override fun getAllCountersWithCurrentCountStream(): Flow<List<CounterWithCurrentCount>> =
        counterDao.getAllCountersWithCurrentCountStream()

    override suspend fun archiveCounterById(id: Long, bool: Int) =
        counterDao.archiveCounterById(id, bool)

    override fun getCounterById(id: Long): Counter? = counterDao.getCounterById(id)

    override suspend fun getCounterFlowById(id: Long): Flow<Counter?> =
        counterDao.getCounterFlowById(id)

    override suspend fun insertCounter(counter: Counter): Long =
        counterDao.insertCounter(counter)

    override suspend fun updateCounter(counter: Counter) = counterDao.updateCounter(counter)

    override suspend fun deleteCounterById(id: Long) = counterDao.deleteCounterById(id)

    override suspend fun deleteCounter(counter: Counter) = counterDao.deleteCounter(counter)

    override suspend fun deleteCountEntry(countEntry: CountEntry) =
        counterDao.deleteCountEntry(countEntry)

    override suspend fun deleteCountEntryByIdAndDate(counterId: Long, dateTime: Long) =
        counterDao.deleteCountEntryByIdAndDate(counterId, dateTime)

    override suspend fun getCountEntryByCounterIdAndDate(
        counterId: Long,
        datetime: Long
    ): CountEntry =
        counterDao.getCountEntryByCounterIdAndDate(counterId, datetime)

    override suspend fun updateCountEntry(countEntry: CountEntry) =
        counterDao.updateCountEntry(countEntry)

    override suspend fun updateCountEntry(
        itemId: Long,
        date: Long,
        editedCount: Int?,
    ) = counterDao.updateCountEntry(itemId, date, editedCount)

    override suspend fun insertCountEntry(countEntry: CountEntry) =
        counterDao.insertCountEntry(countEntry)

    override fun getAllCounters(): List<Counter>? = counterDao.getAllCounters()
    override fun getMostRecentCountEntry(counterId: Long): CountEntry =
        counterDao.getMostRecentCountEntry(counterId)

    override fun cleanupDummyCounts() {
        counterDao.cleanupCountEntriesNone()
        counterDao.cleanupCountEntriesDaily()
    }

    override fun getAllWeeklyCountEntries(): List<CountEntry>? =
        counterDao.getAllWeeklyCountEntries()

    override fun getCounterHistoryByIdFlow(
        counterId: Long,
        dateFormat: String
    ): Flow<List<CountEntryWithFormattedDate>> =
        counterDao.getCounterHistoryByIdFlow(counterId, dateFormat)

    override fun getAllNotesByCounterId(counterId: Long): Flow<List<Note>> =
        counterDao.getAllNotesByCounterId(counterId)

    override fun insertNote(note: Note): Long = counterDao.insertNote(note)

    override suspend fun getNoteByCounterAndDate(counterId: Long, date: Long): Note? =
        counterDao.getNoteByCounterAndDate(counterId, date)

    override suspend fun updateNote(counterId: Long, date: Long, text: String) =
        counterDao.updateNote(counterId, date, text)

    override suspend fun deleteNote(counterId: Long, date: Long) =
                                                      counterDao.deleteNote(counterId, date)

    override suspend fun updateRecentCountEntryTarget(counterId: Long, target: Int?) =
        counterDao.updateRecentCountEntryTarget(counterId, target)
}