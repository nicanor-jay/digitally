package com.example.digitallyapp.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.digitallyapp.presentation.home.SortDropdownValues
import com.example.digitallyapp.presentation.home.getStringFromSortDropdownValue
import com.example.digitallyapp.utils.DateFormats
import com.example.digitallyapp.utils.getDateFormatString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

class PreferencesManager(private val dataStore: DataStore<Preferences>) {
    // Preferences keys
    private object PreferencesKeys {
        val DYNAMIC_COLOR_SETTING_KEY = booleanPreferencesKey("dynamic_color_setting_key")
        val ARCHIVED_VISIBILITY_SETTING_KEY =
            booleanPreferencesKey("archived_visibility_setting_key")
        val SHOW_TARGET_SETTING_KEY = booleanPreferencesKey("show_target_setting_key")
        val DATE_FORMAT_SETTING_KEY = stringPreferencesKey("date_format_setting_key")
        val CONFETTI_SETTING_KEY = booleanPreferencesKey("confetti_setting_key")
        val SORT_TYPE_KEY = stringPreferencesKey("sort_type_key")
        val SORT_ORDER_ASC_KEY = booleanPreferencesKey("sort_order_asc_key")
    }

    // Create a flow for the dynamic color preference with an initial value
    val dynamicColorPreferenceFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            // Catch any exception during reading preferences
            if (exception is IOException) {
                emit(emptyPreferences()) // Handle error gracefully
            } else {
                throw exception // Rethrow other exceptions
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR_SETTING_KEY] ?: false
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            false
        ) // Set initial value as 'false'

    val viewArchivedCountersPreferenceFlow: Flow<Boolean> = dataStore.data.catch { exception ->
        // Catch any exception during reading preferences
        if (exception is IOException) {
            emit(emptyPreferences()) // Handle error gracefully
        } else {
            throw exception // Rethrow other exceptions
        }
    }
        .map { preferences ->
            preferences[PreferencesKeys.ARCHIVED_VISIBILITY_SETTING_KEY] ?: false
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            false
        ) // Set initial value as 'false'

    val showTargetPreferencesFlow: Flow<Boolean> = dataStore.data.catch { exception ->
        // Catch any exception during reading preferences
        if (exception is IOException) {
            emit(emptyPreferences()) // Handle error gracefully
        } else {
            throw exception // Rethrow other exceptions
        }
    }
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_TARGET_SETTING_KEY] ?: true
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            true
        ) // Set initial value as 'true'

    val dateFormatPreference: Flow<String> = dataStore.data.catch { exception ->
        // Catch any exception during reading preferences
        if (exception is IOException) {
            emit(emptyPreferences()) // Handle error gracefully
        } else {
            throw exception // Rethrow other exceptions
        }
    }
        .map { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT_SETTING_KEY] ?: "dd/MM/yyyy"
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            "dd/MM/yyyy"
        )

    val confettiPreference: Flow<Boolean> = dataStore.data.catch { exception ->
        // Catch any exception during reading preferences
        if (exception is IOException) {
            emit(emptyPreferences()) // Handle error gracefully
        } else {
            throw exception // Rethrow other exceptions
        }
    }
        .map { preferences ->
            preferences[PreferencesKeys.CONFETTI_SETTING_KEY] ?: true
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            true
        ) // Set initial value as 'true'

    val sortTypePreferenceFlow: Flow<String> = dataStore.data.catch { exception ->
        // Catch any exception during reading preferences
        if (exception is IOException) {
            emit(emptyPreferences()) // Handle error gracefully
        } else {
            throw exception // Rethrow other exceptions
        }
    }
        .map { preferences ->
            preferences[PreferencesKeys.SORT_TYPE_KEY] ?: "Date created"
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            "Date created"
        )

    val sortOrderPreferenceFlow: Flow<Boolean> = dataStore.data.catch { exception ->
        // Catch any exception during reading preferences
        if (exception is IOException) {
            emit(emptyPreferences()) // Handle error gracefully
        } else {
            throw exception // Rethrow other exceptions
        }
    }
        .map { preferences ->
            preferences[PreferencesKeys.SORT_ORDER_ASC_KEY] ?: true
        }
        .stateIn(
            CoroutineScope(Dispatchers.Main),
            SharingStarted.Eagerly,
            true
        ) // Set initial value as 'false'

    // Function to update the dynamic color preference
    suspend fun updateDynamicColorPreference(dynamicColor: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR_SETTING_KEY] = dynamicColor
        }
    }

    suspend fun updateShowTargetPreference(showTarget: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TARGET_SETTING_KEY] = showTarget

        }
    }

    suspend fun updateViewArchivedCountersPreference(viewArchivedCounters: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ARCHIVED_VISIBILITY_SETTING_KEY] = viewArchivedCounters
        }
        // Log the updated value
        val updatedValue =
            dataStore.data.first()[PreferencesKeys.ARCHIVED_VISIBILITY_SETTING_KEY] ?: false
        Log.d(
            "PreferencesManager",
            "Preference updated to : $updatedValue"
        )
    }

    suspend fun updateDateFormatPreference(dateFormat: DateFormats) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT_SETTING_KEY] = getDateFormatString(dateFormat)
        }
    }

    suspend fun updateSortValuePreference(sortValue: SortDropdownValues) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_TYPE_KEY] = getStringFromSortDropdownValue(sortValue)
        }
    }

    suspend fun updateSortOrderPreference(bool: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER_ASC_KEY] = bool
        }
    }

    suspend fun updateConfettiPreference(bool: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONFETTI_SETTING_KEY] = bool
        }
    }

}