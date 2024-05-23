package com.example.digitallyapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.Counter
import com.example.digitallyapp.data.Note
import com.example.digitallyapp.presentation.details.CountEntryWithFormattedDate
import com.example.digitallyapp.presentation.home.CounterWithCurrentCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CountersDao {

    // Counter funtions
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCounter(counter: Counter): Long

    @Update
    suspend fun updateCounter(counter: Counter)

    @Delete
    suspend fun deleteCounter(counter: Counter)

    @Query(" DELETE FROM counters WHERE counters.id =:id")
    suspend fun deleteCounterById(id: Long)

    @Query(
        """
        SELECT c.id AS counterId,
       c.name AS counterName,
       c.emojiCon AS counterEmoji,
       c.resetFrequency,
       c.target,
       ce.id AS countEntryId,
       ce.count AS currentCount,
       c.isArchived
    FROM counters c
    INNER JOIN (
        SELECT counterId, MAX(dateTime) AS maxDateTime
        FROM count_entries
        GROUP BY counterId
    ) AS latest_entry
    ON c.id = latest_entry.counterId
    LEFT JOIN count_entries ce
    ON c.id = ce.counterId AND ce.dateTime = latest_entry.maxDateTime
    ORDER BY c.name ASC
    """
    )
    fun getAllCountersWithCurrentCountStream(): Flow<List<CounterWithCurrentCount>>

    @Query(
        """
        SELECT * FROM counters
    """
    )
    fun getAllCounters(): List<Counter>?


    @Query("SELECT * from counters WHERE id = :id")
    fun getCounterById(id: Long): Counter?

    @Query("SELECT * from counters WHERE id = :id")
    fun getCounterFlowById(id: Long): Flow<Counter?>

    @Query("UPDATE counters SET isArchived = :bool WHERE id = :counterId")
    suspend fun archiveCounterById(counterId: Long, bool: Int)

    // Count Entry Functions

    @Delete
    suspend fun deleteCountEntry(countEntry: CountEntry)

    @Query("DELETE FROM count_entries WHERE counterId = :counterId AND dateTime = :dateTime")
    fun deleteCountEntryByIdAndDate(counterId: Long, dateTime: Long)

    @Update
    suspend fun updateCountEntry(countEntry: CountEntry)

    @Query(
        """
    UPDATE count_entries 
    SET editedCount = :editedCount
    WHERE counterId = :itemId 
    AND strftime('%Y-%m-%d', dateTime / 1000, 'unixepoch') = strftime('%Y-%m-%d', :date / 1000, 'unixepoch')
    """
    )
    suspend fun updateCountEntry(
        itemId: Long,
        date: Long,
        editedCount: Int?
    )

    @Insert
    suspend fun insertCountEntry(countEntry: CountEntry)

    @Query("SELECT * FROM count_entries WHERE counterId =:counterId AND dateTime = :datetime")
    suspend fun getCountEntryByCounterIdAndDate(counterId: Long, datetime: Long): CountEntry

    @Query(
        """
        SELECT c.* 
        FROM count_entries c 
        WHERE c.counterId = :counterId 
        ORDER BY dateTime 
        DESC LIMIT 1
    """
    )
    fun getMostRecentCountEntry(counterId: Long): CountEntry

    @Query(
        """
        SELECT ce.*
        FROM count_entries ce
        JOIN counters c ON ce.counterId = c.id 
        WHERE c.resetFrequency = 'Weekly'
        ORDER BY ce.counterId, ce.dateTime;
    """
    )
    fun getAllWeeklyCountEntries(): List<CountEntry>?

    @Query(
        """
    DELETE FROM count_entries
    WHERE id IN (
        SELECT ce1.id
        FROM count_entries ce1
        JOIN counters c ON ce1.counterId = c.id
        JOIN count_entries ce2 ON ce1.counterId = ce2.counterId 
                              AND ce1.id > ce2.id
                              AND (
                                    (ce1.dateTime = ce2.dateTime AND ce1.id - 1 = ce2.id)
                                    OR (ce1.dateTime > ce2.dateTime)
                                  )
        WHERE c.resetFrequency = 'None'
          AND ce1.count = ce2.count 
          AND (ce1.editedCount = ce2.editedCount OR (ce1.editedCount IS NULL AND ce2.editedCount IS NULL))
          AND NOT EXISTS (
              SELECT 1
              FROM count_entries ce3
              WHERE ce3.counterId = ce1.counterId
                AND ce3.id < ce1.id
                AND ce3.id > ce2.id
          )
    )
       """
    )
    fun cleanupCountEntriesNone()

    @Query(
        """
        DELETE FROM count_entries 
        WHERE id IN (
            SELECT ce.id
            FROM count_entries ce
            JOIN counters c ON ce.counterId = c.id
            WHERE c.resetFrequency = 'Daily'
            AND ce.count = 0
            AND (ce.editedCount = 0 OR ce.editedCount IS NULL)
            AND EXISTS (
                SELECT 1
                FROM count_entries ce2
                WHERE ce2.counterId = ce.counterId
                AND ce2.id != ce.id
            )
        );
    """
    )
    fun cleanupCountEntriesDaily()


    @Query(
        """
        SELECT
            CASE
                WHEN :dateFormat = 'MM/dd/yyyy' THEN strftime('%m/%d/%Y', ce.dateTime / 1000, 'unixepoch', 'localtime')
                WHEN :dateFormat = 'yyyy/MM/dd' THEN strftime('%Y/%m/%d', ce.dateTime / 1000, 'unixepoch', 'localtime')
                ELSE strftime('%d/%m/%Y', ce.dateTime / 1000, 'unixepoch', 'localtime') -- Default: dd/MM/yyyy
            END AS formattedDate,
            ce.id AS originalId,
            ce.count AS count,
            ce.dateTime AS dateTime,
            ce.target AS target,
            ce.editedCount AS editedCount
        FROM
            count_entries AS ce
        WHERE
            ce.counterId = :counterId
        ORDER BY
            ce.dateTime ASC;
    """
    )
    fun getCounterHistoryByIdFlow(
        counterId: Long,
        dateFormat: String
    ): Flow<List<CountEntryWithFormattedDate>>

    // Note Functions

    @Insert
    fun insertNote(note: Note): Long

    @Query("SELECT * FROM notes WHERE counterId = :counterId")
    fun getAllNotesByCounterId(counterId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE counterId = :counterId AND strftime('%Y-%m-%d', dateTime/1000, 'unixepoch') = strftime('%Y-%m-%d', :date/1000, 'unixepoch')")
    suspend fun getNoteByCounterAndDate(counterId: Long, date: Long): Note?

    @Query("DELETE FROM notes WHERE counterId = :counterId AND strftime('%Y-%m-%d', dateTime/1000, 'unixepoch') = strftime('%Y-%m-%d', :date/1000, 'unixepoch')")
    suspend fun deleteNote(counterId: Long, date: Long)

    @Query("UPDATE notes SET text = :newText WHERE counterId = :counterId AND strftime('%Y-%m-%d', dateTime /1000, 'unixepoch') = strftime('%Y-%m-%d', :date/1000, 'unixepoch')")
    suspend fun updateNote(counterId: Long, date: Long, newText: String)

    @Query(
        """
    UPDATE count_entries 
    SET target = :target
    WHERE counterId = :counterId AND dateTime = (
        SELECT MAX(dateTime) 
        FROM count_entries 
        WHERE counterId = :counterId
    )
"""
    )
    fun updateRecentCountEntryTarget(counterId: Long, target: Int?)

}