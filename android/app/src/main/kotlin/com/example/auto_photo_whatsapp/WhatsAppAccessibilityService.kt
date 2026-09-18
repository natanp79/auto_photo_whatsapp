package com.example.auto_photo_whatsapp

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class WhatsAppAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        /*
         * A lógica de automação será implementada na próxima etapa.
         *
         * Por enquanto, o serviço apenas recebe os eventos de
         * acessibilidade. Não executa nenhum clique automaticamente.
         */
    }

    override fun onInterrupt() {
        // Interrupção do serviço.
    }
}
