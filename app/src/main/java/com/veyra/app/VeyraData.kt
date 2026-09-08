package com.veyra.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Habit(val id: Long, var name: String, var done: Boolean = false)

data class DayRecord(val date: String, val completed: Int, val mood: Int, val xp: Int)

class VeyraData(context: Context) {
    private val prefs = context.getSharedPreferences("veyra_data", Context.MODE_PRIVATE)
    var goal: String
        get() = prefs.getString("goal", "Build a better day") ?: "Build a better day"
        set(value) = prefs.edit().putString("goal", value).apply()
    var mood: Int
        get() = prefs.getInt("mood", 2)
        set(value) = prefs.edit().putInt("mood", value).apply()
    var xp: Int
        get() = prefs.getInt("xp", 0)
        set(value) = prefs.edit().putInt("xp", value).apply()
    var streak: Int
        get() = prefs.getInt("streak", 0)
        set(value) = prefs.edit().putInt("streak", value).apply()

    fun loadHabits(): MutableList<Habit> {
        val a = JSONArray(prefs.getString("habits", "[]"))
        return MutableList(a.length()) { i -> val o=a.getJSONObject(i); Habit(o.getLong("id"),o.getString("name"),o.optBoolean("done")) }
    }
    fun saveHabits(items: List<Habit>) {
        val a=JSONArray();items.forEach{a.put(JSONObject().apply{put("id",it.id);put("name",it.name);put("done",it.done)})};prefs.edit().putString("habits",a.toString()).apply()
    }
    fun saveJournal(date: String, body: String) = prefs.edit().putString("journal_$date", body).apply()
    fun loadJournal(date: String) = prefs.getString("journal_$date", "") ?: ""
}
