package com.allanbil214.leafyapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val weatherRepository = WeatherRepository()

    private val _currentWeather = MutableLiveData<Resource<WeatherResponse>>()
    val currentWeather: LiveData<Resource<WeatherResponse>> = _currentWeather

    private val _forecast = MutableLiveData<Resource<List<ForecastItem>>>()
    val forecast: LiveData<Resource<List<ForecastItem>>> = _forecast

    fun getCurrentLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _currentWeather.value = Resource.Error("Location permission required")
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    fetchWeatherData(it.latitude, it.longitude)
                } ?: run {
                    _currentWeather.value = Resource.Error("Unable to get location")
                }
            }
            .addOnFailureListener {
                _currentWeather.value = Resource.Error("Failed to get location: ${it.message}")
            }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _currentWeather.value = Resource.Loading()

            try {
                val currentWeather = weatherRepository.getCurrentWeather(latitude, longitude)
                _currentWeather.value = Resource.Success(currentWeather)

                val forecast = weatherRepository.getWeatherForecast(latitude, longitude)
                _forecast.value = Resource.Success(forecast)
            } catch (e: Exception) {
                _currentWeather.value = Resource.Error("Failed to fetch weather data: ${e.message}")
            }
        }
    }
}