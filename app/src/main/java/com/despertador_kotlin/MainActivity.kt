package com.despertador_kotlin

import android.content.Intent
import android.media.MediaPlayer
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


class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
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
                    Greeting("Android")
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Despertador_kotlinTheme {
        Greeting("Android")
    }
}

