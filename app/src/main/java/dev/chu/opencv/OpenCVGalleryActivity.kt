package dev.chu.opencv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dev.chu.opencv.databinding.ActivityOpencvGalleryBinding
import dev.chu.opencv.util.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * http://melonicedlatte.com/android/2018/05/12/030916.html
 */
class OpenCVGalleryActivity : AppCompatActivity() {

    companion object {
        private const val GET_GALLERY_IMAGE = 2000
        private const val WRITE_STORAGE_PERMISSION_REQUEST_CODE = 1000
    }

    private val binding by lazy { ActivityOpencvGalleryBinding.inflate(layoutInflater) }
    private val native by lazy { NativeWrapper() }
    private val permissions by lazy { arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) }
    private val loaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    imgInput = Mat()
                    imgOutput = Mat()
                }
                else -> super.onManagerConnected(status)
            }
        }
    }

    private var imgInput: Mat? = null
    private var imgOutput: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onEvent()
    }

    override fun onResume() {
        super.onResume()

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, loaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
        getPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK) {
            data?.let { intent ->
                intent.data?.let {
                    val str = getRealPathFromURI(it)
                    val checkImage = native.checkImage(str)
                    Log.i(TAG, "result : str = $str\ncheckImage = $checkImage")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_STORAGE_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            readImageFile()
            imageProcessAndShowResult()
        } else {
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가해~~")
        }
    }

    private fun getPermission() {
        if (hasPermissions(*permissions)) {
            readImageFile()
            imageProcessAndShowResult()
        } else {
            requestNecessaryPermissions(*permissions)
        }
    }

    private fun onEvent() {
        binding.gallery.click {
            startActivityForResult(Intent(Intent.ACTION_PICK).apply {
                setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            }, GET_GALLERY_IMAGE)
        }
    }

    private fun requestNecessaryPermissions(vararg permission: String) {
        if (isUpAndroid23()) {
            requestPermissions(
                permission,
                WRITE_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun readImageFile() {
        copyFile("autumn.jpg")

        if (imgInput != null)
            native.loadImage("autumn.jpg", imgInput!!.nativeObjAddr)
    }

    private fun imageProcessAndShowResult() {
        if (imgInput != null && imgOutput != null) {
            native.imageProcessing(imgInput!!.nativeObjAddr, imgOutput!!.nativeObjAddr)

            val bitmapInput: Bitmap =
                Bitmap.createBitmap(imgInput!!.cols(), imgInput!!.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(imgInput, bitmapInput)
            binding.imageViewInput.setImageBitmap(bitmapInput)

            val bitmapOutput: Bitmap =
                Bitmap.createBitmap(imgOutput!!.cols(), imgOutput!!.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(imgOutput, bitmapOutput)
            binding.imageViewOutput.setImageBitmap(bitmapOutput)
        }
    }

    private fun copyFile(filename: String) {
        val baseDir: String = Environment.getExternalStorageDirectory().path
        val pathDir = baseDir + File.separator.toString() + filename
        val assetManager: AssetManager = this.assets
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            Log.d(TAG, "copyFile :: 다음 경로로 파일복사 $pathDir")
            inputStream = assetManager.open(filename)
            outputStream = FileOutputStream(pathDir)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            inputStream = null
            outputStream.flush()
            outputStream.close()
            outputStream = null
        } catch (e: Exception) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 $e")
        }
    }

    private fun showDialogForPermission(msg: String) {
        if (isUpAndroid23()) {
            showAlert(getString(R.string.notice),
                msg,
                getString(R.string.ok),
                getString(R.string.cancel),
                { _, _ ->
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        WRITE_STORAGE_PERMISSION_REQUEST_CODE
                    )
                },
                { _, _ -> finish() }
            ).create().show()
        } else {
            toast("버전이 낮아 권한 필요 없음")
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String {
        val result: String
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {   // Source is drop box or other similar local file path
            result = contentURI.path ?: "no path"
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }

        return result
    }
}