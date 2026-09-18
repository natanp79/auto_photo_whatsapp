package com.example.auto_photo_whatsapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class AutomationForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "automation_channel"
        private const val NOTIFICATION_ID = 1001
        private const val INTERVAL_MS = 2 * 60 * 1000L
    }

    private val handler = Handler(Looper.getMainLooper())

    private val automationRunnable = object : Runnable {
        override fun run() {

            if (!AutomationState.running) {
                stopSelf()
                return
            }

            executeAutomationCycle()

            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        AutomationState.running = true

        handler.removeCallbacks(automationRunnable)
        handler.post(automationRunnable)

        return START_STICKY
    }

    private fun executeAutomationCycle() {

        /*
         * Próxima etapa:
         *
         * 1. Obter as 10 fotos mais recentes.
         * 2. Criar ACTION_SEND_MULTIPLE.
         * 3. Abrir o WhatsApp Business.
         * 4. AccessibilityService confirmar a tela correta.
         * 5. Executar o envio.
         */
    }

    override fun onDestroy() {
        handler.removeCallbacks(automationRunnable)

        AutomationState.running = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Automação de fotos",
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description =
                "Status da automação de envio de fotos."

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Automação de fotos ativa")
            .setContentText(
                "O aplicativo está executando a automação."
            )
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}