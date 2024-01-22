package com.example.recordingautoscreenshoot

// ScreenCaptureService.kt


import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class ScreenCaptureService : Service() {
    private var backgroundHandler: Handler? = null
    val flutterEngine by lazy { FlutterEngine(applicationContext) }
    private var methodChannel: MethodChannel? = null
    private val CHANNEL_ID = "ScreenCaptureServiceChannel"
    private val NOTIFICATION_ID = 123
    private val CHANNEL_NAME = "com.example.screen_capture"
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       // startForeground(NOTIFICATION_ID, createNotification())
        Log.e("AndroidNative", "$this::onStartCommand: intent= $intent, flags= $flags, startId: $startId")
        initMethodChannel()
        Thread.sleep(500L)
        startForeGroundInternal()
        startAlarm()

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForeGroundInternal(){
        createNotificationChannel()

        val builder = NotificationCompat.Builder(this,getString(R.string.my_channel_id))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("DucAnh Day")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notification = builder.build()
        startForeground(
            1,
            notification
        )

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val name: String = getString(R.string.app_name)
        val descriptionText: String = getString(R.string.my_channel_descripton)
        val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            getString(R.string.my_channel_id),
            name,
            importance).apply {
            description = descriptionText
        }
        //register the channel with the system
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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
    private fun captureScreen() {
        // Sử dụng backgroundHandler để thực hiện chụp ảnh trên một thread khác
        backgroundHandler?.post {
            // Lấy ảnh từ ImageReader
            val image = imageReader?.acquireLatestImage()

            if (image != null) {
                try {
                    // Lấy dữ liệu pixel từ ảnh
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    // Tạo Bitmap từ dữ liệu pixel
                    val bitmap = Bitmap.createBitmap(
                        image.width, image.height, Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    // Gửi bitmap về Flutter hoặc lưu vào thư mục ảnh
                    sendBitmapToFlutter(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // Đóng ảnh sau khi sử dụng
                    image.close()
                }
            }
        }
    }
    private fun initMethodChannel() {

        // Sử dụng flutterEngine.dartExecutor.binaryMessenger để có BinaryMessenger
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_NAME)
    }
    private fun sendBitmapToFlutter(bitmap: Bitmap) {
        // Chuyển đổi Bitmap thành mảng byte
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Gửi mảng byte về Flutter
        methodChannel?.invokeMethod("your_method_name", byteArray)
    }
    // Stop the service when it is no longer needed
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        // Clean up resources, stop any ongoing tasks
        super.onDestroy()
        stopSelf()
    }
}



