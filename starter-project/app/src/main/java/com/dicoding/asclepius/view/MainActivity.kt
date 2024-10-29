package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private var currentImageUri: Uri? = null

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){isGranted: Boolean ->
            if (isGranted){
                Toast.makeText(this,"Permission request granted", Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this, "Permissioan request denied", Toast.LENGTH_LONG).show()
            }
        }


    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_main)
        setContentView(binding.root)


        with(binding){
            galleryButton.setOnClickListener { startGallery() }
            analyzeButton.setOnClickListener {
                currentImageUri?.let {
                    analyzeImage(it)
                }?: run {
                    showToast(getString(R.string.image_empty))
                }
            }
        }

    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ){uri: Uri? ->
        if (uri != null){
            currentImageUri = uri
            showImage()
        }else{
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
//            binding.previewImageView.setImageURI(it)
            if (currentImageUri != null){
                binding.previewImageView.setImageURI(it)
            }
        }
    }

    private fun analyzeImage(uri: Uri) {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResults(results: List<Classifications>?) {
                    runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                println(it)
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it?.score }
                                val highestConfidenceCategory = sortedCategories[0]
                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${highestConfidenceCategory.label} " + NumberFormat.getPercentInstance().format(highestConfidenceCategory.score).trim()
                                    }
                                val confidentScore =
                                    sortedCategories[0].label + NumberFormat.getPercentInstance()
                                        .format(sortedCategories[0].score)

                                showToast("Analysis Complete")
                                moveToResult(displayResult, confidentScore)



                            }
                        }
                    }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(uri)

    }

    private fun moveToResult(result1: String, result2: String) {
       lifecycleScope.launch {
           delay(3000)
           val intent = Intent(this@MainActivity, ResultActivity::class.java)
           intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
           intent.putExtra(ResultActivity.EXTRA_RESULT, arrayOf(result1, result2))
           startActivity(intent)
       }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object{
        private const val REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}