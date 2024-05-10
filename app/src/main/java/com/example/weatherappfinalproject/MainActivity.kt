package com.example.weatherappfinalproject

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import android.media.MediaPlayer
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.fragment.NavHostFragment
import android.widget.CompoundButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)
    private lateinit var cityEditText: EditText
    private lateinit var temperatureTextView: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var rainChanceTextView: TextView
    private lateinit var conditionTextView: TextView
    private lateinit var cityNameTextView: TextView
    private lateinit var temperatureSwitch: Switch
    private lateinit var windSwitch: Switch
    private lateinit var conditionImageView: ImageView

    private val switchLog = "switch_Info"
    private var temperatureCelsius = 0
    private var temperatureFahrenheit = 0
    private var windMetric = 0.0
    private var windImperial = 0.0
    private var mediaPlayer: MediaPlayer? = null

    private val weatherVM: WeatherViewModel by lazy {
        // Initialize the MyPreferenceRepository first
        MyPreferenceRepository.initialize(this)

        // Now create the ViewModel
        ViewModelProvider(this)[WeatherViewModel::class.java].also {
            it.loadInputs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // Getting the NavHostFragment and NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Configure AppBar with top-level destinations
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
        cityEditText = findViewById(R.id.idEdtCity)
        temperatureTextView = findViewById(R.id.idTVTemperature)
        windSpeedTextView = findViewById(R.id.idTVWindSpeed)
        humidityTextView = findViewById(R.id.idTVHumidity)
        rainChanceTextView = findViewById(R.id.idTVRainChance)
        conditionTextView = findViewById(R.id.idTVCondition)
        cityNameTextView = findViewById(R.id.idTVCityName)
        val temperatureSwitch: SwitchCompat = findViewById(R.id.idTempSwitch)
        val windSwitch: SwitchCompat = findViewById(R.id.idWindSwitch)
        conditionImageView = findViewById(R.id.idIVIcon)


        fetchWeatherData(weatherVM.cityInput)
        cityNameTextView.text = weatherVM.cityInput.uppercase()

        populateData()
        initializeSwitches()
        findViewById<ImageView>(R.id.idTVSearch).setOnClickListener {
            populateData()
        }

        temperatureSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            tempSwitchConditions()
        }

        windSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            windSwitchConditions()
        }
    }
        private fun fetchWeatherData(city: String) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val weatherResponse = weatherService.getWeather(city, "8a8509cd5d77ca6eef6520e383454eac")
                    updateUI(weatherResponse)
                } catch (e: Exception) {
                    // Handle error
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Not in location data", Toast.LENGTH_SHORT).show()
                        Log.e("WeatherApp", "Not in location data.: ${e.message}")
                    }
                }
            }
        }

        private fun populateData() {
            weatherVM.cityInput = cityEditText.text.toString().trim()
            weatherVM.saveStringInput(MyPreferenceRepository.CITY_KEY, weatherVM.cityInput)
            if (weatherVM.cityInput.isNotEmpty()) {
                fetchWeatherData(weatherVM.cityInput)
                cityNameTextView.text = weatherVM.cityInput.uppercase()
                cityEditText.text.clear() // Clear the search bar
            } else {
                Toast.makeText(this, getString(R.string.warning_text_search_bar), Toast.LENGTH_SHORT).show()
            }
        }

        private fun updateUI(weatherResponse: WeatherResponse?) {
            runOnUiThread {
                weatherResponse?.let {
                    //Temperature Units on search
                    val tempKelvin = it.main.temp
                    if(weatherVM.tempBool)
                    {
                        temperatureFahrenheit = (tempKelvin - 273.15).toInt() * 9 / 5 + 32
                        temperatureTextView.text = getString(R.string.temperature_main) + " ${temperatureFahrenheit}°F"
                    }
                    else
                    {
                        temperatureCelsius = (tempKelvin - 273.15).toInt()
                        temperatureTextView.text = getString(R.string.temperature_main) + " ${temperatureCelsius}°C"
                    }

                    //Wind speed Units on search
                    if(weatherVM.windBool)
                    {
                        windImperial = (it.wind.speed) * 2.237
                        val windSpeedText = getString(R.string.wind_speed_text) + String.format(" %.2f mi/hr", windImperial)
                        windSpeedTextView.text = windSpeedText
                    }
                    else
                    {
                        windMetric = it.wind.speed
                        val windSpeedText = getString(R.string.wind_speed_text) + String.format(" %.2f m/s", windMetric)
                        windSpeedTextView.text = windSpeedText
                    }

                    //Default search values
                    humidityTextView.text = getString(R.string.humidity_text) + " ${it.main.humidity}%"
                    rainChanceTextView.text = getString(R.string.rain_chance_text) + " ${it.rain?.chance ?: 0.0}%"
                    val condition = it.weather[0].description
                    conditionTextView.text = getString(R.string.condition_text) + " $condition"

                    // Check if the weather condition is "clear sky"
                    if (condition.lowercase(Locale.ROOT).contains("clear sky")) {
                        // Play audio here
                        playClearSkyAudio()
                    } else {
                        // Stop the audio if it's playing
                        stopAudio()
                    }

                    val conditionIcon = it.weather[0].icon
                    val iconUrl = "https://openweathermap.org/img/wn/$conditionIcon.png"
                    Picasso.get().isLoggingEnabled = true
                    Picasso.get().load(iconUrl).resize(250, 250).placeholder(R.drawable.sunny).into(conditionImageView)
                    Log.d("Picasso", "Loading image from url: $iconUrl")
                }
            }
        }

        private fun playClearSkyAudio() {
            // Release the previous MediaPlayer if it exists
            stopAudio()

            // Initialize a new MediaPlayer with your audio file
            mediaPlayer = MediaPlayer.create(this, R.raw.clear_sky_audio)
            mediaPlayer?.start()
        }

        private fun stopAudio() {
            mediaPlayer?.release()
            mediaPlayer = null
        }

        //Set temperature and wind speed values based on desired unit of on launch/relaunch of app
        private fun initializeSwitches() {
            temperatureSwitch.isChecked = weatherVM.tempBool
            windSwitch.isChecked = weatherVM.windBool
            Log.d(switchLog, "Initialize switches called")
        }

        //Calculate and save temperature value based on Switch position
        private fun tempSwitchConditions()
        {
            if(temperatureSwitch.isChecked)
            {
                weatherVM.tempBool = true
                weatherVM.saveBooleanInput(MyPreferenceRepository.TEMP_KEY, weatherVM.tempBool)
            }
            else
            {
                weatherVM.tempBool = false
                weatherVM.saveBooleanInput(MyPreferenceRepository.TEMP_KEY,weatherVM.tempBool)
            }
            fetchWeatherData(weatherVM.cityInput)
        }

        //Calculate and save wind speed values based on Switch position
        private fun windSwitchConditions()
        {
            if(windSwitch.isChecked)
            {
                weatherVM.windBool = true
                weatherVM.saveBooleanInput(MyPreferenceRepository.WIND_KEY, weatherVM.windBool)
            }
            else
            {
                weatherVM.windBool = false
                weatherVM.saveBooleanInput(MyPreferenceRepository.WIND_KEY, weatherVM.windBool)
            }
            fetchWeatherData(weatherVM.cityInput)
        }


}