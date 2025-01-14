package com.prabh.prabhkiratcallapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.prabh.prabhkiratcallapp.databinding.MainActivityLayoutBinding
import com.prabh.prabhkiratcallapp.utils.CallType
import com.prabh.prabhkiratcallapp.utils.cancelIncomingCallNotification
import com.prabh.prabhkiratcallapp.utils.showCallNotification

class MainActivity : ComponentActivity() {

    private var binding: MainActivityLayoutBinding? = null

    private val cameraRequest : ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if (isGranted) {
                showCallNotification(this@MainActivity, CallType.VIDEO)
            } else {
                binding?.videoCallButton?.let {
                    Snackbar.make(it, "Needs Camera Permission To Initiate Video Call", Snackbar.LENGTH_SHORT)
                        .setAction("SETTINGS") {
                            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri =
                                Uri.fromParts("package", this.packageName, null)
                            settingsIntent.data = uri
                            cameraSettingsLaunch.launch(settingsIntent)
                        }
                        .setBackgroundTint(Color.DKGRAY)
                        .setTextColor(Color.WHITE)
                        .setActionTextColor(Color.WHITE)
                        .show()
                }
            }
        }

    private val notificationRequest : ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            binding?.permissionView?.root?.isVisible = isGranted.not()
        }

    private val settingsLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                binding?.permissionView?.root?.visibility = View.GONE
            }
        }

    private val cameraSettingsLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            cameraRequest.launch(Manifest.permission.CAMERA)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        binding = MainActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.audioCallButton?.setOnClickListener {
            showCallNotification(this@MainActivity, CallType.AUDIO)
        }

        binding?.videoCallButton?.setOnClickListener {
            cameraRequest.launch(Manifest.permission.CAMERA)
        }

        binding?.permissionView?.settingsButton?.setOnClickListener {
            val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri =
                Uri.fromParts("package", this.packageName, null)
            settingsIntent.data = uri
            settingsLaunch.launch(settingsIntent)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.getStringExtra("ACTION")
        if (action == "REJECT") {
            cancelIncomingCallNotification(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}