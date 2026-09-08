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
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Random

class MainActivity : Activity() {
    private val handler=Handler(Looper.getMainLooper())
    private val store by lazy { VeyraStore(this) }
    private val createBackup=100
    private val openBackup=101
    private val notificationPermission=900
    private var pendingReminderHour=20
    private var pendingReminderMinute=0
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContentView(OpeningView(this){setContentView(VeyraHomeView(this))})}
    override fun onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy()}
    override fun onResume(){super.onResume();if(VeyraReminder.isEnabled(this))VeyraReminder.schedule(this,VeyraReminder.hour(this),VeyraReminder.minute(this))}
    fun textInput(title:String,hint:String,onSave:(String)->Unit){val input=EditText(this).apply{this.hint=hint;setSingleLine(false);minLines=2};AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save"){_,_->input.text.toString().trim().takeIf{it.isNotEmpty()}?.let(onSave)}.show()}
    fun showSettings(){
        val labels=arrayOf("Daily reminder","Manage habits","Backup data","Restore backup","About Veyra • v1.1","Reset Veyra data")
        AlertDialog.Builder(this).setTitle("Veyra settings").setItems(labels){_,which->when(which){0->reminderDialog();1->manageHabits();2->createBackupFile();3->openBackupFile();4->showAbout();5->confirmReset()}}.show()
    }
    private fun showAbout(){AlertDialog.Builder(this).setTitle("Veyra").setMessage("Build your universe.\n\nVersion 1.1\nPersonal life tracking with habits, goals, mood, journal, stats, XP and achievements.\n\nYour data stays on this device unless you choose to export a backup.").setPositiveButton("OK",null).show()}
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
    private fun deleteHabit(habit:VeyraStore.HabitRecord){AlertDialog.Builder(this).setTitle("Delete habit?").setMessage("Remove \"${habit.name}\" from your habit list? Its completion history will also be removed.").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->store.removeHabit(habit.id);refreshHome()}.show()}
    private fun refreshHome(){setContentView(VeyraHomeView(this))}
    private fun reminderDialog(){
        val picker=TimePicker(this).apply{setIs24HourView(true);hour=VeyraReminder.hour(this@MainActivity);minute=VeyraReminder.minute(this@MainActivity)}
        AlertDialog.Builder(this).setTitle("Daily reminder").setView(picker)
            .setNegativeButton("Turn off"){_,_->VeyraReminder.set(this,false)}
            .setPositiveButton(if(VeyraReminder.isEnabled(this))"Update" else "Enable"){_,_->enableReminder(picker.hour,picker.minute)}.show()
    }
    private fun enableReminder(hour:Int,minute:Int){
        pendingReminderHour=hour;pendingReminderMinute=minute
        if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),notificationPermission)}else{VeyraReminder.set(this,true,hour,minute)}
    }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==notificationPermission){if(grantResults.firstOrNull()==PackageManager.PERMISSION_GRANTED){VeyraReminder.set(this,true,pendingReminderHour,pendingReminderMinute)}else{VeyraReminder.set(this,false);showRestartMessage("Notifications are disabled, so Veyra's daily reminder was not enabled. You can allow notifications in Android Settings and try again.")}}}
    private fun createBackupFile(){startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{type="application/json";putExtra(Intent.EXTRA_TITLE,"veyra-backup.json")},createBackup)}
    private fun openBackupFile(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/json";addCategory(Intent.CATEGORY_OPENABLE)},openBackup)}
    @Deprecated("Legacy activity result API is used for broad Android compatibility")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK)return;val uri=data?.data?:return;try{if(requestCode==createBackup)contentResolver.openOutputStream(uri)?.use{it.write(store.exportJson().toByteArray())};else if(requestCode==openBackup){val raw=BufferedReader(InputStreamReader(contentResolver.openInputStream(uri))).use{it.readText()};store.importJson(raw);showRestartMessage("Backup restored. Reopen Veyra to refresh the dashboard.")}}catch(e:Exception){showRestartMessage("Backup failed: ${e.message?:"invalid file"}")}}
    private fun confirmReset(){AlertDialog.Builder(this).setTitle("Reset Veyra?").setMessage("This deletes habits, history, journals, XP and achievements from this device.").setNegativeButton("Cancel",null).setPositiveButton("Reset"){_,_->store.reset();VeyraReminder.set(this,false);setContentView(VeyraHomeView(this))}.show()}
    private fun showRestartMessage(message:String){AlertDialog.Builder(this).setTitle("Veyra").setMessage(message).setPositiveButton("OK",null).show()}
}

private class OpeningView(ctx:Context,private val onFinished:()->Unit):View(ctx){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG);private val stars=List(70){Random(it+91L).nextFloat() to Random(it+143L).nextFloat()};private val handler=Handler(Looper.getMainLooper());private var stage=0;private var alpha=0f;private var finished=false
    init{handler.postDelayed({stage=1;animateIn()},250)}
    private fun animateIn(){alpha=0f;val start=System.currentTimeMillis();fun tick(){alpha=((System.currentTimeMillis()-start).toFloat()/650L).coerceIn(0f,1f);invalidate();if(alpha<1f)handler.postDelayed({tick()},16L)else if(stage<3)handler.postDelayed({stage++;animateIn()},650L)else handler.postDelayed({finishOnce()},950L)};tick()}
    private fun finishOnce(){if(!finished){finished=true;onFinished()}}
    override fun onDetachedFromWindow(){handler.removeCallbacksAndMessages(null);super.onDetachedFromWindow()}
    override fun onTouchEvent(event:MotionEvent):Boolean{if(event.action==MotionEvent.ACTION_UP)finishOnce();return true}
    override fun onDraw(c:Canvas){val w=width.toFloat();val h=height.toFloat();paint.shader=LinearGradient(0f,0f,w,h,Color.rgb(8,5,26),Color.rgb(64,30,118),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,paint);paint.shader=null;paint.color=Color.argb(100,185,145,255);stars.forEach{(x,y)->c.drawCircle(x*w,y*h,1.2f,paint)};paint.color=Color.argb(45,190,150,255);paint.style=Paint.Style.STROKE;paint.strokeWidth=2f;val p=Path();p.moveTo(-20f,h*.52f);p.cubicTo(w*.25f,h*.38f,w*.62f,h*.65f,w+20f,h*.48f);c.drawPath(p,paint);paint.style=Paint.Style.FILL;val label=when(stage){1->"VEYRA";2->"GOOD MORNING";else->"Build your universe."};paint.color=Color.argb((alpha*255).toInt(),255,255,255);paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create("sans",if(stage==1)Typeface.BOLD else Typeface.NORMAL);paint.textSize=if(stage==1)42f else if(stage==2)24f else 19f;c.drawText(label,w/2f,h/2f+8f,paint);paint.textAlign=Paint.Align.LEFT}
}
