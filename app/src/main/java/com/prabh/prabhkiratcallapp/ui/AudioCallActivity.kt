package com.prabh.prabhkiratcallapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.prabh.prabhkiratcallapp.databinding.ActivityAudioCallBinding
import com.prabh.prabhkiratcallapp.utils.AudioState
import com.prabh.prabhkiratcallapp.utils.cancelIncomingCallNotification
import com.prabh.prabhkiratcallapp.utils.handleMuteState

class AudioCallActivity : AppCompatActivity() {

    private var binding: ActivityAudioCallBinding? = null

    private var muteState: AudioState = AudioState.UN_MUTE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAudioCallBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.controls?.spacer?.visibility = View.GONE
        binding?.controls?.cameraSwitch?.visibility = View.GONE

        cancelIncomingCallNotification(this@AudioCallActivity)

        binding?.controls?.muteButton?.let { muteButton ->
            muteButton.setOnClickListener {
                muteState = handleMuteState(muteState, muteButton)
            }
        }

        binding?.controls?.endCall?.setOnClickListener {
            this@AudioCallActivity.finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}