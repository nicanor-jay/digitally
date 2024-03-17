package com.example.digitallyapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.digitallyapp.DigitallyApplication

class DateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_DATE_CHANGED) {
            Log.d(
                "com.example.countingapp.utils.DateChangeReceiver",
                "Date has changed, perform cleanup and counter resets here"
            )

            // Get the application context
            val appContext = context.applicationContext

            // Cast the application context to com.example.countingapp.CountingApplication
            if (appContext is DigitallyApplication) {
                // Call the public method to reset counters on day change
                appContext.applyCounterResets()
            }
        }
    }
}