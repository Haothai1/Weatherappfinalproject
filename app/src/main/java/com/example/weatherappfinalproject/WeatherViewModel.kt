package com.example.weatherappfinalproject

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WeatherViewModel : ViewModel(){
    private val prefs = MyPreferenceRepository.get()

    var cityInput = ""
    var tempBool = false
    var windBool = false

    fun saveStringInput( key: Preferences.Key<String>, s: String){
        viewModelScope.launch {
            prefs.saveStringValue(key, s)
        }
    }


    fun saveBooleanInput(key: Preferences.Key<Boolean>, b: Boolean){
        viewModelScope.launch{
            prefs.saveBooleanValue(key, b)
        }
    }

    fun loadInputs() {
        runBlocking {
            cityInput = prefs.cityName.first()
            tempBool = prefs.tempState.first()
            windBool = prefs.windState.first()
        }
    }
}