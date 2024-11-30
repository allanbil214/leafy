package com.allanbil214.leafyapp

import android.R
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import com.allanbil214.leafyapp.databinding.ActivityWeatherBinding

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupViewModel()
        observeWeatherData()

        // Get location and fetch weather data
        weatherViewModel.getCurrentLocation(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        forecastAdapter = ForecastAdapter()
        binding.rvForecast.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity)
            adapter = forecastAdapter
        }
    }

    private fun setupViewModel() {
        weatherViewModel = WeatherViewModel()
    }

    private fun observeWeatherData() {
        weatherViewModel.currentWeather.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    showLoading(false)
                    result.data?.let { weather ->
                        updateCurrentWeather(weather)
                    } ?: showError("Weather data is not available")
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(result.message ?: "An unknown error occurred")
                }
                is Resource.Loading -> showLoading(true)
            }
        }

        weatherViewModel.forecast.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    forecastAdapter.submitList(result.data)
                }
                is Resource.Error -> {
                    showError(result.message ?: "An unknown error occurred")
                }
                is Resource.Loading -> { /* Already handled by current weather */ }
            }
        }
    }

    private fun updateCurrentWeather(weather: WeatherResponse) {
        binding.apply {
            tvDetailLocation.text = weather.location
            tvDetailTemp.text = "${weather.temperature.current.roundToInt()}°C"
            tvDetailFeelsLike.text = "Feels like: ${weather.temperature.feels_like.roundToInt()}°C"
            tvDetailWeatherDescription.text = weather.weather.description
            tvDetailHumidity.text = "Humidity: ${weather.humidity}%"
            tvDetailWind.text = "Wind: ${weather.wind.speed} km/h"

            // Load weather icon
            Glide.with(this@WeatherActivity)
                .load(weather.weather.icon)
                .into(ivDetailWeatherIcon)

            // Format and display timestamp
            val dateTime = LocalDateTime.ofInstant(
                Instant.parse(weather.timestamp),
                ZoneId.systemDefault()
            )
            tvDetailDateTime.text = "Updated: ${dateTime.format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            )}"
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // Implement loading state UI
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}