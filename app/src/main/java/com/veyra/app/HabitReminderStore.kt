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
        val list = all().filterNot { it.habitId == item.habitId } + item.copy(habitName = item.habitName.trim())
        prefs.edit().putString("items", JSONArray().apply { list.forEach { put(JSONObject().apply { put("habitId", it.habitId); put("habitName", it.habitName); put("hour", it.hour.coerceIn(0,23)); put("minute", it.minute.coerceIn(0,59)); put("enabled", it.enabled) }) } }.toString()).apply()
        if (item.enabled) HabitReminderScheduler.schedule(context, item) else HabitReminderScheduler.cancel(context, item.habitId)
    }
    fun remove(habitId: Long) {
        val list = all().filterNot { it.habitId == habitId }
        prefs.edit().putString("items", JSONArray().apply { list.forEach { put(JSONObject().apply { put("habitId", it.habitId); put("habitName", it.habitName); put("hour", it.hour); put("minute", it.minute); put("enabled", it.enabled) }) } }.toString()).apply()
        HabitReminderScheduler.cancel(context, habitId)
    }
}
