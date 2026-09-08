package com.veyra.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Small dependency-free local store for Veyra v1.1 data. */
class VeyraStore(context: Context) {
    private val prefs = context.getSharedPreferences("veyra_store", Context.MODE_PRIVATE)

    data class HabitRecord(val id: Long, val name: String)

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun habits(): List<HabitRecord> {
        val json = JSONArray(prefs.getString("habits", "[]"))
        return List(json.length()) { i ->
            val item = json.getJSONObject(i)
            HabitRecord(item.getLong("id"), item.getString("name"))
        }
    }

    fun setHabits(items: List<HabitRecord>) {
        val json = JSONArray()
        items.forEach { item ->
            json.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
            })
        }
        prefs.edit().putString("habits", json.toString()).apply()
    }

    fun isCompleted(habitId: Long, date: String = today()): Boolean =
        prefs.getBoolean("done_${date}_$habitId", false)

    fun setCompleted(habitId: Long, completed: Boolean, date: String = today()) {
        prefs.edit().putBoolean("done_${date}_$habitId", completed).apply()
    }

    fun completionDates(habitId: Long, days: Int): List<String> {
        val result = mutableListOf<String>()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        repeat(days) {
            result += format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return result.filter { isCompleted(habitId, it) }
    }

    fun streak(habitIds: List<Long>): Int {
        if (habitIds.isEmpty()) return 0
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var count = 0
        while (true) {
            val date = format.format(calendar.time)
            if (habitIds.all { isCompleted(it, date) }) {
                count++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        return count
    }

    fun completedCount(days: Int, habitIds: List<Long>): Int {
        if (habitIds.isEmpty()) return 0
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        var total = 0
        repeat(days) {
            val date = format.format(calendar.time)
            total += habitIds.count { isCompleted(it, date) }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return total
    }

    fun mood(): Int = prefs.getInt("mood", 2)
    fun setMood(value: Int) = prefs.edit().putInt("mood", value.coerceIn(0, 3)).apply()

    fun xp(): Int = prefs.getInt("xp", 0)
    fun setXp(value: Int) = prefs.edit().putInt("xp", value.coerceAtLeast(0)).apply()

    fun goal(): String = prefs.getString("goal", "Build a better day") ?: "Build a better day"
    fun setGoal(value: String) = prefs.edit().putString("goal", value).apply()

    fun journal(date: String = today()): String = prefs.getString("journal_$date", "") ?: ""
    fun setJournal(value: String, date: String = today()) = prefs.edit().putString("journal_$date", value).apply()
}
