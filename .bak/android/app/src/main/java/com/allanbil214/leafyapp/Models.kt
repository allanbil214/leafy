package com.allanbil214.leafyapp

import java.text.SimpleDateFormat
import java.util.*

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

data class WeatherResponse(
    val location: String,
    val temperature: Temperature,
    val weather: Weather,
    val humidity: Int,
    val wind: Wind,
    val timestamp: String
)

data class Temperature(
    val current: Double,
    val feels_like: Double,
    val min: Double,
    val max: Double
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val direction: Int
)

// Added new data classes for forecast API response
data class ForecastResponse(
    val list: List<ForecastData>
)

data class ForecastData(
    val dt: Long,  // Unix timestamp
    val main: ForecastMain,
    val weather: List<ForecastWeather>
)

data class ForecastMain(
    val temp: Double
)

data class ForecastWeather(
    val description: String,
    val icon: String
)

data class ForecastItem(
    val day: String,
    val temperature: Double,
    val description: String,
    val iconUrl: String
)

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

// Helper function to convert timestamp to day string
fun getDayFromTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}