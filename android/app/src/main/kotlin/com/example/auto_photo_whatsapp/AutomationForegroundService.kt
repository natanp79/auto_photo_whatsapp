package com.example.auto_photo_whatsapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log

class AutomationForegroundService : Service() {

    companion object {
        private const val TAG = "AutoPhotoWhatsApp"

        private const val CHANNEL_ID = "automation_channel"
        private const val NOTIFICATION_ID = 1001

        private const val INTERVAL_MS = 2 * 60 * 1000L

        /*
         * Pacote oficial do WhatsApp Business no Android.
         *
         * Vamos validar a instalação no dispositivo durante o teste.
         */
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

        private const val PHOTO_COUNT = 10
    }

    private val handler = Handler(Looper.getMainLooper())

    private var cycleRunning = false

    private val automationRunnable = object : Runnable {

        override fun run() {

            if (!AutomationState.running) {
                stopAutomationService()
                return
            }

            executeAutomationCycle()

            handler.postDelayed(
                this,
                INTERVAL_MS
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        Log.d(TAG, "Foreground Service criado.")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        AutomationState.running = true

        handler.removeCallbacks(automationRunnable)

        /*
         * Executa o primeiro ciclo imediatamente.
         * Depois continua a cada 2 minutos.
         */
        handler.post(automationRunnable)

        Log.d(TAG, "Automação iniciada.")

        return START_STICKY
    }

    private fun executeAutomationCycle() {

        if (cycleRunning) {
            Log.d(TAG, "Ciclo anterior ainda está executando.")
            return
        }

        cycleRunning = true

        try {

            Log.d(TAG, "Iniciando ciclo de automação.")

            val photos = getLatestPhotos()

            if (photos.isEmpty()) {
                Log.d(TAG, "Nenhuma foto encontrada.")
                return
            }

            Log.d(
                TAG,
                "Fotos encontradas: ${photos.size}"
            )

            openWhatsAppBusiness(photos)

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Erro durante o ciclo de automação.",
                exception
            )

        } finally {

            cycleRunning = false
        }
    }

    /**
     * Obtém as fotos mais recentes diretamente do MediaStore.
     *
     * A lista é ordenada da mais recente para a mais antiga.
     */
    private fun getLatestPhotos(): ArrayList<Uri> {

        val photos = ArrayList<Uri>()

        val collection: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder =
            "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {

            val idColumn =
                it.getColumnIndexOrThrow(
                    MediaStore.Images.Media._ID
                )

            var count = 0

            while (
                it.moveToNext() &&
                count < PHOTO_COUNT
            ) {

                val id = it.getLong(idColumn)

                val photoUri =
                    Uri.withAppendedPath(
                        collection,
                        id.toString()
                    )

                photos.add(photoUri)

                count++
            }
        }

        return photos
    }

    /**
     * Abre o WhatsApp Business com as 10 fotos
     * selecionadas em uma única ação de compartilhamento.
     */
    private fun openWhatsAppBusiness(
        photos: ArrayList<Uri>
    ) {

        val packageManager = packageManager

        val whatsappIntent = packageManager.getLaunchIntentForPackage(
            WHATSAPP_BUSINESS_PACKAGE
        )

        if (whatsappIntent == null) {

            Log.e(
                TAG,
                "WhatsApp Business não está instalado."
            )

            return
        }

        val shareIntent = Intent(
            Intent.ACTION_SEND_MULTIPLE
        ).apply {

            type = "image/*"

            putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                photos
            )

            setPackage(
                WHATSAPP_BUSINESS_PACKAGE
            )

            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            addFlags(
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            /*
             * Permite que o aplicativo destinatário receba
             * a permissão temporária para acessar as imagens.
             */
            clipData = createClipData(photos)
        }

        Log.d(
            TAG,
            "Abrindo WhatsApp Business com ${photos.size} fotos."
        )

        startActivity(shareIntent)
    }

    /**
     * Cria o ClipData contendo todas as imagens.
     * Isso ajuda a garantir a concessão das URIs
     * para o aplicativo destinatário.
     */
    private fun createClipData(
        photos: ArrayList<Uri>
    ): android.content.ClipData {

        val firstUri = photos.first()

        val clipData = android.content.ClipData.newRawUri(
            "photos",
            firstUri
        )

        for (index in 1 until photos.size) {

            clipData.addItem(
                android.content.ClipData.Item(
                    photos[index]
                )
            )
        }

        return clipData
    }

    private fun stopAutomationService() {

        AutomationState.running = false

        handler.removeCallbacks(
            automationRunnable
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(
                STOP_FOREGROUND_REMOVE
            )
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        stopSelf()
    }

    override fun onDestroy() {

        handler.removeCallbacks(
            automationRunnable
        )

        cycleRunning = false

        AutomationState.running = false

        Log.d(
            TAG,
            "Foreground Service encerrado."
        )

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
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
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createNotification(): Notification {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        return Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "Automação de fotos ativa"
            )
            .setContentText(
                "A automação está em execução."
            )
            .setSmallIcon(
                android.R.drawable.ic_menu_send
            )
            .setContentIntent(
                pendingIntent
            )
            .setOngoing(true)
            .build()
    }
}