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
import android.content.pm.PackageManager
import java.io.IOException
import java.util.Calendar
class NotificationListener : NotificationListenerService() {
    private lateinit var mediaPlayer: MediaPlayer
    companion object {
        var notificationPresent:Boolean = false
        var NoHacerSonarMediaPlayerCheckbox:Boolean = false
        var hora_inicio: Int = 0
        var minuto_inicio: Int = 0
        var hora_final: Int = 0
        var minuto_final: Int = 0
        var service_iniciado: Int = 0
    }

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    fun saveTime(context: Context, hour1: Int, minute1: Int, hour2: Int, minute2: Int) {
        try {

            if (service_iniciado == 0) {
                return
            }
            val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

            with (sharedPref.edit()) {
                putInt("hour1", hour1)
                putInt("minute1", minute1)
                putInt("hour2", hour2)
                putInt("minute2", minute2)
                apply()
            }
        } catch (e: NullPointerException) {
            Log.e("NotificationListener", "NullPointerException: ", e)
        }
    }
    fun loadTime(context: Context) {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        hora_inicio = sharedPref.getInt("hour1", -1)
        minuto_inicio = sharedPref.getInt("minute1", -1)
        hora_final = sharedPref.getInt("hour2", -1)
        minuto_final = sharedPref.getInt("minute2", -1)
        Log.d("LoadingTimeAppx1", "Notification text: ${hora_inicio}, min ${minuto_inicio}," +
                " tiempo final ${hora_final} min ${minuto_final}")

    }


    fun setTime(context: Context, hour1: Int, minute1: Int, hour2: Int, minute2: Int) {
        hora_inicio = hour1
        minuto_inicio = minute1
        hora_final = hour2
        minuto_final = minute2
        if (service_iniciado == 1) {
            saveTime(context, hour1, minute1, hour2, minute2)
        }

        Log.d("UpdatingTimeAppx1", "Notification text: ${hora_inicio}, min ${minuto_inicio}," +
                " tiempo final ${hora_final} min ${minuto_final}")
    }
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
    fun tryReconnectService() {
        toggleNotificationListenerService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val componentName = ComponentName(this, NotificationListener::class.java)
            NotificationListenerService.requestRebind(componentName)
        }
    }

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationListener::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationListener::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
    fun startMediaPlayer() {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        //if (!NoHacerSonarMediaPlayerCheckbox && !mediaPlayer.isPlaying && (currentHour < 10 || currentHour > 14)) {

        //if (!NoHacerSonarMediaPlayerCheckbox && !mediaPlayer.isPlaying) {
        if (!NoHacerSonarMediaPlayerCheckbox) {
            //mediaPlayer.isLooping = true
            try {
                //mediaPlayer.prepare()
                if (mediaPlayer != null) {
                    mediaPlayer.release()
                }
                mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm_sound)

            } catch (e: Exception) {
                Log.e("NotificationListener_1", "Exception while releasing MediaPlayer", e)
            }
            setVolumeToMax()
            mediaPlayer.start()
        }
    }
    override fun onCreate() {
        super.onCreate()
        loadTime(this)
        service_iniciado = 1
        Log.d("NotificationListenerOnCreate", "Notification text: ")

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm_sound)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                tryReconnectService()
                if (notificationPresent && !mediaPlayer.isPlaying) {
                    startMediaPlayer()
                }
                checkActiveNotifications()
                //handler.postDelayed(this, 60000)
                // 60 segundos arriba
                handler.postDelayed(this, 35000)
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
        val packageName = sbn.packageName
        val appName = packageName.lowercase()

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.lowercase()
        val text_normal = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.lowercase()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.lowercase()
        val text = listOfNotNull("$appName (<-appIs-)", title, text_normal, subText).joinToString(" ")
        Log.d("NotificationListener", "Notification text: $text")
        //if (text != null && text.contains("pattern,", true)) {
        //if (text != null && text.contains("prog1", true)) {
        if (text.contains("prog1", ignoreCase = true)) {
            notificationPresent = true
            if (!mediaPlayer.isPlaying) {
                startMediaPlayer()
                /*
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
                // el codigo si funciona pero la app debe ser la ultima cosa abierta
                // antes de bloquear con clave el telefono
                */

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
            //notificationPresent = false
            if (mediaPlayer.isPlaying) {
                //mediaPlayer.stop()
            }
            //stopRunnable()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            //mediaPlayer.stop()
        }
        //mediaPlayer.release()
        //stopRunnable()
    }

}