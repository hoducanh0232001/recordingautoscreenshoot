package com.example.recordingautoscreenshoot

// ScreenCaptureReceiver.kt


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenCaptureReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == "com.example.screen_capture.ACTION_CAPTURE_SCREEN") {
            // Gọi service để chụp màn hình
            val serviceIntent = Intent(context, ScreenCaptureService::class.java)
            context.startService(serviceIntent)
        }
    }
}

