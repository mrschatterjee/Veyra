package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object VeyraReminder {
    private const val REQUEST_CODE = 1001
    private const val PREFS = "veyra_reminders"
    private const val ENABLED = "enabled"
    private const val HOUR = "hour"
    private const val MINUTE = "minute"

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(ENABLED, false)
    fun hour(context: Context): Int = prefs(context).getInt(HOUR, 20)
    fun minute(context: Context): Int = prefs(context).getInt(MINUTE, 0)

    fun set(context: Context, enabled: Boolean, hour: Int = hour(context), minute: Int = minute(context)) {
        prefs(context).edit().putBoolean(ENABLED, enabled).putInt(HOUR, hour.coerceIn(0,23)).putInt(MINUTE, minute.coerceIn(0,59)).apply()
        if (enabled) schedule(context, hour, minute) else cancel(context)
    }

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0,23)); set(Calendar.MINUTE, minute.coerceIn(0,59)); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent(context))
    }

    fun cancel(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent(context))
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
