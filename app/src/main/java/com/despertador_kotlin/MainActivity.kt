package com.despertador_kotlin

import android.app.KeyguardManager
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.despertador_kotlin.ui.theme.Despertador_kotlinTheme
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import android.os.PowerManager
import android.os.PowerManager.WakeLock

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        // pantalla_bloqueada_start
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        // pantalla_bloqueada_end
        // bateria_optimizacion_ignore_start
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                // La aplicación no tiene permiso para ignorar las optimizaciones de batería
                // Solicitar al usuario que otorgue este permiso
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                // La aplicación ya tiene permiso para ignorar las optimizaciones de batería
                // No es necesario hacer nada
            }
        }
        // bateria_optimizacion_ignore_end
        // layout_start
        setContentView(R.layout.activity_main)
        // layout_end

        super.onCreate(savedInstanceState)
        val notificationListenerEnabled = Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        if (notificationListenerEnabled == null || !notificationListenerEnabled.contains(packageName)) {
            // El servicio de notificación no está habilitado, abre la configuración
            val intent2 = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent2)
        } else {
            // El servicio de notificación ya está habilitado, no hagas nada
        }


        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm_sound)
        // Iniciar servicio start
        val intent = Intent(this, NotificationListener::class.java)
        startService(intent)
        // Iniciar servicio end

        //mediaPlayer.start()
        //mediaPlayer.stop()
        /*
        mediaPlayer.stop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
         */
        setContent {
            Despertador_kotlinTheme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting("this is an alarm")
                        MyApp()
                    }
                }

            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
@Composable
fun MyApp() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
        /* Aquí va el código que se ejecutará al presionar el botón */
            NotificationListener.notificationPresent = false
        }) {
            Text("Apagar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Despertador_kotlinTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MyApp()
        }
        //Greeting("Android12")
    }
}

