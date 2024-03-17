package com.example.digitallyapp.utils

import java.util.Calendar

fun isDifferentDay(timestamp1: Long, timestamp2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply {
        timeInMillis = timestamp1
    }
    val calendar2 = Calendar.getInstance().apply {
        timeInMillis = timestamp2
    }

    val day1 = calendar1.get(Calendar.DAY_OF_YEAR)
    val day2 = calendar2.get(Calendar.DAY_OF_YEAR)

    return day1 != day2
}