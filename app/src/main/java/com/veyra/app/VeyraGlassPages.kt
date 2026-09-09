package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private abstract class VeyraGlassPage(private val activity: MainActivity) : View(activity) {
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val bgStars = listOf(0.08f to 0.12f,0.18f to 0.33f,0.31f to 0.08f,0.47f to 0.25f,0.63f to 0.11f,0.78f to 0.29f,0.9f to 0.08f,0.14f to 0.74f,0.38f to 0.88f,0.66f to 0.7f,0.84f to 0.9f)
    protected fun base(c: Canvas, title: String, subtitle: String) {
        val w=width.toFloat(); val h=height.toFloat()
        paint.shader=LinearGradient(0f,0f,w,h,Color.rgb(7,4,24),Color.rgb(43,20,86),Shader.TileMode.CLAMP); c.drawRect(0f,0f,w,h,paint); paint.shader=null
        paint.color=Color.argb(100,184,139,255); bgStars.forEach{c.drawCircle(it.first*w,it.second*h,if(it.first>.7f)2.5f else 1.5f,paint)}
        paint.color=Color.argb(55,150,90,255); c.drawCircle(w*0.92f,h*0.18f,w*.25f,paint)
        paint.color=Color.argb(42,100,80,255); c.drawCircle(w*0.08f,h*.86f,w*.3f,paint)
        paint.textAlign=Paint.Align.LEFT; paint.typeface=Typeface.create("sans",Typeface.BOLD); paint.textSize=31f; paint.color=Color.WHITE; c.drawText(title,28f,64f,paint)
        paint.typeface=Typeface.create("sans",Typeface.NORMAL); paint.textSize=14f; paint.color=Color.argb(185,235,225,255); c.drawText(subtitle,28f,91f,paint)
        paint.textSize=28f; paint.color=Color.WHITE; c.drawText("‹",18f,64f,paint)
    }
    protected fun glass(c:Canvas,l:Float,t:Float,r:Float,b:Float,alpha:Int=48){paint.shader=LinearGradient(l,t,r,b,Color.argb(alpha,145,115,220),Color.argb(20,80,55,150),Shader.TileMode.CLAMP);c.drawRoundRect(RectF(l,t,r,b),22f,22f,paint);paint.shader=null;paint.style=Paint.Style.STROKE;paint.strokeWidth=1.5f;paint.color=Color.argb(75,205,185,255);c.drawRoundRect(RectF(l,t,r,b),22f,22f,paint);paint.style=Paint.Style.FILL}
    protected fun button(c:Canvas,label:String,l:Float,t:Float,r:Float,b:Float){glass(c,l,t,r,b,75);paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create("sans",Typeface.BOLD);paint.textSize=14f;paint.color=Color.WHITE;c.drawText(label,(l+r)/2f,(t+b)/2f+5f,paint);paint.textAlign=Paint.Align.LEFT}
    override fun onTouchEvent(e:MotionEvent):Boolean {if(e.action==MotionEvent.ACTION_UP && e.y<115f && e.x<85f){activity.showSettings();return true};return handleTap(e)}
    protected abstract fun handleTap(e:MotionEvent):Boolean
}

class HabitHistoryView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store=VeyraStore(activity)
    override fun onDraw(c:Canvas){base(c,"Habit History","Your consistency, visualized over the last 14 days.");val habits=store.habits();val top=125f;val gap=12f
        glass(c,20f,top,width-20f,top+82f,65);val ids=habits.map{it.id};val done=store.completedCount(14,ids);paint.textSize=25f;paint.typeface=Typeface.DEFAULT_BOLD;paint.color=Color.WHITE;c.drawText("$done",38f,top+36f,paint);paint.textSize=12f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(185,235,225,255);c.drawText("actions completed",38f,top+58f,paint)
        paint.textSize=25f;paint.typeface=Typeface.DEFAULT_BOLD;c.drawText("${habits.size}",width/2f-30f,top+36f,paint);paint.textSize=12f;paint.typeface=Typeface.DEFAULT;c.drawText("active habits",width/2f-30f,top+58f,paint)
        val streak=store.streak(ids);paint.textSize=25f;paint.typeface=Typeface.DEFAULT_BOLD;c.drawText("$streak",width-125f,top+36f,paint);paint.textSize=12f;paint.typeface=Typeface.DEFAULT;c.drawText("day streak",width-125f,top+58f,paint)
        var y=top+96f;val f=SimpleDateFormat("d",Locale.US);val df=SimpleDateFormat("EEE",Locale.US);val date=Calendar.getInstance()
        habits.forEachIndexed{index,h->val cardH=116f;glass(c,20f,y,width-20f,y+cardH,48);paint.textSize=17f;paint.typeface=Typeface.DEFAULT_BOLD;paint.color=Color.WHITE;c.drawText(h.name,38f,y+30f,paint);val count=store.completionDates(h.id,14).size;paint.textSize=12f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(185,225,215,255);c.drawText("$count / 14 completed",38f,y+51f,paint)
            date.time=Calendar.getInstance().time;for(i in 0 until 14){val x=40f+i*((width-80f)/13f);val key=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(date.time);val ok=store.isCompleted(h.id,key);paint.style=Paint.Style.FILL;paint.color=if(ok)Color.rgb(147,102,245) else Color.argb(45,255,255,255);c.drawCircle(x,y+78f,8f,paint);paint.style=Paint.Style.STROKE;paint.strokeWidth=1f;paint.color=Color.argb(90,210,195,255);c.drawCircle(x,y+78f,8f,paint);paint.style=Paint.Style.FILL;paint.textAlign=Paint.Align.CENTER;paint.textSize=8f;paint.color=Color.argb(190,240,235,255);c.drawText(f.format(date.time),x,y+101f,paint);paint.textSize=7f;c.drawText(df.format(date.time),x,y+112f,paint);paint.textAlign=Paint.Align.LEFT;date.add(Calendar.DAY_OF_YEAR,-1)};y+=cardH+gap}
        if(habits.isEmpty()){paint.textSize=15f;paint.color=Color.WHITE;c.drawText("No habits yet. Add one from the Habits page.",28f,y+30f,paint)}
    }
    override fun handleTap(e:MotionEvent):Boolean=true
}

class AchievementsView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store=VeyraStore(activity)
    override fun onDraw(c:Canvas){base(c,"Achievements","Milestones for the habits that shape your universe.");val items=store.achievements();var y=125f;items.forEach{a->glass(c,20f,y,width-20f,y+103f,if(a.unlocked)70 else 35);paint.textSize=29f;paint.color=if(a.unlocked)Color.rgb(190,145,255) else Color.argb(110,220,210,235);paint.typeface=Typeface.DEFAULT_BOLD;c.drawText(if(a.unlocked)"✦" else "○",39f,y+48f,paint);paint.textSize=17f;paint.color=Color.WHITE;paint.typeface=Typeface.DEFAULT_BOLD;c.drawText(a.title,78f,y+35f,paint);paint.textSize=12f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(185,230,220,250);c.drawText(a.description,78f,y+58f,paint);paint.textSize=11f;paint.color=if(a.unlocked)Color.rgb(194,156,255) else Color.argb(120,230,220,245);c.drawText(if(a.unlocked)"UNLOCKED" else "IN PROGRESS",78f,y+80f,paint);y+=115f}}
    override fun handleTap(e:MotionEvent):Boolean=true
}

class ReminderView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store=VeyraReminder; private var hour=VeyraReminder.hour(activity); private var minute=VeyraReminder.minute(activity); private var enabled=VeyraReminder.isEnabled(activity)
    override fun onDraw(c:Canvas){base(c,"Daily Reminder","A small nudge, at a time that works for you.");glass(c,20f,125f,width-20f,290f,65);paint.textAlign=Paint.Align.CENTER;paint.textSize=48f;paint.typeface=Typeface.DEFAULT_BOLD;paint.color=Color.WHITE;c.drawText(String.format(Locale.US,"%02d:%02d",hour,minute),width/2f,195f,paint);paint.textSize=13f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(185,230,220,250);c.drawText(if(enabled)"Reminder is active" else "Reminder is off",width/2f,222f,paint);button(c,"−  1 HOUR",38f,245f,150f,282f);button(c,"+  1 HOUR",width-150f,245f,width-38f,282f)
        glass(c,20f,310f,width-20f,430f,45);paint.textAlign=Paint.Align.LEFT;paint.textSize=15f;paint.typeface=Typeface.DEFAULT_BOLD;paint.color=Color.WHITE;c.drawText("Minute",40f,342f,paint);button(c,"− 5",38f,360f,140f,400f);button(c,"+ 5",width-140f,360f,width-38f,400f)
        button(c,if(enabled)"TURN OFF" else "TURN ON",30f,455f,width/2f-8f,505f);button(c,"SAVE REMINDER",width/2f+8f,455f,width-30f,505f)
        paint.textAlign=Paint.Align.CENTER;paint.textSize=12f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(145,230,220,250);c.drawText("Veyra will gently remind you to take one small action.",width/2f,540f,paint);paint.textAlign=Paint.Align.LEFT}
    override fun handleTap(e:MotionEvent):Boolean{val x=e.x;val y=e.y;if(e.action!=MotionEvent.ACTION_UP)return true;when{y in 245f..290f&&x<width/2f->hour=(hour+23)%24;y in 245f..290f&&x>=width/2f->hour=(hour+1)%24;y in 360f..405f&&x<width/2f->minute=(minute+55)%60;y in 360f..405f&&x>=width/2f->minute=(minute+5)%60;y in 450f..515f&&x<width/2f-> {enabled=!enabled;invalidate()};y in 450f..515f&&x>=width/2f->{if(enabled)store.set(activity,true,hour,minute) else store.set(activity,false);activity.showSettings()}};invalidate();return true}
}

class ManageHabitsView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store=VeyraStore(activity)
    override fun onDraw(c:Canvas){base(c,"Your Habits","Shape the daily actions that earn your XP.");var y=125f;store.habits().forEachIndexed{i,h->glass(c,20f,y,width-20f,y+76f,55);paint.textSize=17f;paint.typeface=Typeface.DEFAULT_BOLD;paint.color=Color.WHITE;c.drawText(h.name,38f,y+31f,paint);paint.textSize=11f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(165,225,215,245);c.drawText("Tap to manage",38f,y+52f,paint);paint.textSize=24f;paint.color=Color.argb(190,210,190,255);c.drawText("›",width-52f,y+43f,paint);y+=88f};button(c,"+  ADD A HABIT",30f,y+12f,width-30f,y+62f);paint.textAlign=Paint.Align.CENTER;paint.textSize=12f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(140,230,220,250);c.drawText("Keep it small. Make it repeatable. Build your universe.",width/2f,y+94f,paint);paint.textAlign=Paint.Align.LEFT}
    override fun handleTap(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;var y=125f;val habits=store.habits();habits.forEach{h->if(e.y in y..y+76f){activity.habitActionsFromPage(h.id);return true};y+=88f};if(e.y in y+12f..y+70f)activity.addHabitFromPage();return true}
}
