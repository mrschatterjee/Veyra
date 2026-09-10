package com.veyra.app

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                // Recalculate from the phone's current wall clock and timezone.
                // This prevents old epoch timestamps from surviving a manual time
                // change, automatic network time correction, or timezone change.
                HabitReminderScheduler.rescheduleAll(context)
                NudgeScheduler.rescheduleAll(context)
            }
        }
    }
}
