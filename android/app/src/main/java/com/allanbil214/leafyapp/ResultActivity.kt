package com.allanbil214.leafyapp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.noties.markwon.Markwon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ResultActivity : AppCompatActivity() {

    private lateinit var selectedImage: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var diseaseInfoTextView: TextView
    private lateinit var diseaseInfoLabel: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var textLinkClickable: TextView

    // Declare variables for the data
    private var result: String? = null
    private var plant: String? = null
    private var disease: String? = null
    private var url: String? = null
    private var imageBase64: String? = null

    private val historyManager = HistoryManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        selectedImage = findViewById(R.id.selectedImage)
        resultTextView = findViewById(R.id.resultTextView)
        textLinkClickable = findViewById(R.id.textLinkClickable)
        diseaseInfoLabel = findViewById(R.id.diseaseInfoLabel)
        diseaseInfoTextView = findViewById(R.id.diseaseInfoTextView)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Fetching information, please wait...")
            setCancelable(false)
        }

        result = intent.getStringExtra("RESULT")
        plant = intent.getStringExtra("PLANT")
        disease = intent.getStringExtra("DISEASE")
        url = intent.getStringExtra("URL")
        imageBase64 = intent.getStringExtra("IMAGE")

        resultTextView.text = plant
        diseaseInfoLabel.text = disease

        displayImage(imageBase64)

        result?.let {
            fetchDiseaseInfo(it)
        }

        url?.let { urlString ->
            textLinkClickable.apply {
                setOnClickListener {
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                        startActivity(browserIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
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
        progressDialog.show() // Show the loading dialog

        val retrofit = Retrofit.Builder()
            .baseUrl("https://hyena-pure-violently.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getDiseaseInfo(disease)

        call.enqueue(object : Callback<DiseaseInfo> {
            override fun onResponse(call: Call<DiseaseInfo>, response: Response<DiseaseInfo>) {
                progressDialog.dismiss() // Dismiss the loading dialog

                if (response.isSuccessful) {
                    val info = response.body()?.disease_info
                    info?.let {
                        // Render Markdown to TextView using Markwon
                        val markwon = Markwon.create(this@ResultActivity)
                        markwon.setMarkdown(diseaseInfoTextView, it)
                    } ?: run {
                        diseaseInfoTextView.text = "No information available."
                    }
                    val historyItem = HistoryItem(result, plant, disease, url, imageBase64, info)
                    historyManager.saveHistoryItem(historyItem)

                } else {
                    diseaseInfoTextView.text = "API Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<DiseaseInfo>, t: Throwable) {
                progressDialog.dismiss() // Dismiss the loading dialog

                diseaseInfoTextView.text = "Network Error: ${t.message}"
            }
        })
    }
}

data class HistoryItem(
    val result: String?,
    val plant: String?,
    val disease: String?,
    val url: String?,
    val imageBase64: String?,
    val output: String?
)

class HistoryManager(private val context: Context) {
    private val fileName = "history.json"
    private val gson = Gson()

    fun saveHistoryItem(item: HistoryItem) {
        val history = loadHistory().toMutableList()
        history.add(item)
        saveHistory(history)
    }

    fun loadHistory(): List<HistoryItem> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return emptyList()
        }
        val json = file.readText()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveHistory(history: List<HistoryItem>) {
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(history))
    }
}