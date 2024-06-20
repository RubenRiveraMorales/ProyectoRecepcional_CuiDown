package uv.tc.tesisapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import uv.tc.tesisapp.R
import uv.tc.tesisapp.VideoActivity
import uv.tc.tesisapp.pojo.Alarma


class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = 1
        const val ALARMA_EXTRA = "alarma_extra"
        const val CHANNEL_ID = "myChannel"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)
        Log.d("Notificaciones", "Creando canal de notificación...")
        val alarma = intent?.getSerializableExtra(ALARMA_EXTRA) as? Alarma
        if (alarma != null) {
            createSimpleNotification(context, alarma)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MySuperChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarma"
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createSimpleNotification(context: Context, alarma: Alarma) {
        val intent = Intent(context, VideoActivity::class.java)
        intent.putExtra("actividad", alarma.actividad)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_add_alert_24)
            .setContentTitle("Alarma para: ${alarma.actividad}")
            .setContentText("Hora: ${alarma.hora}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)//esta linea
            .build()


        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}