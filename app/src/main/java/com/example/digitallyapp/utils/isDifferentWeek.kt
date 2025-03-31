package com.example.digitallyapp.utils

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.IsoFields

fun isDifferentWeek(timestamp1: Long, timestamp2: Long): Boolean {
    val date1 = Instant.ofEpochMilli(timestamp1).atZone(ZoneId.systemDefault()).toLocalDate()
    val date2 = Instant.ofEpochMilli(timestamp2).atZone(ZoneId.systemDefault()).toLocalDate()

    val week1 = date1.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val week2 = date2.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    val year1 = date1.year
    val year2 = date2.year

    return year1 != year2 || week1 != week2
}