package com.veyra.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VeyraStore(context: Context) {
    private val prefs = context.getSharedPreferences("veyra_store", Context.MODE_PRIVATE)
    data class HabitRecord(val id: Long, val name: String)
    data class Achievement(val id: String, val title: String, val description: String, val unlocked: Boolean)
    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    fun habits(): List<HabitRecord> { val raw=prefs.getString("habits",null);if(raw==null||raw=="[]"){val defaults=listOf(HabitRecord(1,"Drink water"),HabitRecord(2,"Study 20 min"),HabitRecord(3,"Move 20 min"));if(raw==null)setHabits(defaults);return if(raw=="[]") emptyList() else defaults};val json=JSONArray(raw);return List(json.length()){i->val o=json.getJSONObject(i);HabitRecord(o.getLong("id"),o.getString("name"))} }
    fun setHabits(items:List<HabitRecord>){val json=JSONArray();items.forEach{json.put(JSONObject().put("id",it.id).put("name",it.name))};prefs.edit().putString("habits",json.toString()).apply()}
    fun renameHabit(id:Long,name:String){val cleaned=name.trim();if(cleaned.isBlank())return;setHabits(habits().map{if(it.id==id)HabitRecord(id,cleaned)else it})}
    fun removeHabit(id:Long){setHabits(habits().filterNot{it.id==id});val e=prefs.edit();val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();repeat(3650){e.remove("done_${f.format(c.time)}_$id");c.add(Calendar.DAY_OF_YEAR,-1)};e.apply()}
    fun isCompleted(id:Long,date:String=today()):Boolean=prefs.getBoolean("done_${date}_$id",false)
    fun setCompleted(id:Long,done:Boolean,date:String=today())=prefs.edit().putBoolean("done_${date}_$id",done).apply()
    fun completionDates(id:Long,days:Int):List<String>{val r=mutableListOf<String>();val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();repeat(days.coerceAtLeast(0)){r+=f.format(c.time);c.add(Calendar.DAY_OF_YEAR,-1)};return r.filter{isCompleted(id,it)}}
    fun streak(ids:List<Long>):Int{if(ids.isEmpty())return 0;val c=Calendar.getInstance();val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);var n=0;while(ids.all{isCompleted(it,f.format(c.time))}){n++;c.add(Calendar.DAY_OF_YEAR,-1)};return n}
    fun completedCount(days:Int,ids:List<Long>):Int{if(ids.isEmpty())return 0;val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();var n=0;repeat(days.coerceAtLeast(0)){val d=f.format(c.time);n+=ids.count{isCompleted(it,d)};c.add(Calendar.DAY_OF_YEAR,-1)};return n}
    fun mood():Int=prefs.getInt("mood",2).coerceIn(0,3);fun setMood(v:Int)=prefs.edit().putInt("mood",v.coerceIn(0,3)).apply()
    fun xp():Int=prefs.getInt("xp",0).coerceAtLeast(0);fun setXp(v:Int)=prefs.edit().putInt("xp",v.coerceAtLeast(0)).apply()
    fun goal():String=prefs.getString("goal","Build a better day")?:"Build a better day";fun setGoal(v:String)=prefs.edit().putString("goal",v.trim().ifBlank{"Build a better day"}).apply()
    fun journal(date:String=today()):String=prefs.getString("journal_$date","")?:"";fun setJournal(v:String,date:String=today())=prefs.edit().putString("journal_$date",v).apply()
    fun achievement(id:String)=prefs.getBoolean("achievement_$id",false)
    fun unlockAchievement(id:String):Boolean{if(achievement(id))return false;prefs.edit().putBoolean("achievement_$id",true).apply();return true}
    fun achievements():List<Achievement>{val d=listOf(Triple("first_step","First Step","Complete your first habit."),Triple("week_warrior","Week Warrior","Reach a 7-day streak."),Triple("century","Century","Earn 100 XP."),Triple("journalist","Thoughtful","Write your first journal entry."));return d.map{Achievement(it.first,it.second,it.third,achievement(it.first))}}
    fun exportJson():String{val root=JSONObject().put("schema",1).put("goal",goal()).put("mood",mood()).put("xp",xp());val hs=JSONArray();habits().forEach{hs.put(JSONObject().put("id",it.id).put("name",it.name))};root.put("habits",hs);val ids=habits().map{it.id};val f=SimpleDateFormat("yyyy-MM-dd",Locale.US);val c=Calendar.getInstance();val done=JSONArray();repeat(3650){val d=f.format(c.time);ids.filter{isCompleted(it,d)}.forEach{done.put(JSONObject().put("date",d).put("habitId",it))};c.add(Calendar.DAY_OF_YEAR,-1)};root.put("completed",done);val journals=JSONArray();val j=Calendar.getInstance();repeat(3650){val d=f.format(j.time);val body=journal(d);if(body.isNotBlank())journals.put(JSONObject().put("date",d).put("body",body));j.add(Calendar.DAY_OF_YEAR,-1)};root.put("journals",journals);val a=JSONArray();achievements().filter{it.unlocked}.forEach{a.put(it.id)};root.put("achievements",a);return root.toString(2)}
    fun importJson(raw:String){val root=JSONObject(raw);if(root.optInt("schema",0)!=1)throw IllegalArgumentException("Unsupported backup version");val importedHabits=root.optJSONArray("habits")?:throw IllegalArgumentException("Backup is missing habits");val validated=JSONArray();for(i in 0 until importedHabits.length()){val o=importedHabits.optJSONObject(i)?:throw IllegalArgumentException("Invalid habit data");val id=o.optLong("id",-1);val name=o.optString("name","").trim();if(id<1||name.isBlank())throw IllegalArgumentException("Invalid habit data");validated.put(JSONObject().put("id",id).put("name",name))};val e=prefs.edit().clear();e.putString("habits",validated.toString()).putString("goal",root.optString("goal","Build a better day")).putInt("mood",root.optInt("mood",2).coerceIn(0,3)).putInt("xp",root.optInt("xp",0).coerceAtLeast(0));root.optJSONArray("completed")?.let{for(i in 0 until it.length()){val o=it.getJSONObject(i);e.putBoolean("done_${o.getString("date")}_${o.getLong("habitId")}",true)}};root.optJSONArray("journals")?.let{for(i in 0 until it.length()){val o=it.getJSONObject(i);e.putString("journal_${o.getString("date")}",o.optString("body"))}};root.optJSONArray("achievements")?.let{for(i in 0 until it.length())e.putBoolean("achievement_${it.getString(i)}",true)};e.apply()}
    fun reset()=prefs.edit().clear().apply()
}
