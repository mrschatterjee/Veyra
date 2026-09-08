package com.veyra.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && VeyraReminder.isEnabled(context)) {
            VeyraReminder.schedule(context, VeyraReminder.hour(context), VeyraReminder.minute(context))
        }
    }
}
