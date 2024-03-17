package com.example.digitallyapp.utils

import java.util.Calendar

fun isDifferentWeek(timestamp1: Long, timestamp2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply {
        timeInMillis = timestamp1
        firstDayOfWeek = Calendar.MONDAY
    }
    val calendar2 = Calendar.getInstance().apply {
        timeInMillis = timestamp2
        firstDayOfWeek = Calendar.MONDAY
    }

    val year1 = calendar1.get(Calendar.YEAR)
    val year2 = calendar2.get(Calendar.YEAR)

    val week1 = calendar1.get(Calendar.WEEK_OF_YEAR)
    val week2 = calendar2.get(Calendar.WEEK_OF_YEAR)

    return year1 != year2 || week1 != week2
}