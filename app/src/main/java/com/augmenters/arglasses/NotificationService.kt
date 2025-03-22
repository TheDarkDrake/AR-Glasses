package com.augmenters.arglasses

import android.Manifest
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresPermission

class NotificationService : NotificationListenerService() {
    private val btHelper = BluetoothHelper()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onListenerConnected() {
        btHelper.connectToDevice()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val prefs = getSharedPreferences("com.augmenters.arglasses", MODE_PRIVATE)
        val readNotifications = prefs.getBoolean("read_notifications", true)

        if (readNotifications) {
            val packageName = sbn.packageName
            val extras = sbn.notification.extras
            val title = extras.getString("android.title")
            val text = extras.getString("android.text")

            if (title != null && text != null) {
                val message = when (packageName) {
                    "com.whatsapp" -> "Notification: WhatsApp: $title: $text"
                    "com.android.mms" -> "Notification: SMS: $title: $text"
                    else -> "Notification: $title: $text"
                }
                btHelper.sendMessage(message)
            }
        }
    }

    override fun onListenerDisconnected() {
        btHelper.disconnect()
    }
}