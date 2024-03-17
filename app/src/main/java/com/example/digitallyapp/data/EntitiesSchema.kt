package com.example.digitallyapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.digitallyapp.utils.ResetFrequency

@Entity(tableName = "counters")
data class Counter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val emojiCon: String? = null,
    val resetFrequency: ResetFrequency = ResetFrequency.NONE,
    val target: Int? = null,
    val dateCreated: Long = 0,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "count_entries",
    foreignKeys = [
        ForeignKey(
            entity = Counter::class,
            parentColumns = ["id"],
            childColumns = ["counterId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["counterId"])]
)
data class CountEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val counterId: Long = -1,
    val count: Int = 0,
    val dateTime: Long = 0,
    val target: Int? = null,
    val editedCount: Int? = null
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Counter::class,
            parentColumns = ["id"],
            childColumns = ["counterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["counterId", "dateTime"]
)
data class Note(
    val counterId: Long = 0,
    val dateTime: Long = 0,
    val text: String? = null
)