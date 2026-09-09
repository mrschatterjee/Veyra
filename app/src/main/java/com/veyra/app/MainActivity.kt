package com.veyra.app

import android.Manifest
import android.app.Activity
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Random

class MainActivity : Activity() {
    private val handler=Handler(Looper.getMainLooper()); private val store by lazy { VeyraStore(this) }
    private val createBackup=100; private val openBackup=101; private val notificationPermission=900
    private var pendingReminderHour=20; private var pendingReminderMinute=0
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContentView(OpeningView(this){showHome()})}
    override fun onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy()}
    override fun onResume(){super.onResume();if(VeyraReminder.isEnabled(this))VeyraReminder.schedule(this,VeyraReminder.hour(this),VeyraReminder.minute(this));NudgeScheduler.rescheduleAll(this)}
    fun textInput(title:String,hint:String,onSave:(String)->Unit){VeyraGlassDialog.showInput(this,title,hint,onSave=onSave)}
    private fun showHome(){val view=VeyraHomeView(this);setContentView(view);VeyraMotion.enter(view)}
    fun showSettings(){val view=SettingsView(this){showHome()};setContentView(view);VeyraMotion.enter(view,22f)}
    fun settingsAction(which:Int){when(which){0->showReminderPage();1->showNudges();2->showHabitsPage();3->showHabitHistory();4->showAchievements();5->createBackupFile();6->openBackupFile();7->showAbout();8->confirmReset()}}
    fun showNudges(){val view=NudgesView(this);setContentView(view);VeyraMotion.enter(view,18f)}
    fun showNudgeConfig(n:Nudge){val view=NudgeConfigView(this,n);setContentView(view);VeyraMotion.enter(view,18f)}
    private fun showHabitsPage(){val view=ManageHabitsView(this);setContentView(view);VeyraMotion.enter(view,18f)}
    fun addHabitFromPage(){textInput("Add a habit","e.g. Sleep"){name->val next=(store.habits().maxOfOrNull{it.id}?:0L)+1L;store.setHabits(store.habits()+VeyraStore.HabitRecord(next,name));showHabitsPage()}}
    fun habitActionsFromPage(id:Long){habitActions(id)}
    private fun habitActions(id:Long){val habit=store.habits().firstOrNull{it.id==id}?:return;VeyraGlassDialog.showChoice3(this,habit.name,"Choose what you want to do with this habit.","RENAME","REMIND","DELETE",{renameHabit(habit)},{showHabitReminder(habit.id)},{deleteHabit(habit)})}
    private fun showHabitReminder(habitId:Long){val view=HabitReminderView(this,habitId);setContentView(view);VeyraMotion.enter(view,18f)}
    private fun renameHabit(habit:VeyraStore.HabitRecord){textInput("Rename habit","Enter a new name"){name->store.renameHabit(habit.id,name);showHabitsPage()}}
    private fun deleteHabit(habit:VeyraStore.HabitRecord){VeyraGlassDialog.showConfirm(this,"Delete habit?","\"${habit.name}\" and its completion history will be removed from Veyra.","DELETE"){store.removeHabit(habit.id);showHabitsPage()}}
    private fun showAchievements(){val view=AchievementsView(this);setContentView(view);VeyraMotion.enter(view,18f)}
    private fun showHabitHistory(){val view=HabitHistoryView(this);setContentView(view);VeyraMotion.enter(view,18f)}
    private fun showReminderPage(){val habit=store.habits().firstOrNull();if(habit!=null)showHabitReminder(habit.id)else{val view=ReminderView(this);setContentView(view);VeyraMotion.enter(view,18f)}}
    private fun showAbout(){VeyraGlassDialog.showInfo(this,"Veyra","Build your universe.\n\nVersion 1.2\nPersonal life tracking with habits, goals, mood, journal, stats, XP and achievements.\n\nYour data stays on this device unless you choose to export a backup.")}
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==notificationPermission&&grantResults.firstOrNull()==PackageManager.PERMISSION_GRANTED)VeyraReminder.set(this,true,pendingReminderHour,pendingReminderMinute)}
    private fun createBackupFile(){startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{type="application/json";putExtra(Intent.EXTRA_TITLE,"veyra-backup.json")},createBackup)}
    private fun openBackupFile(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/json";addCategory(Intent.CATEGORY_OPENABLE)},openBackup)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data?.data==null)return;when(requestCode){createBackup->contentResolver.openOutputStream(data.data!!)?.use{it.write(store.exportJson().toByteArray())};openBackup->contentResolver.openInputStream(data.data!!)?.use{stream->val json=BufferedReader(InputStreamReader(stream)).readText();VeyraGlassDialog.showConfirm(this,"Restore backup?","This replaces your current Veyra data with the selected backup.","RESTORE"){try{store.importJson(json);showHome();VeyraGlassDialog.showInfo(this,"Restore complete","Your Veyra data has been restored successfully.")}catch(e:Exception){VeyraGlassDialog.showInfo(this,"Restore failed",e.message?:"The backup could not be restored.")}}}}}
    private fun confirmReset(){VeyraGlassDialog.showConfirm(this,"Reset Veyra?","All habits, progress, goals, mood, journal entries and achievements will be removed from this device.","RESET"){store.reset();VeyraReminder.set(this,false);showHome()}}
    private class OpeningView(context:Context,val done:()->Unit):View(context){private val paint=Paint(Paint.ANTI_ALIAS_FLAG);private val stars=List(70){Random(it*19L+4L).nextFloat() to Random(it*37L+9L).nextFloat()};private val handler=Handler(Looper.getMainLooper());private var phase=0;init{postDelayed({phase=1;invalidate()},900);postDelayed({phase=2;invalidate()},1800);postDelayed({done()},2700)};override fun onDetachedFromWindow(){handler.removeCallbacksAndMessages(null);super.onDetachedFromWindow()};override fun onDraw(c:Canvas){val w=width.toFloat();val h=height.toFloat();paint.shader=LinearGradient(0f,0f,w,h,Color.rgb(9,5,29),Color.rgb(65,31,122),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,paint);paint.shader=null;paint.color=Color.argb(110,200,170,255);stars.forEach{(x,y)->c.drawCircle(x*w,y*h,if(x>.7f)2.5f else 1.4f,paint)};paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create("sans",Typeface.BOLD);paint.color=Color.WHITE;paint.textSize=48f;val title=when(phase){0->"VEYRA";1->"GOOD MORNING";else->"BUILD YOUR UNIVERSE"};c.drawText(title,w/2f,h/2f,paint);paint.textSize=18f;paint.typeface=Typeface.create("sans",Typeface.NORMAL);paint.color=Color.argb(190,255,255,255);val sub=when(phase){0->"";1->"One small action at a time.";else->""};c.drawText(sub,w/2f,h/2f+34f,paint);paint.textAlign=Paint.Align.LEFT}}
}
