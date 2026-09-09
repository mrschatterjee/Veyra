package com.veyra.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

class MainActivity : Activity() {
    private val handler=Handler(Looper.getMainLooper())
    private val store by lazy { VeyraStore(this) }
    private val createBackup=100
    private val openBackup=101
    private val notificationPermission=900
    private var pendingReminderHour=20
    private var pendingReminderMinute=0
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContentView(OpeningView(this){showHome()})}
    override fun onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy()}
    override fun onResume(){super.onResume();if(VeyraReminder.isEnabled(this))VeyraReminder.schedule(this,VeyraReminder.hour(this),VeyraReminder.minute(this))}
    fun textInput(title:String,hint:String,onSave:(String)->Unit){val input=EditText(this).apply{this.hint=hint;setSingleLine(false);minLines=2};AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->input.text.toString().trim().takeIf{it.isNotEmpty()}?.let(onSave)}.show()}
    private fun showHome(){val view=VeyraHomeView(this);setContentView(view);VeyraMotion.enter(view)}
    fun showSettings(){val view=SettingsView(this){showHome()};setContentView(view);VeyraMotion.enter(view,22f)}
    fun settingsAction(which:Int){when(which){0->reminderDialog();1->manageHabits();2->showHabitHistory();3->showAchievements();4->createBackupFile();5->openBackupFile();6->showAbout();7->confirmReset()}}
    private fun showAchievements(){
        val items=store.achievements().map{if(it.unlocked)"✓ ${it.title}\n${it.description}" else "○ ${it.title}\n${it.description}"}.toTypedArray()
        AlertDialog.Builder(this).setTitle("Achievements").setItems(items,null).setPositiveButton("OK",null).show()
    }
    private fun showHabitHistory(){
        val habits=store.habits()
        if(habits.isEmpty()){AlertDialog.Builder(this).setTitle("Habit history").setMessage("Add a habit first to build completion history.").setPositiveButton("OK",null).show();return}
        val date=Calendar.getInstance()
        val f=SimpleDateFormat("EEE, d MMM",Locale.getDefault())
        val lines=mutableListOf<String>()
        habits.forEach{habit->
            lines+="${habit.name}"
            val days=mutableListOf<String>()
            repeat(14){
                val key=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(date.time)
                days+="${if(store.isCompleted(habit.id,key))"✓" else "—"} ${f.format(date.time)}"
                date.add(Calendar.DAY_OF_YEAR,-1)
            }
            lines+=days.chunked(2).map{it.joinToString("    ")}.map{"  $it"}
            date.timeInMillis=Calendar.getInstance().timeInMillis
            lines+=""
        }
        AlertDialog.Builder(this).setTitle("Habit history • 14 days").setMessage(lines.joinToString("\n")).setPositiveButton("OK",null).show()
    }
    private fun showAbout(){AlertDialog.Builder(this).setTitle("Veyra").setMessage("Build your universe.\n\nVersion 1.2\nPersonal life tracking with habits, goals, mood, journal, stats, XP and achievements.\n\nYour data stays on this device unless you choose to export a backup.").setPositiveButton("OK",null).show()}
    private fun manageHabits(){
        val habits=store.habits()
        if(habits.isEmpty()){AlertDialog.Builder(this).setTitle("Manage habits").setMessage("You have no habits yet. Add one from the Habits tab.").setPositiveButton("OK",null).show();return}
        AlertDialog.Builder(this).setTitle("Manage habits").setItems(habits.map{"${it.name}\nTap to edit or delete"}.toTypedArray()){_,index->habitActions(habits[index].id)}.show()
    }
    private fun habitActions(id:Long){
        val habit=store.habits().firstOrNull{it.id==id}?:return
        AlertDialog.Builder(this).setTitle(habit.name).setItems(arrayOf("Rename","Delete")){_,which->if(which==0)renameHabit(habit)else deleteHabit(habit)}.show()
    }
    private fun renameHabit(habit:VeyraStore.HabitRecord){textInput("Rename habit",habit.name){store.renameHabit(habit.id,it);refreshHome()}}
    private fun deleteHabit(habit:VeyraStore.HabitRecord){AlertDialog.Builder(this).setTitle("Delete habit?").setMessage("This removes the habit from Veyra.").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->store.removeHabit(habit.id);refreshHome()}.show()}
    private fun refreshHome(){showHome()}
    private fun reminderDialog(){
        val enabled=VeyraReminder.isEnabled(this)
        val picker=TimePicker(this).apply{setIs24HourView(false);hour=VeyraReminder.hour(this@MainActivity);minute=VeyraReminder.minute(this@MainActivity)}
        AlertDialog.Builder(this).setTitle("Daily reminder").setMessage(if(enabled)"Reminder is on. Choose a time to update it."else"Choose a daily time for your Veyra reminder.").setView(picker).setNegativeButton(if(enabled)"Turn off" else "Cancel"){_,_->if(enabled)VeyraReminder.set(this,false)}.setPositiveButton("Save"){_,_->pendingReminderHour=picker.hour;pendingReminderMinute=picker.minute;if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED&&android.os.Build.VERSION.SDK_INT>=33){requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),notificationPermission)}else VeyraReminder.set(this,true,pendingReminderHour,pendingReminderMinute)}.show()
    }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==notificationPermission&&grantResults.firstOrNull()==PackageManager.PERMISSION_GRANTED)VeyraReminder.set(this,true,pendingReminderHour,pendingReminderMinute)}
    private fun createBackupFile(){startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{type="application/json";putExtra(Intent.EXTRA_TITLE,"veyra-backup.json")},createBackup)}
    private fun openBackupFile(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/json";addCategory(Intent.CATEGORY_OPENABLE)},openBackup)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data?.data==null)return;when(requestCode){createBackup->contentResolver.openOutputStream(data.data!!)?.use{it.write(store.exportJson().toByteArray())};openBackup->contentResolver.openInputStream(data.data!!)?.use{stream->val json=BufferedReader(InputStreamReader(stream)).readText();AlertDialog.Builder(this).setTitle("Restore backup?").setMessage("This replaces your current Veyra data with the selected backup.").setNegativeButton("Cancel",null).setPositiveButton("Restore"){_,_->try{store.importJson(json);refreshHome();AlertDialog.Builder(this).setTitle("Restore complete").setMessage("Your Veyra data has been restored.").setPositiveButton("OK",null).show()}catch(e:Exception){AlertDialog.Builder(this).setTitle("Restore failed").setMessage(e.message?:("The backup could not be restored.")).setPositiveButton("OK",null).show()}}.show()}}
    }
    private fun confirmReset(){AlertDialog.Builder(this).setTitle("Reset Veyra?").setMessage("All habits, progress, goals, mood, journal entries and achievements will be removed from this device.").setNegativeButton("Cancel",null).setPositiveButton("Reset"){_,_->store.reset();VeyraReminder.set(this,false);refreshHome()}.show()}

    private class OpeningView(context:Context,val done:()->Unit):View(context){
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG);private val stars=List(70){Random(it*19L+4L).nextFloat() to Random(it*37L+9L).nextFloat()};private val handler=Handler(Looper.getMainLooper());private var phase=0
        init{postDelayed({phase=1;invalidate()},900);postDelayed({phase=2;invalidate()},1800);postDelayed({done()},2700)}
        override fun onDetachedFromWindow(){handler.removeCallbacksAndMessages(null);super.onDetachedFromWindow()}
        override fun onDraw(c:Canvas){val w=width.toFloat();val h=height.toFloat();paint.shader=LinearGradient(0f,0f,w,h,Color.rgb(9,5,29),Color.rgb(65,31,122),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,paint);paint.shader=null;paint.color=Color.argb(110,200,170,255);stars.forEach{(x,y)->c.drawCircle(x*w,y*h,if(x>.7f)2.5f else 1.4f,paint)};paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create("sans",Typeface.BOLD);paint.color=Color.WHITE;paint.textSize=48f;val title=when(phase){0->"VEYRA";1->"GOOD MORNING";else->"BUILD YOUR UNIVERSE"};c.drawText(title,w/2f,h/2f,paint);paint.textSize=18f;paint.typeface=Typeface.create("sans",Typeface.NORMAL);paint.color=Color.argb(190,255,255,255);val sub=when(phase){0->"";1->"One small action at a time.";else->""};c.drawText(sub,w/2f,h/2f+34f,paint);paint.textAlign=Paint.Align.LEFT}
    }
}
