package dev.chu.opencv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.chu.opencv.databinding.ActivityMainBinding
import dev.chu.opencv.util.click

/**
 * https://webnautes.tistory.com/1054?category=704164
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.camera.click {
            startActivity(Intent(this, OpenCVCameraActivity::class.java))
        }

        binding.gallery.click {
            startActivity(Intent(this, OpenCVGalleryActivity::class.java))
        }
    }
}