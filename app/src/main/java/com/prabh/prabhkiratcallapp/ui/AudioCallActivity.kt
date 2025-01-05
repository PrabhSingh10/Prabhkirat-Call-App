package com.prabh.prabhkiratcallapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.prabh.prabhkiratcallapp.databinding.ActivityAudioCallBinding
import com.prabh.prabhkiratcallapp.utils.AudioState
import com.prabh.prabhkiratcallapp.utils.CallState
import com.prabh.prabhkiratcallapp.utils.cancelIncomingCallNotification
import com.prabh.prabhkiratcallapp.utils.handleMuteState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioCallActivity : AppCompatActivity() {

    private var binding: ActivityAudioCallBinding? = null

    private var muteState: AudioState = AudioState.UN_MUTE

    private var timerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAudioCallBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.controls?.spacer?.visibility = View.GONE
        binding?.controls?.cameraSwitch?.visibility = View.GONE

        cancelIncomingCallNotification(this@AudioCallActivity)

        lifecycleScope.launch(Dispatchers.Main) {
            binding?.callState?.visibility = View.VISIBLE
            handleCallState(CallState.RINGING)
            delay(3000)
            handleCallState(CallState.IN_CALL)
        }

        binding?.controls?.muteButton?.let { muteButton ->
            muteButton.setOnClickListener {
                muteState = handleMuteState(muteState, muteButton)
            }
        }

        binding?.controls?.endCall?.setOnClickListener {
            handleCallState(CallState.END_CALL)
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1000)
                this@AudioCallActivity.finish()
            }
        }

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
            }
            CallState.END_CALL -> {
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
    }
}