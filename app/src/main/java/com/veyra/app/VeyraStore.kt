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
        val raw = prefs.getString("habits", null)
        if (raw == null) {
            val defaults = listOf(HabitRecord(1, "Drink water"), HabitRecord(2, "Study 20 min"), HabitRecord(3, "Move 20 min"))
            setHabits(defaults); return defaults
        }
        val json = JSONArray(raw)
        return List(json.length()) { i -> val item=json.getJSONObject(i); HabitRecord(item.getLong("id"),item.getString("name")) }
    }
    fun setHabits(items: List<HabitRecord>) { val json=JSONArray();items.forEach{json.put(JSONObject().apply{put("id",it.id);put("name",it.name)})};prefs.edit().putString("habits",json.toString()).apply() }
    fun isCompleted(habitId:Long,date:String=today()):Boolean=prefs.getBoolean("done_${date}_$habitId",false)
    fun setCompleted(habitId:Long,completed:Boolean,date:String=today()){prefs.edit().putBoolean("done_${date}_$habitId",completed).apply()}
    fun completionDates(habitId:Long,days:Int):List<String>{val result=mutableListOf<String>();val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();repeat(days.coerceAtLeast(0)){result+=f.format(c.time);c.add(Calendar.DAY_OF_YEAR,-1)};return result.filter{isCompleted(habitId,it)}}
    fun streak(ids:List<Long>):Int{if(ids.isEmpty())return 0;val c=Calendar.getInstance();val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);var n=0;while(ids.all{isCompleted(it,f.format(c.time))}){n++;c.add(Calendar.DAY_OF_YEAR,-1)};return n}
    fun completedCount(days:Int,ids:List<Long>):Int{if(ids.isEmpty())return 0;val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();var total=0;repeat(days.coerceAtLeast(0)){val d=f.format(c.time);total+=ids.count{isCompleted(it,d)};c.add(Calendar.DAY_OF_YEAR,-1)};return total}
    fun mood():Int=prefs.getInt("mood",2).coerceIn(0,3)
    fun setMood(v:Int)=prefs.edit().putInt("mood",v.coerceIn(0,3)).apply()
    fun xp():Int=prefs.getInt("xp",0).coerceAtLeast(0)
    fun setXp(v:Int)=prefs.edit().putInt("xp",v.coerceAtLeast(0)).apply()
    fun goal():String=prefs.getString("goal","Build a better day")?:"Build a better day"
    fun setGoal(v:String)=prefs.edit().putString("goal",v.trim().ifBlank{"Build a better day"}).apply()
    fun journal(date:String=today()):String=prefs.getString("journal_$date","")?:""
    fun setJournal(v:String,date:String=today())=prefs.edit().putString("journal_$date",v).apply()
    fun achievement(id:String):Boolean=prefs.getBoolean("achievement_$id",false)
    fun unlockAchievement(id:String):Boolean{if(achievement(id))return false;prefs.edit().putBoolean("achievement_$id",true).apply();return true}
    fun achievements():List<Achievement>{val d=listOf(Triple("first_step","First Step","Complete your first habit."),Triple("week_warrior","Week Warrior","Reach a 7-day streak."),Triple("century","Century","Earn 100 XP."),Triple("journalist","Thoughtful","Write your first journal entry."));return d.map{Achievement(it.first,it.second,it.third,achievement(it.first))}}

    fun exportJson(): String {
        val root=JSONObject().put("schema",1).put("goal",goal()).put("mood",mood()).put("xp",xp())
        val hs=JSONArray();habits().forEach{hs.put(JSONObject().put("id",it.id).put("name",it.name))};root.put("habits",hs)
        val completed=JSONArray();val ids=habits().map{it.id};val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();repeat(3650){val d=f.format(c.time);ids.filter{isCompleted(it,d)}.forEach{completed.put(JSONObject().put("date",d).put("habitId",it))};c.add(Calendar.DAY_OF_YEAR,-1)};root.put("completed",completed)
        val journals=JSONArray();val start=Calendar.getInstance();repeat(3650){val d=f.format(start.time);val body=journal(d);if(body.isNotBlank())journals.put(JSONObject().put("date",d).put("body",body));start.add(Calendar.DAY_OF_YEAR,-1)};root.put("journals",journals)
        val achievements=JSONArray();achievements().filter{it.unlocked}.forEach{achievements.put(it.id)};root.put("achievements",achievements);return root.toString(2)
    }
    fun importJson(raw:String){val root=JSONObject(raw);if(root.optInt("schema",0)!=1)throw IllegalArgumentException("Unsupported backup version");val edit=prefs.edit().clear();edit.putString("habits",root.optJSONArray("habits")?.toString()? : "[]");edit.putString("goal",root.optString("goal","Build a better day"));edit.putInt("mood",root.optInt("mood",2).coerceIn(0,3));edit.putInt("xp",root.optInt("xp",0).coerceAtLeast(0));root.optJSONArray("completed")?.let{for(i in 0 until it.length()){val o=it.getJSONObject(i);edit.putBoolean("done_${o.getString("date")}_${o.getLong("habitId")}",true)}};root.optJSONArray("journals")?.let{for(i in 0 until it.length()){val o=it.getJSONObject(i);edit.putString("journal_${o.getString("date")}",o.optString("body"))}};root.optJSONArray("achievements")?.let{for(i in 0 until it.length())edit.putBoolean("achievement_${it.getString(i)}",true)};edit.apply()}
    fun reset(){prefs.edit().clear().apply()}
}
