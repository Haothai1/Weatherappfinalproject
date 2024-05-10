package com.example.weatherappfinalproject

// Represents the response from the weather API
data class WeatherResponse(
    val dt: Long, // Timestamp in seconds since Unix epoch - Doesn't really work - need to fix
    val main: Main,
    val timezone: String,
    val wind: Wind,
    val weather: List<Weather>,
    val rain: Rain?
)

data class Main(
    // temperature in Kelvin
    val temp: Double,
    // humidity as a %
    val humidity: Int
)

data class Wind(
    // windspeed in metric
    val speed: Double
)

data class Weather(
    // Description of weather - ("Partly Cloudy", "Sunny", "Rainy", etc)
    val description: String,
    val icon: String

)

data class Rain(
    // Chance of rain
    val chance: Double
)
