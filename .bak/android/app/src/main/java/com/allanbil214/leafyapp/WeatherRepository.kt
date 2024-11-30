package com.allanbil214.leafyapp

import retrofit2.http.POST
import retrofit2.http.Body

interface WeatherApiService {
    @POST("weather/current")
    suspend fun getCurrentWeather(@Body location: LocationData): WeatherResponse

    @POST("weather/forecast")
    suspend fun getWeatherForecast(@Body location: LocationData): ForecastResponse
}

class WeatherRepository {
    private val api = RetrofitClient.weatherApiService

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherResponse {
        return api.getCurrentWeather(LocationData(latitude, longitude))
    }

    suspend fun getWeatherForecast(latitude: Double, longitude: Double): List<ForecastItem> {
        val response = api.getWeatherForecast(LocationData(latitude, longitude))
        return processForecastResponse(response)
    }

    private fun processForecastResponse(response: ForecastResponse): List<ForecastItem> {
        // Process and group forecast data by day
        // This is a simplified version - you might want to add more processing
        return response.list.map { forecast ->
            ForecastItem(
                day = getDayFromTimestamp(forecast.dt),
                temperature = forecast.main.temp,
                description = forecast.weather.first().description,
                iconUrl = forecast.weather.first().icon
            )
        }
    }
}