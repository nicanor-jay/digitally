package com.example.digitallyapp.data

import com.example.digitallyapp.presentation.home.CounterWithCurrentCount
import com.example.digitallyapp.utils.ResetFrequency

fun loadCounters(): List<CounterWithCurrentCount> {
    return listOf(
        CounterWithCurrentCount(0, "Pull-ups", "", ResetFrequency.NONE, 5),
        CounterWithCurrentCount(1, "Glasses Drank", "", ResetFrequency.NONE, 0),
        CounterWithCurrentCount(2, "Misc", "", ResetFrequency.NONE, 2),
        CounterWithCurrentCount(3, "Counter 4", "", ResetFrequency.DAILY, 4),
        CounterWithCurrentCount(4, "Counter 5", "", ResetFrequency.DAILY, 7),
        CounterWithCurrentCount(5, "Counter 6", "", ResetFrequency.DAILY, 11),
        CounterWithCurrentCount(6, "Counter 7", "", ResetFrequency.WEEKLY, 42),
        CounterWithCurrentCount(7, "Counter 8", "", ResetFrequency.WEEKLY, 6),
        CounterWithCurrentCount(8, "Counter 9", "", ResetFrequency.WEEKLY, 7),
    )
}