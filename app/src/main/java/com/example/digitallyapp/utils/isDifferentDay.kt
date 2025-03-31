package com.example.digitallyapp.utils

import java.time.Instant
import java.time.ZoneId

fun isDifferentDay(timestamp1: Long, timestamp2: Long): Boolean {
    val date1 = Instant.ofEpochMilli(timestamp1).atZone(ZoneId.systemDefault()).toLocalDate()
    val date2 = Instant.ofEpochMilli(timestamp2).atZone(ZoneId.systemDefault()).toLocalDate()

    return date1 != date2
}
