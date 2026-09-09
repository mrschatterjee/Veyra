package com.veyra.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HabitReminder(val habitId: Long, val habitName: String, val hour: Int, val minute: Int, val enabled: Boolean)

class HabitReminderStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("veyra_habit_reminders", Context.MODE_PRIVATE)
    fun all(): List<HabitReminder> = runCatching {
        val a = JSONArray(prefs.getString("items", "[]"))
        List(a.length()) { i ->
            val o = a.getJSONObject(i)
            HabitReminder(o.getLong("habitId"), o.getString("habitName"), o.getInt("hour"), o.getInt("minute"), o.optBoolean("enabled", true))
        }
    }.getOrDefault(emptyList())
    fun get(habitId: Long): HabitReminder? = all().firstOrNull { it.habitId == habitId }
    fun save(item: HabitReminder) {
        val clean = item.copy(habitName = item.habitName.trim(), hour = item.hour.coerceIn(0,23), minute = item.minute.coerceIn(0,59))
        val list = all().filterNot { it.habitId == clean.habitId } + clean
        write(list)
        if (clean.enabled) HabitReminderScheduler.schedule(context, clean) else HabitReminderScheduler.cancel(context, clean.habitId)
    }
    fun remove(habitId: Long) {
        HabitReminderScheduler.cancel(context, habitId)
        write(all().filterNot { it.habitId == habitId })
    }
    fun clear() {
        all().forEach { HabitReminderScheduler.cancel(context, it.habitId) }
        prefs.edit().clear().apply()
    }
    private fun write(list: List<HabitReminder>) {
        prefs.edit().putString("items", JSONArray().apply {
            list.forEach { put(JSONObject().apply { put("habitId", it.habitId); put("habitName", it.habitName); put("hour", it.hour); put("minute", it.minute); put("enabled", it.enabled) }) }
        }.toString()).apply()
    }
}
