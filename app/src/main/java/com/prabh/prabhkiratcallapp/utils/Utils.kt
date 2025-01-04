package com.prabh.prabhkiratcallapp.utils

import com.google.android.material.imageview.ShapeableImageView
import com.prabh.prabhkiratcallapp.R

enum class AudioState {
    MUTE,
    UN_MUTE
}

fun handleMuteState(muteState: AudioState, muteButton: ShapeableImageView): AudioState {
    return when (muteState) {
        AudioState.UN_MUTE -> {
            muteButton.setBackgroundColor(muteButton.context.resources.getColor(R.color.white))
            muteButton.drawable.setTint(muteButton.context.resources.getColor(R.color.black))
            AudioState.MUTE
        }
        AudioState.MUTE -> {
            muteButton.setBackgroundColor(muteButton.context.resources.getColor(R.color.black))
            muteButton.drawable.setTint(muteButton.context.resources.getColor(R.color.white))
            AudioState.UN_MUTE
        }
    }
}

enum class CallType {
    VIDEO,
    AUDIO
}