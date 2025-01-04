package com.prabh.prabhkiratcallapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.prabh.prabhkiratcallapp.databinding.ActivityVideoCallBinding
import com.prabh.prabhkiratcallapp.utils.AudioState
import com.prabh.prabhkiratcallapp.utils.cancelIncomingCallNotification
import com.prabh.prabhkiratcallapp.utils.handleMuteState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoCallActivity : AppCompatActivity() {

    private var binding: ActivityVideoCallBinding? = null

    private var cameraExecutor: ExecutorService? = null

    private var muteState: AudioState = AudioState.UN_MUTE

    private var showFrontCamera: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        cancelIncomingCallNotification(this@VideoCallActivity)

        binding?.controls?.muteButton?.let { muteButton ->
            muteButton.setOnClickListener {
                muteState = handleMuteState(muteState, muteButton)
            }
        }

        binding?.controls?.cameraSwitch?.setOnClickListener {
            showFrontCamera = !showFrontCamera
            startCamera()
        }

        binding?.controls?.endCall?.setOnClickListener {
            this@VideoCallActivity.finish()
        }

        startCamera()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@VideoCallActivity)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding?.cameraView?.surfaceProvider
                }

            val cameraSelector = if (showFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner = this@VideoCallActivity,
                    cameraSelector = cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this@VideoCallActivity))

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        cameraExecutor?.shutdown()
    }

}