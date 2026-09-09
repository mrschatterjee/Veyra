package com.veyra.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Nudge(val id: Long, val name: String, val intervalMinutes: Int, val startHour: Int, val endHour: Int, val enabled: Boolean)

class NudgeStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("veyra_nudges", Context.MODE_PRIVATE)
    fun all(): List<Nudge> = runCatching {
        val a = JSONArray(prefs.getString("items", "[]"))
        (0 until a.length()).map { i -> val x=a.getJSONObject(i); Nudge(x.getLong("id"),x.getString("name"),x.getInt("interval"),x.getInt("start"),x.getInt("end"),x.optBoolean("enabled",true)) }
    }.getOrDefault(emptyList())
    fun save(n: Nudge) {
        val a=JSONArray(); (all().filterNot{it.id==n.id}+n).forEach{a.put(JSONObject().apply{put("id",it.id);put("name",it.name);put("interval",it.intervalMinutes);put("start",it.startHour);put("end",it.endHour);put("enabled",it.enabled)})}
        prefs.edit().putString("items",a.toString()).apply()
        if(n.enabled) NudgeScheduler.schedule(context,n) else NudgeScheduler.cancel(context,n.id)
    }
    fun remove(id:Long){NudgeScheduler.cancel(context,id);val a=JSONArray();all().filterNot{it.id==id}.forEach{a.put(JSONObject().apply{put("id",it.id);put("name",it.name);put("interval",it.intervalMinutes);put("start",it.startHour);put("end",it.endHour);put("enabled",it.enabled)})};prefs.edit().putString("items",a.toString()).apply()}
    fun nextId()=(all().maxOfOrNull{it.id}?:0L)+1L
}
