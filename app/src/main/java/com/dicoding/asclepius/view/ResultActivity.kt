package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : BaseActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(EXTRA_IMAGE_URI)?.let {
            val selectedImage = Uri.parse(it)
            binding.resultImage.setImageURI(selectedImage)
        }

        val resultPrediction = intent.getStringExtra(EXTRA_RESULT)
        resultPrediction?.let {
            binding.resultText.text = it
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }

}