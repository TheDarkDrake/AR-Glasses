package com.augmenters.arglasses

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SMSReceiver : BroadcastReceiver() {
    private val btHelper = BluetoothHelper()

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                val message = "SMS: ${smsMessage.displayOriginatingAddress}: ${smsMessage.messageBody}"
                btHelper.sendMessage(message)
                Log.d("SMSReceiver", message)
            }
        }
    }
}