package com.allanbil214.leafyapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import android.app.ProgressDialog // Import this for the loading dialog
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import androidx.appcompat.app.AppCompatDelegate

class ControlledRandomizer(private val items: List<Int>) {
    private var lastIndex = -1
    private var secondLastIndex = -1

    fun getNextItem(): Int {
        // Ensure we have items to work with
        if (items.isEmpty()) throw IllegalStateException("Items list cannot be empty")
        if (items.size == 1) return items[0]

        // Get a new random index, avoiding the last two used indices
        var newIndex: Int
        do {
            newIndex = (0 until items.size).random()
        } while (newIndex == lastIndex || newIndex == secondLastIndex)

        // Update history
        secondLastIndex = lastIndex
        lastIndex = newIndex

        return items[newIndex]
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var girlplanting_image: ImageView
    private lateinit var selectedImage: ImageView
    private lateinit var button_choosefile: ImageView
    private lateinit var button_takephoto: ImageView
    private lateinit var historyButton: ImageView
    private var imageBase64: String? = null
    private lateinit var currentPhotoPath: String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var randomizer: ControlledRandomizer

    // List of vector drawable resources
    private val images = listOf(
        R.drawable.girl_planting_var1,
        R.drawable.girl_planting_var2,
        R.drawable.girl_planting_var3,
        R.drawable.girl_planting_var4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Base features
        selectedImage = findViewById(R.id.selectedImage)
        button_choosefile = findViewById(R.id.button_choosefile)
        button_takephoto = findViewById(R.id.button_takephoto)
        girlplanting_image = findViewById(R.id.girlplanting_image)
        historyButton = findViewById(R.id.historyImageView)

        // Progress Bar
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Processing, please wait...")
        progressDialog.setCancelable(false)

        randomizer = ControlledRandomizer(images)

        // Randomizer
        setNewImage()

        // Randomizer 2
        girlplanting_image.setOnClickListener {
            setNewImage()
        }

        // Base Features
        button_choosefile.setOnClickListener {
            openGallery()
        }

        button_takephoto.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }


    }

    // Helper function to set a random image
    private fun setNewImage() {
        val nextImage = randomizer.getNextItem()
        girlplanting_image.setImageResource(nextImage)
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.allanbil214.leafyapp.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        val inputStream = contentResolver.openInputStream(it)
                        val selectedBitmap = BitmapFactory.decodeStream(inputStream)
                        processImage(selectedBitmap)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val file = File(currentPhotoPath)
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    processImage(bitmap)
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        progressDialog.show() // Show the loading dialog

        val resizedBitmap = resizeBitmap(bitmap, 640, 420)
        selectedImage.setImageBitmap(resizedBitmap)
        imageBase64 = encodeImage(resizedBitmap)
        sendImage(imageBase64!!)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val scaleFactor = min(maxWidth.toFloat() / originalWidth, maxHeight.toFloat() / originalHeight)

        val newWidth = (originalWidth * scaleFactor).toInt()
        val newHeight = (originalHeight * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream) // Reduced quality to 90%
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun sendImage(base64String: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://hyena-pure-violently.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val request = PredictRequest(base64_encoded = base64String)

        val call = apiService.predictDisease(request)

        call.enqueue(object : Callback<PredictionResult> {
            override fun onResponse(call: Call<PredictionResult>, response: Response<PredictionResult>) {
                progressDialog.dismiss()

                if (response.isSuccessful) {
                    val result = response.body()?.predicted_class
                    val plant = response.body()?.plant_name
                    val disease = response.body()?.plant_disease
                    val url = response.body()?.plant_url
                    startResultActivity(result, plant, disease, url)
                } else {
                    val result = "API Error: ${response.code()}"
                    val plant = "API Error: ${response.code()}"
                    val disease = "API Error: ${response.code()}"
                    val url = "API Error: ${response.code()}"
                    startResultActivity(result, plant, disease, url)
                }
            }

            override fun onFailure(call: Call<PredictionResult>, t: Throwable) {
                progressDialog.dismiss() // Dismiss the dialog in case of failure
                val result = "Network Error: ${t.message}"
                val plant = "Network Error: ${t.message}"
                val disease = "Network Error: ${t.message}"
                val url = "Network Error: ${t.message}"
                startResultActivity(result, plant, disease, url)
            }
        })
    }

    private fun startResultActivity(result: String?, plant: String?, disease: String?, url: String?) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("RESULT", result)
        intent.putExtra("PLANT", plant)
        intent.putExtra("DISEASE", disease)
        intent.putExtra("URL", url)
        intent.putExtra("IMAGE", imageBase64)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_CAMERA_PERMISSION = 102
    }
}