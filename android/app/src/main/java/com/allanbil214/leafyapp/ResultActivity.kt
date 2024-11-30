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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

        // Initialize views with null checks
        try {
            selectedImage = findViewById(R.id.selectedImage)
            resultTextView = findViewById(R.id.resultTextView)
            textLinkClickable = findViewById(R.id.textLinkClickable)
            diseaseInfoLabel = findViewById(R.id.diseaseInfoLabel)
            diseaseInfoTextView = findViewById(R.id.diseaseInfoTextView)

            progressDialog = ProgressDialog(this).apply {
                setMessage("Fetching information, please wait...")
                setCancelable(false)
            }

            // Safely extract intent extras
            result = intent.getStringExtra("RESULT") ?: "Unknown Result"
            plant = intent.getStringExtra("PLANT") ?: "Unknown Plant"
            disease = intent.getStringExtra("DISEASE") ?: "Unknown Disease"
            url = intent.getStringExtra("URL")
            imageBase64 = intent.getStringExtra("IMAGE")

            resultTextView.text = plant
            diseaseInfoLabel.text = disease

            displayImage(imageBase64)

            result?.let { fetchDiseaseInfo(it) }

            setupUrlClickListener()

        } catch (e: Exception) {
            handleInitializationError(e)
        }
    }

    private fun setupUrlClickListener() {
        url?.let { urlString ->
            textLinkClickable.apply {
                setOnClickListener {
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                        startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ResultActivity,
                            "Unable to open link: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } ?: run {
            textLinkClickable.setTextColor(
                ContextCompat.getColor(this, R.color.d45f0000)
            )
            textLinkClickable.isClickable = false
        }
    }


    private fun handleInitializationError(e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            this,
            "Error initializing result screen: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
        finish() // Close the activity if initialization fails
    }

    private fun displayImage(imageBase64: String?) {
        try {
            imageBase64?.let {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap != null) {
                    // Calculate the aspect ratio of the image
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

                    // Update the ImageView's layout parameters
                    val layoutParams = selectedImage.layoutParams as ConstraintLayout.LayoutParams
                    layoutParams.dimensionRatio = "H,$aspectRatio"
                    selectedImage.layoutParams = layoutParams

                    selectedImage.setImageBitmap(bitmap)
                } else {
                    throw IllegalStateException("Failed to decode bitmap")
                }
            } ?: run {
                selectedImage.setImageResource(android.R.drawable.ic_menu_camera)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Error displaying image: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            selectedImage.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }

    private fun fetchDiseaseInfo(disease: String) {
        progressDialog.show()

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://hyena-pure-violently.ngrok-free.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val call = apiService.getDiseaseInfo(disease)
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            call.enqueue(object : Callback<DiseaseInfo> {
                override fun onResponse(call: Call<DiseaseInfo>, response: Response<DiseaseInfo>) {
                    progressDialog.dismiss()

                    try {
                        if (response.isSuccessful) {
                            val info = response.body()?.disease_info ?: "No information available."

                            // Render Markdown to TextView using Markwon
                            val markwon = Markwon.create(this@ResultActivity)
                            markwon.setMarkdown(diseaseInfoTextView, info)

                            val historyItem = HistoryItem(
                                result,
                                plant,
                                disease,
                                url,
                                imageBase64,
                                info,
                                currentDate
                            )
                            historyManager.saveHistoryItem(historyItem)

                        } else {
                            throw Exception("API Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        handleDiseaseInfoError(e)
                    }
                }

                override fun onFailure(call: Call<DiseaseInfo>, t: Throwable) {
                    progressDialog.dismiss()
                    handleDiseaseInfoError(t)
                }
            })
        } catch (e: Exception) {
            progressDialog.dismiss()
            handleDiseaseInfoError(e)
        }
    }

    private fun handleDiseaseInfoError(e: Throwable) {
        e.printStackTrace()
        val errorMessage = "Error fetching disease information: ${e.localizedMessage}"

        diseaseInfoTextView.text = errorMessage

        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }
}

data class HistoryItem(
    val result: String?,
    val plant: String?,
    val disease: String?,
    val url: String?,
    val imageBase64: String?,
    val output: String?,
    val date: String?
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

    fun saveHistory(history: List<HistoryItem>) {
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(history))
    }
}