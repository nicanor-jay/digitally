package com.example.digitallyapp.utils

import androidx.room.TypeConverter

enum class ResetFrequency(val label: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly");

    companion object {
        fun fromString(value: String): ResetFrequency {
            return entries.firstOrNull { it.label == value }
                ?: throw IllegalArgumentException("Invalid string value: $value")
        }
    }
}

class Converters {
    @TypeConverter
    fun fromString(value: String?): ResetFrequency? {
        return ResetFrequency.fromString(value.toString())
    }

    @TypeConverter
    fun resetFrequencyToString(resetFrequency: ResetFrequency?): String? {
        return resetFrequency?.label
    }
}