# Aplicación de Alarma de Notificaciones

Esta aplicación está escrita en Kotlin y utiliza el framework Jetpack Compose para la interfaz de usuario. La aplicación está diseñada para detectar notificaciones con un texto específico y reproducir un sonido de alarma cuando se detecta dicha notificación.

## Características

- **Detección de Notificaciones**: Utiliza `NotificationListenerService` para detectar notificaciones con un texto específico. Cuando se detecta una notificación con el texto objetivo, la aplicación reproduce un sonido de alarma.

- **Manejo de Permisos**: La aplicación maneja los permisos necesarios como ignorar la optimización de la batería, mostrar sobre la pantalla bloqueada, y acceso a las notificaciones.

- **Reproducción de Sonido de Alarma**: La aplicación utiliza `MediaPlayer` para reproducir el sonido de alarma cuando se detecta la notificación objetivo. Controla el volumen del dispositivo para asegurarse que suene fuerte.

- **Interfaz de Usuario**: La aplicación tiene una interfaz simple con un botón para apagar la alarma y un checkbox para deshabilitar el sonido si se desea. Muestra un diálogo de confirmación al habilitar la opción de deshabilitar.

- **Estado Global**: Mantiene un estado global para saber si hay una notificación detectada actualmente y si el sonido está deshabilitado.

- **Verificación Periódica de Notificaciones**: Usa un `Handler` y `Runnable` para chequear notificaciones activas periódicamente.

- **Gestión del Ciclo de Vida**: La aplicación maneja el ciclo de vida de la aplicación y el servicio apropiadamente, deteniendo y liberando recursos cuando sea necesario.

- **Compatibilidad con Android**: Testeado en versión "Red Velvet Cake" y en teoria en Android versión Oreo y posteriores.

## Contribución

Si tienes alguna sugerencia o mejora, no dudes en abrir un PR o una nueva issue. Se esta bienvenida cualquier contribución que pueda mejorar la aplicación.


## Contacto

Si tienes alguna pregunta o necesitas ayuda, no dudes en contactar.

**Olwer Altuve**  
[LinkedIn](https://www.linkedin.com/in/olwer-altuve-santaromita-97824518a/) |
[Email](mailto:olwerjose33@hotmail.com)