package com.veyra.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class HabitReminder(val habitId: Long, val habitName: String, val hour: Int, val minute: Int, val enabled: Boolean)

object VeyraReminder {
    private const val PREFS = "veyra_reminders"
    private const val ITEMS = "items"
    private const val BASE_REQUEST = 1001

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun all(context: Context): List<HabitReminder> = runCatching {
        val a = JSONArray(prefs(context).getString(ITEMS, "[]"))
        (0 until a.length()).map { i -> val o=a.getJSONObject(i); HabitReminder(o.getLong("habitId"),o.getString("habitName"),o.getInt("hour"),o.getInt("minute"),o.optBoolean("enabled",true)) }
    }.getOrDefault(emptyList())
    fun forHabit(context: Context, habitId: Long): HabitReminder? = all(context).firstOrNull { it.habitId == habitId }
    fun isEnabled(context: Context): Boolean = all(context).any { it.enabled }
    fun hour(context: Context): Int = all(context).firstOrNull { it.enabled }?.hour ?: 20
    fun minute(context: Context): Int = all(context).firstOrNull { it.enabled }?.minute ?: 0
    fun habitId(context: Context): Long = all(context).firstOrNull { it.enabled }?.habitId ?: -1L
    fun habitName(context: Context): String = all(context).firstOrNull { it.enabled }?.habitName ?: ""

    fun set(context: Context, enabled: Boolean, hour: Int, minute: Int, habitId: Long, habitName: String) {
        val items = all(context).filterNot { it.habitId == habitId }.toMutableList()
        if (enabled) items += HabitReminder(habitId, habitName.trim(), hour.coerceIn(0,23), minute.coerceIn(0,59), true)
        write(context, items)
        cancel(context, habitId)
        if (enabled) schedule(context, items.last())
    }

    fun setLegacy(context: Context, enabled: Boolean, hour: Int = hour(context), minute: Int = minute(context)) = set(context, enabled, hour, minute, habitId(context), habitName(context).ifBlank { "Veyra" })

    fun schedule(context: Context, hour: Int, minute: Int) {
        val existing = all(context).firstOrNull { it.enabled }
        if (existing != null) schedule(context, existing.copy(hour=hour,minute=minute))
    }

    fun schedule(context: Context, reminder: HabitReminder) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent(context, reminder.habitId))
    }

    fun rescheduleAll(context: Context) { all(context).filter { it.enabled }.forEach { schedule(context,it) } }
    fun cancel(context: Context, habitId: Long) { (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent(context,habitId)) }
    fun cancel(context: Context) { all(context).forEach { cancel(context,it.habitId) } }

    private fun write(context: Context, items: List<HabitReminder>) {
        val a=JSONArray();items.forEach{a.put(JSONObject().put("habitId",it.habitId).put("habitName",it.habitName).put("hour",it.hour).put("minute",it.minute).put("enabled",it.enabled))}
        prefs(context).edit().putString(ITEMS,a.toString()).apply()
    }
    private fun pendingIntent(context: Context, habitId: Long): PendingIntent = PendingIntent.getBroadcast(context, BASE_REQUEST+(habitId%100000).toInt(), Intent(context, ReminderReceiver::class.java).putExtra("habit_id",habitId), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
