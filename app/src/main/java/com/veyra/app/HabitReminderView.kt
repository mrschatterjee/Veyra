package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import java.util.Locale

class HabitReminderView(private val activity: MainActivity, private val habitId: Long) : VeyraGlassPage(activity) {
    private val store=VeyraStore(activity)
    private val reminders=HabitReminderStore(activity)
    private val habit=store.habits().firstOrNull{it.id==habitId}
    private val existing=reminders.get(habitId)
    private var hour=existing?.hour?:20
    private var minute=existing?.minute?:0
    private var enabled=existing?.enabled?:false
    private var pm=hour>=12
    override fun onDraw(c:Canvas)=page(c){cc,w,h->
        base(cc,"Habit Reminder",habit?.name?:"Habit",w,h)
        glass(cc,16f,94f,w-16f,236f,72)
        paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.DEFAULT_BOLD;paint.textSize=13f;paint.color=Color.argb(205,235,225,255);cc.drawText("EVERY DAY",w/2,122f,paint)
        paint.textSize=38f;paint.color=Color.WHITE;paint.setShadowLayer(11f,0f,0f,Color.argb(175,165,75,255));cc.drawText(String.format(Locale.US,"%02d:%02d %s",displayHour(),minute,if(pm)"PM" else "AM"),w/2,174f,paint);paint.clearShadowLayer()
        paint.textSize=11f;paint.typeface=Typeface.DEFAULT;paint.color=Color.argb(190,230,220,250);cc.drawText(if(enabled)"Reminder is active" else "Reminder is off",w/2,204f,paint);paint.textAlign=Paint.Align.LEFT
        button(cc,"−  1 HOUR",24f,252f,132f,298f);button(cc,"+  1 HOUR",w-132f,252f,w-24f,298f)
        button(cc,"−  5 MIN",24f,310f,132f,356f);button(cc,"+  5 MIN",w-132f,310f,w-24f,356f)
        button(cc,"AM",24f,370f,w/2-6f,420f);button(cc,"PM",w/2+6f,370f,w-24f,420f)
        button(cc,if(enabled)"TURN OFF" else "TURN ON",22f,436f,w/2-6f,486f);button(cc,"SAVE",w/2+6f,436f,w-22f,486f)
        paint.textAlign=Paint.Align.CENTER;paint.textSize=10.5f;paint.color=Color.argb(165,230,220,250);cc.drawText("Choose AM or PM, then save to activate the notification.",w/2,514f,paint);paint.textAlign=Paint.Align.LEFT
    }
    private fun displayHour()=when(val h=hour%12){0->12;else->h}
    private fun setAm(value:Boolean){pm=!value;hour=when{value&&hour>=12->hour-12;!value&&hour<12->hour+12;else->hour}}
    override fun handleTap(e:MotionEvent):Boolean{
        if(e.action!=MotionEvent.ACTION_UP)return true
        val x=e.x/density;val y=(e.y-topInset)/density;val w=width/density
        when{
            y in 246f..304f&&x<w/2->{hour=(hour+23)%24;pm=hour>=12}
            y in 246f..304f&&x>=w/2->{hour=(hour+1)%24;pm=hour>=12}
            y in 304f..362f&&x<w/2->minute=(minute+55)%60
            y in 304f..362f&&x>=w/2->minute=(minute+5)%60
            y in 364f..426f&&x<w/2->setAm(true)
            y in 364f..426f&&x>=w/2->setAm(false)
            y in 430f..494f&&x<w/2->enabled=!enabled
            y in 430f..494f&&x>=w/2->{if(habit!=null)activity.saveHabitReminder(habit.id,habit.name,hour,minute,enabled);return true}
        }
        invalidate();return true
    }
}
