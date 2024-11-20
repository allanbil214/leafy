package com.example.leafy

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ResultActivity : AppCompatActivity() {

    private lateinit var selectedImage: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var diseaseInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        selectedImage = findViewById(R.id.selectedImage)
        resultTextView = findViewById(R.id.resultTextView)
        diseaseInfoTextView = findViewById(R.id.diseaseInfoTextView)

        val result = intent.getStringExtra("RESULT")
        val imageBase64 = intent.getStringExtra("IMAGE")

        resultTextView.text = result
        displayImage(imageBase64)

        result?.let {
            fetchDiseaseInfo(it)
        }
    }

    private fun displayImage(imageBase64: String?) {
        imageBase64?.let {
            val imageBytes = Base64.decode(it, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // Calculate the aspect ratio of the image
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

            // Update the ImageView's layout parameters
            val layoutParams = selectedImage.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.dimensionRatio = "H,$aspectRatio"
            selectedImage.layoutParams = layoutParams

            selectedImage.setImageBitmap(bitmap)
        }
    }

    private fun fetchDiseaseInfo(disease: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hyena-pure-violently.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getDiseaseInfo(disease)

        call.enqueue(object : Callback<DiseaseInfo> {
            override fun onResponse(call: Call<DiseaseInfo>, response: Response<DiseaseInfo>) {
                if (response.isSuccessful) {
                    val info = response.body()?.disease_info
                    diseaseInfoTextView.text = info
                } else {
                    diseaseInfoTextView.text = "API Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<DiseaseInfo>, t: Throwable) {
                diseaseInfoTextView.text = "Network Error: ${t.message}"
            }
        })
    }
}