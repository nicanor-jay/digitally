package com.example.digitallyapp

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.digitallyapp.presentation.counter.CounterAddEditViewModel
import com.example.digitallyapp.presentation.details.DetailsViewModel
import com.example.digitallyapp.presentation.home.HomeViewModel
import com.example.digitallyapp.presentation.settings.SettingsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            HomeViewModel(
                digitallyApplication().preferencesManager,
                digitallyApplication().container.countersRepository
            )
        }

        // Initializer for CounterAddEditViewModel
        initializer {
            CounterAddEditViewModel(
                this.createSavedStateHandle(),
                digitallyApplication().container.countersRepository
            )
        }

        // Initializer for SettingsViewModel
        initializer {
            SettingsViewModel(
//                this.createSavedStateHandle(),
                digitallyApplication().preferencesManager
            )
        }

        initializer {
            DetailsViewModel(
                this.createSavedStateHandle(),
                digitallyApplication().container.countersRepository,
                digitallyApplication().preferencesManager
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [digitallyApplication].
 */
fun CreationExtras.digitallyApplication(): DigitallyApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as DigitallyApplication)