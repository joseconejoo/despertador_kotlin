package com.despertador_kotlin
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import android.provider.Settings
import android.app.KeyguardManager

class NotificationListener : NotificationListenerService() {
    private lateinit var mediaPlayer: MediaPlayer
    companion object {
        var notificationPresent = false
    }
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    fun setVolumeToMax() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }
    private fun checkActiveNotifications() {
        val activeNotifications = getActiveNotifications()
        for (sbn in activeNotifications) {
            handleNotification(sbn)
        }
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
                checkActiveNotifications()
                //handler.postDelayed(this, 60000)
                // 60 segundos arriba
                handler.postDelayed(this, 30000)
            }
        }
        handler.post(runnable)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("activeNotif_0", "Notification text: 0")
        // Maneja -las notificaciones existentes
        checkActiveNotifications()
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationListenerAccessGranted(ComponentName(this, NotificationListener::class.java))) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }
        val notification = sbn.notification
        val extras = notification.extras
        //val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase()
        //val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.lowercase()
        val text_normal = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.lowercase()
        val text = listOfNotNull(title, text_normal, subText).joinToString(" ")
        Log.d("NotificationListener", "Notification text: $text")
        //if (text != null && text.contains("pattern,", true)) {
        //if (text != null && text.contains("prog1", true)) {
        if (text.contains("pattern,", ignoreCase = true)) {
            notificationPresent = true
            if (!mediaPlayer.isPlaying) {
                setVolumeToMax()
                mediaPlayer.start()

                val notificationIntent = Intent(this, MainActivity::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "channel_id",
                        "Channel Name",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)

                    NotificationCompat.Builder(this, "channel_id")
                        .setContentTitle("Mi aplicación de despertador")
                        .setContentText("Reproduciendo música")
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build()
                } else {
                    NotificationCompat.Builder(this)
                        .setContentTitle("Mi aplicación de despertador")
                        .setContentText("Reproduciendo música")
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build()
                }

                // encender pantalla
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyApp::WakeLockTag"
                )
                wakeLock.acquire(10*60*1000L /*10 minutes*/)
                /*
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyApp:WakeLock"
                )
                wakeLock.acquire(3000) // Duración en milisegundos para mantener la pantalla encendida
                 */
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
        if (text != null && text.contains("prog1", true)) {
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