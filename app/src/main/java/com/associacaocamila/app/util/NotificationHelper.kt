package com.associacaocamila.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.associacaocamila.app.MainActivity
import com.associacaocamila.app.R

object NotificationHelper {

    const val CHANNEL_ATTENDANCE_ID = "channel_attendance"
    const val CHANNEL_REMINDERS_ID = "channel_reminders"

    private const val CHANNEL_ATTENDANCE_NAME = "Avisos de Frequência"
    private const val CHANNEL_REMINDERS_NAME = "Lembretes de Atividades"

    private val COLOR_ORANGE = Color.parseColor("#F68A1E")
    private val COLOR_GREEN = Color.parseColor("#00A669")

    /**
     * Inicializa os canais de notificação no Android 8.0 (API 26) ou superior.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Canal 1: Avisos de Frequência (Importância Alta)
            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE_ID,
                CHANNEL_ATTENDANCE_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notificações de presença e registro de faltas nas oficinas."
                enableLights(true)
                lightColor = COLOR_ORANGE
                enableVibration(true)
            }

            // Canal 2: Lembretes de Atividades (Importância Padrão)
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Lembretes semanais de horários e encontros das oficinas."
                enableLights(true)
                lightColor = COLOR_GREEN
            }

            notificationManager.createNotificationChannel(attendanceChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    /**
     * Envia alerta local de falta para um participante de oficina.
     */
    fun sendAbsenceAlert(context: Context, studentName: String, workshopName: String) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ATTENDANCE_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Registro de Frequência - Associação Camila")
            .setContentText("Foi registrada falta para $studentName na oficina $workshopName.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Atenção: Foi registrada falta para o participante $studentName na oficina \"$workshopName\". O acompanhamento contínuo é fundamental para o fortalecimento do vínculo socioeducativo.")
            )
            .setColor(COLOR_ORANGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt(),
                    notification,
                )
            }
        } catch (_: SecurityException) {
            // Permissão negada pelo usuário
        }
    }

    /**
     * Envia lembrete com o horário da oficina.
     */
    fun sendWorkshopReminder(context: Context, workshopName: String, timeText: String) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Lembrete de Oficina - Associação Camila")
            .setContentText("Hoje tem $workshopName às $timeText. Não perca!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Hoje acontece a oficina \"$workshopName\" às $timeText. Esperamos você com muito carinho!"),
            )
            .setColor(COLOR_GREEN)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt(),
                    notification,
                )
            }
        } catch (_: SecurityException) {
            // Permissão negada pelo usuário
        }
    }

    /**
     * Verifica se o app possui permissão para disparar notificações.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
