package com.example.digitallyapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class DateFormats {
    DAY_MONTH_YEAR,
    YEAR_MONTH_DAY,
    MONTH_DAY_YEAR
}

enum class DateComponent {
    DAY, MONTH, YEAR
}

fun getDateFormatString(format: DateFormats): String {
    return when (format) {
        DateFormats.DAY_MONTH_YEAR -> "dd/MM/yyyy"
        DateFormats.YEAR_MONTH_DAY -> "yyyy/MM/dd"
        DateFormats.MONTH_DAY_YEAR -> "MM/dd/yyyy"
    }
}

fun getDateFormatEnum(dateString: String): DateFormats {
    return when (dateString) {
        "dd/MM/yyyy" -> DateFormats.DAY_MONTH_YEAR
        "yyyy/MM/dd" -> DateFormats.YEAR_MONTH_DAY
        "MM/dd/yyyy" -> DateFormats.MONTH_DAY_YEAR
        // if else, return default value
        else -> DateFormats.DAY_MONTH_YEAR
    }
}

fun getComponentIndex(dateFormat: DateFormats, component: DateComponent): Int {
    return when (dateFormat) {
        DateFormats.DAY_MONTH_YEAR -> when (component) {
            DateComponent.DAY -> 0
            DateComponent.MONTH -> 1
            DateComponent.YEAR -> 2
        }

        DateFormats.YEAR_MONTH_DAY -> when (component) {
            DateComponent.DAY -> 2
            DateComponent.MONTH -> 1
            DateComponent.YEAR -> 0
        }

        DateFormats.MONTH_DAY_YEAR -> when (component) {
            DateComponent.DAY -> 1
            DateComponent.MONTH -> 0
            DateComponent.YEAR -> 2
        }
    }
}

fun getDateComponent(
    dateString: String,
    dateFormat: String,
    component: DateComponent
): String {
    if ("/" !in dateString) {
        return "Error"
    }

    val parts = dateString.split("/")
    val index = getComponentIndex(getDateFormatEnum(dateFormat), component)
    return parts[index]
}

fun getMonthName(date: String, dateFormatPreference: String): String {
    val monthIndex =
        getDateComponent(date, dateFormatPreference, DateComponent.MONTH).toIntOrNull()
    return monthIndex?.let { Month.fromIndex(it)?.fullName } ?: ""
}

fun formatDate(date: Long, pattern: String): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = date
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(calendar.time)
}

enum class Month(val fullName: String) {
    JANUARY("January"),
    FEBRUARY("February"),
    MARCH("March"),
    APRIL("April"),
    MAY("May"),
    JUNE("June"),
    JULY("July"),
    AUGUST("August"),
    SEPTEMBER("September"),
    OCTOBER("October"),
    NOVEMBER("November"),
    DECEMBER("December");

    companion object {
        fun fromIndex(index: Int): Month? {
            return entries.getOrNull(index - 1)
        }
    }
}