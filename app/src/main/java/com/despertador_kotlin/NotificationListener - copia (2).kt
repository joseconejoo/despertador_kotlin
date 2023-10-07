package com.despertador_kotlin
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.util.Log

class NotificationListenerRespaldo : NotificationListenerService() {
    private lateinit var mediaPlayer: MediaPlayer
    private var notificationPresent = true
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListenerOnCreate", "Notification text: ")

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm_sound)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (notificationPresent && !mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnable)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase()
        Log.d("NotificationListener", "Notification text: $text")
        if (text != null && text.contains("virtual", true)) {
            notificationPresent = true
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }
    }
    fun stopRunnable() {
        handler.removeCallbacks(runnable)
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        val notification = sbn.notification
        val extras = notification.extras
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase()
        if (text != null && text.contains("virtual", true)) {
            notificationPresent = false
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            //stopRunnable()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        //stopRunnable()
    }

}