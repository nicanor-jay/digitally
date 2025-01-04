package com.example.digitallyapp.utils

import java.util.Calendar

fun isDifferentDay(timestamp1: Long, timestamp2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }

    val year1 = calendar1.get(Calendar.YEAR)
    val month1 = calendar1.get(Calendar.MONTH)
    val day1 = calendar1.get(Calendar.DAY_OF_MONTH)

    val year2 = calendar2.get(Calendar.YEAR)
    val month2 = calendar2.get(Calendar.MONTH)
    val day2 = calendar2.get(Calendar.DAY_OF_MONTH)

    return year1 != year2 || month1 != month2 || day1 != day2
}
