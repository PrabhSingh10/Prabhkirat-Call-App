package com.prabh.prabhkiratcallapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.prabh.prabhkiratcallapp.databinding.ActivityVideoCallBinding
import com.prabh.prabhkiratcallapp.utils.AudioState
import com.prabh.prabhkiratcallapp.utils.CallState
import com.prabh.prabhkiratcallapp.utils.cancelIncomingCallNotification
import com.prabh.prabhkiratcallapp.utils.handleMuteState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoCallActivity : AppCompatActivity() {

    private var binding: ActivityVideoCallBinding? = null

    private var cameraExecutor: ExecutorService? = null

    private var muteState: AudioState = AudioState.UN_MUTE

    private var showFrontCamera: Boolean = true

    private var timerJob: Job? = null

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
            handleCallState(CallState.END_CALL)
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1000)
                this@VideoCallActivity.finish()
            }
        }

        startCamera()

        lifecycleScope.launch(Dispatchers.Main) {
            binding?.callState?.visibility = View.VISIBLE
            handleCallState(CallState.RINGING)
            delay(3000)
            handleCallState(CallState.IN_CALL)
        }

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

    private fun handleCallState(callState: CallState) {
        when (callState) {
            CallState.RINGING -> {
                binding?.callState?.text = "Ringing..."
            }
            CallState.IN_CALL -> {
                var callDuration = 0L
                timerJob = lifecycleScope.launch(Dispatchers.Main) {
                    while (true) {
                        delay(1000)
                        callDuration++
                        binding?.callState?.text = inCallDurationFormat(callDuration)
                    }
                }
                binding?.lottieView?.playAnimation()
            }
            CallState.END_CALL -> {
                binding?.lottieView?.pauseAnimation()
                timerJob?.cancel()
                binding?.callState?.text = "Call Ended"
            }
        }
    }

    private fun inCallDurationFormat(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        timerJob = null
        binding = null
        cameraExecutor?.shutdown()
    }

}