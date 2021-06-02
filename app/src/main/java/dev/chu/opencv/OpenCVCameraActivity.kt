package dev.chu.opencv

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dev.chu.opencv.databinding.ActivityOpencvCameraBinding
import dev.chu.opencv.util.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.*

class OpenCVCameraActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }

    private val binding by lazy { ActivityOpencvCameraBinding.inflate(layoutInflater) }
    private val native by lazy { NativeWrapper() }
    private val loaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> binding.activitySurfaceView.enableView()
                else -> super.onManagerConnected(status)
            }
        }
    }

    private var matInput: Mat? = null
    private var matResult: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        if (isUpAndroid30()) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(binding.root)

        with(binding.activitySurfaceView) {
            visibility = SurfaceView.VISIBLE
            setCvCameraViewListener(this@OpenCVCameraActivity)
            setCameraIndex(0)   // front-camera(1), hack-camera(0)
        }

        val result = "result ${native.sum(5, 11)}\n${native.stringFromJNI()}"
        binding.title.text = result
    }

    override fun onStart() {
        super.onStart()
        var isHavingPermission = true
        if (isUpAndroid23()) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
                isHavingPermission = false
            }
        }

        if (isHavingPermission) {
            onCameraPermissionGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, loaderCallback)
        } else {
            Log.d(TAG, "onResume :: OpenCV library found inside package. Using it")
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.activitySurfaceView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.activitySurfaceView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        matInput = inputFrame?.rgba()
        if (matResult == null && matInput != null) {
            matResult = Mat(matInput!!.rows(), matInput!!.cols(), matInput!!.type())
        }

        native.convertRGBtoGray(matInput!!.nativeObjAddr, matResult!!.nativeObjAddr)
        return matResult!!
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted()
        } else {
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가해~~")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    protected fun getCameraViewList(): List<CameraBridgeViewBase> {
        return Collections.singletonList(binding.activitySurfaceView)
    }

    protected fun onCameraPermissionGranted() {
        val cameraViews: List<CameraBridgeViewBase> = getCameraViewList()
        if (cameraViews.isEmpty()) {
            return
        }

        for (cameraBrideViewBase in cameraViews) {
            cameraBrideViewBase.setCameraPermissionGranted()
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
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                },
                { _, _ -> finish() }
            ).create().show()
        } else {
            toast("버전이 낮아 권한 필요 없음")
        }
    }
}
