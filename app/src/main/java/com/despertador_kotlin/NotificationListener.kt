package com.despertador_kotlin
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.content.Context
import android.media.AudioManager
import android.util.Log

class NotificationListener : NotificationListenerService() {
    private lateinit var mediaPlayer: MediaPlayer
    private var notificationPresent = false
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    fun setVolumeToMax() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }
    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListenerOnCreate", "Notification text: ")

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm_sound)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (notificationPresent && !mediaPlayer.isPlaying) {
                    setVolumeToMax()
                    mediaPlayer.start()
                }
                //handler.postDelayed(this, 60000)
                // 60 segundos arriba
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("activeNotif_0", "Notification text: 0")
        // Maneja -las notificaciones existentes
        val activeNotifications = getActiveNotifications()
        Log.d("activeNotif_1.0", "Notification text: $activeNotifications")
        for (sbn in activeNotifications) {
            Log.d("activeNotif_1.1", "Notification text: $sbn")
            handleNotification(sbn)
        }
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase()
        Log.d("NotificationListener", "Notification text: $text")
        if (text != null && text.contains("pattern,", true)) {
            notificationPresent = true
            if (!mediaPlayer.isPlaying) {
                setVolumeToMax()

                mediaPlayer.start()
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        handleNotification(sbn)
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