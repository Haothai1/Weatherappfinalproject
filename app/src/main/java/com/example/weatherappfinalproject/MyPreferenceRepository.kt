package com.example.weatherappfinalproject

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MyPreferenceRepository private constructor(private val dataStore: DataStore<Preferences>) {
    companion object {
        private const val PREFERENCES_DATA_FILE_NAME = "settings"
        val CITY_KEY = stringPreferencesKey("cityName")
        val TEMP_KEY = booleanPreferencesKey("tempStatus")
        val WIND_KEY = booleanPreferencesKey("windStatus")


        private var INSTANCE: MyPreferenceRepository? = null

        fun initialize(context: Context){
            if(INSTANCE == null){
                val dataStore = PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile(PREFERENCES_DATA_FILE_NAME)
                }
                INSTANCE = MyPreferenceRepository(dataStore)
            }
        }

        fun get(): MyPreferenceRepository {
            return INSTANCE ?: throw IllegalStateException("MyPreferenceRepository has not yet been initialize() 'ed'")
        }
    }

    val cityName: Flow<String> = dataStore.data.map { prefs ->
        prefs[CITY_KEY] ?: ""
    }.distinctUntilChanged()

    val tempState: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[TEMP_KEY] ?: false
    }.distinctUntilChanged()

    val windState: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[WIND_KEY] ?: false
    }.distinctUntilChanged()


    suspend fun saveStringValue(key: Preferences.Key<String>, value: String) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    suspend fun saveBooleanValue(key: Preferences.Key<Boolean>,boolVal: Boolean){
        dataStore.edit {prefs ->
            prefs[key] = boolVal
        }
    }
}
