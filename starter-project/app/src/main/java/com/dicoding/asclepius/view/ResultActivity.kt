package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_result)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)

        val imageUriString = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUriString?.let {
            showImage(it)
        }
        val resultText = intent.getStringExtra(EXTRA_RESULT)
        resultText?.let {
            Log.d("RESULT", "showResult: $it")
            binding.resultText.text = "Prediction :\n${it[0]}"
            binding.resultTextSecond.text = "Score :${it[1]}"
        }
    }

    private fun showImage(uri: Uri){
        Log.d("Uri Image", "Showing Image: $uri")
        binding.resultImage.setImageURI(uri)
    }
    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object{
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }


}