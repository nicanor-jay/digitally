package com.example.digitallyapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.digitallyapp.data.AppContainer
import com.example.digitallyapp.data.AppDataContainer
import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.preferences.PreferencesManager
import com.example.digitallyapp.data.repository.CountersRepository
import com.example.digitallyapp.utils.DateChangeReceiver
import com.example.digitallyapp.utils.ResetFrequency
import com.example.digitallyapp.utils.formatDate
import com.example.digitallyapp.utils.isDifferentDay
import com.example.digitallyapp.utils.isDifferentWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

val Context.dataStore by preferencesDataStore(name = "settings_pref")

class DigitallyApplication : Application(), Application.ActivityLifecycleCallbacks {

    lateinit var container: AppContainer
    private lateinit var countersRepository: CountersRepository
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(applicationContext.dataStore)
    }

    override fun onCreate() {
        Log.d("COUNTINGAPPLICATION", "ONCREATE")
        super.onCreate()
        container = AppDataContainer(this)
        countersRepository = container.countersRepository

        // Register ActivityLifecycleCallbacks
        registerActivityLifecycleCallbacks(this)

        // Register the com.example.countingapp.utils.DateChangeReceiver to receive ACTION_DATE_CHANGED broadcasts
        val dateChangeReceiver = DateChangeReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_DATE_CHANGED)
        registerReceiver(dateChangeReceiver, intentFilter)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Called when an activity is created
        // ...
        Log.d("COUNTINGAPPLICATION", "onActivityCreate")
    }

    override fun onActivityStarted(activity: Activity) {
        // Called when an activity is started
        // ...
        Log.d("COUNTINGAPPLICATION", "ONSTART")
        applyCounterResets()
    }

    override fun onActivityResumed(activity: Activity) {
        // Called when an activity is resumed
        // Check if it's a new day and reset counters if needed
        Log.d("COUNTINGAPPLICATION", "ONRESUME")
    }


    override fun onActivityPaused(activity: Activity) {
        // Called when an activity is paused
        // ...
        Log.d("COUNTINGAPPLICATION", "ONPAUSE")
    }

    override fun onActivityStopped(activity: Activity) {
        // Called when an activity is stopped
        // ...
        Log.d("COUNTINGAPPLICATION", "ONSTOP")

    }


    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Called when an activity's state is saved
        // ...
        Log.d("COUNTINGAPPLICATION", "ONSAVEINSTANCE")
    }


    override fun onActivityDestroyed(activity: Activity) {
        applicationScope.cancel()
    }

    fun applyCounterResets() {
        applicationScope.launch(Dispatchers.IO) {
            //Cleanup counts
            Log.d("DigitallyApplication", "Deleting dummy counts. None & Daily")
            countersRepository.cleanupDummyCounts()

            val allWeeklyCountEntries = countersRepository.getAllWeeklyCountEntries()
            if (allWeeklyCountEntries != null) {
                processWeeklyCountEntries(allWeeklyCountEntries)
            } else {
                Log.e("DigitallyApplication", "Weekly count entries list is null")
            }

            //Apply resets
            val currentTimeStamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis

            val countersList = countersRepository.getAllCounters()

            if (!countersList.isNullOrEmpty()) {
                countersList.forEach { counter ->
                    val mostRecentCountEntry = countersRepository.getMostRecentCountEntry(counter.id)

//                    If no most recent count entry exists, immediately create dummy to 0
                    if (mostRecentCountEntry == null) {
                        val id = countersRepository.insertCountEntry(
                            CountEntry(
                                counterId = counter.id,
                                count = 0,
                                dateTime = currentTimeStamp,
                                target = counter.target
                            )
                        )
                        Log.d(
                            "DigitallyApplication, applyCounterResets()",
                            "Initial Count_Entry ID:$id inserted for new counter"
                        )
                        return@forEach
                    }
                    else {
                        Log.d(
                            "com.example.countingapp.CountingApplication",
                            "Counter:  ${mostRecentCountEntry.counterId}"
                        )
                        Log.d(
                            "com.example.countingapp.CountingApplication",
                            "Most Recent Entry ${formatDate(mostRecentCountEntry.dateTime, "dd/MM/yyyy")}"
                        )
                    }


                    when (counter.resetFrequency) {
                        ResetFrequency.NONE -> {
                            if (isDifferentDay(mostRecentCountEntry.dateTime, currentTimeStamp)) {
                                val id = countersRepository.insertCountEntry(
                                    CountEntry(
                                        counterId = counter.id,
                                        count = mostRecentCountEntry.editedCount
                                            ?: mostRecentCountEntry.count,
                                        dateTime = currentTimeStamp,
                                        target = counter.target
                                    )
                                )
                                Log.d(
                                    "DigitallyApplication, applyCounterResets()",
                                    "Count_Entry ID:$id inserted as dummy"
                                )
                            }
                        }

                        ResetFrequency.DAILY -> {
                            if (isDifferentDay(mostRecentCountEntry.dateTime, currentTimeStamp)) {
                                val id = countersRepository.insertCountEntry(
                                    CountEntry(
                                        counterId = counter.id,
                                        count = 0,  // Set the initial count value for a new day
                                        dateTime = currentTimeStamp,
                                        target = counter.target
                                    )
                                )
                                Log.d(
                                    "DigitallyApplication, applyCounterResets()",
                                    "Count_Entry ID:$id inserted as dummy"
                                )
                            }
                        }

                        ResetFrequency.WEEKLY -> {
                            when {
                                isDifferentWeek(mostRecentCountEntry.dateTime, currentTimeStamp) -> {
                                    val id = countersRepository.insertCountEntry(
                                        CountEntry(
                                            counterId = counter.id,
                                            count = 0,
                                            dateTime = currentTimeStamp,
                                            target = counter.target
                                        )
                                    )
                                    Log.d(
                                        "DigitallyApplication, applyCounterResets()",
                                        "Count_Entry ID:$id inserted as dummy"
                                    )
                                }

                                isDifferentDay(mostRecentCountEntry.dateTime, currentTimeStamp) -> {
                                    val id = countersRepository.insertCountEntry(
                                        CountEntry(
                                            counterId = counter.id,
                                            count = mostRecentCountEntry.editedCount
                                                ?: mostRecentCountEntry.count,
                                            dateTime = currentTimeStamp,
                                            target = counter.target
                                        )
                                    )
                                    Log.d(
                                        "DigitallyApplication, applyCounterResets()",
                                        "Count_Entry ID:$id inserted as dummy"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processWeeklyCountEntries(weeklyCountEntries: List<CountEntry>) {
        // Group count entries by counterId and sort each group by dateTime
        val groupedEntries = weeklyCountEntries.groupBy { it.counterId }.mapValues { (_, entries) ->
            entries.sortedBy { it.dateTime }
        }

        val deleteCountEntries = mutableListOf<CountEntry>()

        for ((_, sortedEntries) in groupedEntries) {
            // Keep track of the previous entry
            var prevEntry: CountEntry? = null

            for (entry in sortedEntries) {
                // Check if the current entry is a consecutive duplicate of the previous one
                if (prevEntry != null &&
                    entry.count == prevEntry.count &&
                    entry.editedCount == prevEntry.editedCount &&
                    !isDifferentWeek(entry.dateTime, prevEntry.dateTime)
                ) {
                    deleteCountEntries += entry
                }

                // Update the previous entry
                prevEntry = entry

                // Check if count is 0 and edited count is null or 0
                if ((entry.count == 0) && (entry.editedCount == null || entry.editedCount == 0)) {
                    // Check if it's not the most recent entry in the entire list
                    if (entry != sortedEntries.last()) {
                        deleteCountEntries += entry
                    }
                }
            }
        }

        if (deleteCountEntries.isNotEmpty()) {
            applicationScope.launch(Dispatchers.IO) {
                for (countEntry in deleteCountEntries) {
                    countersRepository.deleteCountEntry(countEntry)
                }
            }
        }
    }
}