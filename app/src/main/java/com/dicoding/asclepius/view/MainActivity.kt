package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.util.UUID

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }

        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            }
        }
    }

    private fun startGallery() {
        gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val gallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if(uri != null){
            val cropUri = Uri.fromFile(File(cacheDir, "cropped_image_${UUID.randomUUID()}.jpg"))

            UCrop.of(uri, cropUri)
                .withAspectRatio(16F, 16F)
                .withMaxResultSize(2000, 2000)
                .start(this)

            showImage()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultCropped = UCrop.getOutput(data!!)
            currentImageUri = resultCropped
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(selectedImage: Uri) {
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
                                val resultPrediction = results.joinToString("\n") {
                                    "${it.categories[0].label} : ${(it.categories[0].score * 100).toInt()}%"
                                }
                                this@MainActivity.runOnUiThread {
                                    moveToResult(selectedImage, resultPrediction)
                                }
                            } else {
                                showToast()
                            }
                        }
                    }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(selectedImage)
    }

    private fun moveToResult(selectedImage: Uri, resultPrediction: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, selectedImage.toString())
        intent.putExtra(ResultActivity.EXTRA_RESULT, resultPrediction)
        startActivity(intent)
    }

    private fun showToast(message: String = "result not found") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}