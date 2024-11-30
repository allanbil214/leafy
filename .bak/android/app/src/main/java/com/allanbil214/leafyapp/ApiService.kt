package com.allanbil214.leafyapp

import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Retrofit

interface ApiService {
    @POST("predict")
    fun predictDisease(@Body request: PredictRequest): Call<PredictionResult>

    @GET("disease-info/{disease}")
    fun getDiseaseInfo(@Path("disease") disease: String): Call<DiseaseInfo>
}

data class PredictRequest(val base64_encoded: String)

data class PredictionResult(
    val predicted_class: String,
    val plant_name: String,
    val plant_disease: String,
    val plant_url: String
)

data class DiseaseInfo(val disease_info: String)

object RetrofitClient {
    private const val BASE_URL = "http://your-api-url/" // Replace with your FastAPI URL

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherApiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}