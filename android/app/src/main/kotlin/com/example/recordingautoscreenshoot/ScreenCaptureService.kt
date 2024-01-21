package com.example.recordingautoscreenshoot

// ScreenCaptureService.kt


import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ScreenCaptureService : Service() {

    private val CHANNEL_ID = "ScreenCaptureServiceChannel"
    private val NOTIFICATION_ID = 123
    private val ALARM_INTERVAL = 5 * 60 * 1000 // 5 minutes

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null

    inner class ScreenCaptureBinder : Binder() {
        fun getService(): ScreenCaptureService {
            return this@ScreenCaptureService
        }
    }

    private val binder = ScreenCaptureBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startAlarm()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Capture Service")
            .setContentText("Capturing Screenshots")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, ScreenCaptureReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent,
            PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            ALARM_INTERVAL.toLong(),
            pendingIntent
        )
    }

    // Rest of the screen capture logic goes here...

    // Stop the service when it is no longer needed
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        // Clean up resources, stop any ongoing tasks
        super.onDestroy()
    }
}



