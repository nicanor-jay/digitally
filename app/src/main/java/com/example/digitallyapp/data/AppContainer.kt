package com.example.digitallyapp.data

import android.content.Context
import com.example.digitallyapp.data.local.MyCountersDatabase
import com.example.digitallyapp.data.local.OfflineCountersRepository
import com.example.digitallyapp.data.repository.CountersRepository

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val countersRepository: CountersRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineCountersRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [CountersRepository]
     */
    override val countersRepository: CountersRepository by lazy {
        OfflineCountersRepository(MyCountersDatabase.getDatabase(context).counterDao())
    }
}