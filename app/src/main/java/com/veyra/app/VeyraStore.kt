package com.veyra.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Dependency-free local store for Veyra data. */
class VeyraStore(context: Context) {
    private val prefs = context.getSharedPreferences("veyra_store", Context.MODE_PRIVATE)

    data class HabitRecord(val id: Long, val name: String)
    data class Achievement(val id: String, val title: String, val description: String, val unlocked: Boolean)

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
        items.forEach { item -> json.put(JSONObject().apply { put("id", item.id); put("name", item.name) }) }
        prefs.edit().putString("habits", json.toString()).apply()
    }

    fun isCompleted(habitId: Long, date: String = today()): Boolean = prefs.getBoolean("done_${date}_$habitId", false)

    fun setCompleted(habitId: Long, completed: Boolean, date: String = today()) {
        prefs.edit().putBoolean("done_${date}_$habitId", completed).apply()
    }

    fun completionDates(habitId: Long, days: Int): List<String> {
        val result = mutableListOf<String>()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        repeat(days.coerceAtLeast(0)) {
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
        while (habitIds.all { isCompleted(it, format.format(calendar.time)) }) {
            count++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return count
    }

    fun completedCount(days: Int, habitIds: List<Long>): Int {
        if (habitIds.isEmpty()) return 0
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        var total = 0
        repeat(days.coerceAtLeast(0)) {
            val date = format.format(calendar.time)
            total += habitIds.count { isCompleted(it, date) }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return total
    }

    fun mood(): Int = prefs.getInt("mood", 2).coerceIn(0, 3)
    fun setMood(value: Int) = prefs.edit().putInt("mood", value.coerceIn(0, 3)).apply()

    fun xp(): Int = prefs.getInt("xp", 0).coerceAtLeast(0)
    fun setXp(value: Int) = prefs.edit().putInt("xp", value.coerceAtLeast(0)).apply()

    fun goal(): String = prefs.getString("goal", "Build a better day") ?: "Build a better day"
    fun setGoal(value: String) = prefs.edit().putString("goal", value.trim().ifBlank { "Build a better day" }).apply()

    fun journal(date: String = today()): String = prefs.getString("journal_$date", "") ?: ""
    fun setJournal(value: String, date: String = today()) = prefs.edit().putString("journal_$date", value).apply()

    fun achievement(id: String): Boolean = prefs.getBoolean("achievement_$id", false)
    fun unlockAchievement(id: String): Boolean {
        if (achievement(id)) return false
        prefs.edit().putBoolean("achievement_$id", true).apply()
        return true
    }

    fun achievements(): List<Achievement> {
        val definitions = listOf(
            Triple("first_step", "First Step", "Complete your first habit."),
            Triple("week_warrior", "Week Warrior", "Reach a 7-day streak."),
            Triple("century", "Century", "Earn 100 XP."),
            Triple("journalist", "Thoughtful", "Write your first journal entry.")
        )
        return definitions.map { Achievement(it.first, it.second, it.third, achievement(it.first)) }
    }
}
