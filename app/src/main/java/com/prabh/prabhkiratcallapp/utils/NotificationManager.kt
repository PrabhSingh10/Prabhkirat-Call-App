package com.prabh.prabhkiratcallapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.prabh.prabhkiratcallapp.R
import com.prabh.prabhkiratcallapp.ui.AudioCallActivity
import com.prabh.prabhkiratcallapp.ui.MainActivity
import com.prabh.prabhkiratcallapp.ui.VideoCallActivity

const val CHANNEL_ID = "call_notification_channel"
const val NOTIFICATION_ID = 1

fun showCallNotification(context: Context, callType: CallType) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Call Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for ongoing calls"
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    val notificationManager = context.getSystemService(NotificationManager::class.java)

    val acceptIntent = when (callType) {
        CallType.VIDEO -> videoCallIntent(context)
        CallType.AUDIO -> audioCallIntent(context)
    }

    val acceptPendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 0, acceptIntent, PendingIntent.FLAG_IMMUTABLE
    )

    val rejectIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("ACTION", "REJECT")
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val rejectPendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 1, rejectIntent, PendingIntent.FLAG_IMMUTABLE
    )

    val customView = RemoteViews(context.packageName, R.layout.notification_ui)

    customView.setOnClickPendingIntent(R.id.accept_button, acceptPendingIntent)
    customView.setOnClickPendingIntent(R.id.reject_button, rejectPendingIntent)

    val collapsedCustomView = RemoteViews(context.packageName, R.layout.collapsed_notification_ui)

    collapsedCustomView.setOnClickPendingIntent(R.id.accept_button, acceptPendingIntent)
    collapsedCustomView.setOnClickPendingIntent(R.id.reject_button, rejectPendingIntent)

    val notification =  NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.rounded_call_24)
        .setCustomBigContentView(customView)
        .setCustomContentView(collapsedCustomView)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(Notification.CATEGORY_CALL)
        .build()

    notificationManager?.notify(NOTIFICATION_ID, notification)

}

private fun videoCallIntent(context: Context) = Intent(context, VideoCallActivity::class.java)

private fun audioCallIntent(context: Context) = Intent(context, AudioCallActivity::class.java)

fun cancelIncomingCallNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(NOTIFICATION_ID)
}