package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import kotlin.random.Random

class SettingsView(private val activity: MainActivity, private val onBack: () -> Unit) : View(activity) {
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG);private val density=resources.displayMetrics.density;private val stars=List(70){Random(it*91+13).nextFloat() to Random(it*37+7).nextFloat()}
 private val rows=listOf("Habit reminders" to "Choose when each habit reminds you to take action.","Nudges" to "Small recurring reminders like water, stretch and rest.","Manage habits" to "Rename or remove your habits.","Habit history" to "See your recent completion history.","Calendar" to "Explore any date and see your daily stats.","Achievements" to "View your unlocked milestones.","Backup data" to "Export your Veyra data as a backup file.","Restore backup" to "Restore data from a Veyra backup.","About Veyra" to "Version 1.4 and information about the app.","Reset Veyra data" to "Clear all locally stored Veyra progress.")
 private var topInset=0;private var bottomInset=0
 init{setOnApplyWindowInsetsListener{_,i->topInset=i.getInsets(WindowInsets.Type.statusBars()).top;bottomInset=i.getInsets(WindowInsets.Type.navigationBars()).bottom;invalidate();i}}
 private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,alpha:Int=255,bold:Boolean=false){paint.shader=null;paint.style=Paint.Style.FILL;paint.color=Color.argb(alpha,255,255,255);paint.textSize=size;paint.typeface=Typeface.create("sans",if(bold)Typeface.BOLD else Typeface.NORMAL);c.drawText(s,x,y,paint)}
 private fun glass(c:Canvas,l:Float,t:Float,r:Float,b:Float){paint.shader=null;paint.style=Paint.Style.FILL;paint.color=Color.argb(58,255,255,255);c.drawRoundRect(l,t,r,b,18f,18f,paint);paint.style=Paint.Style.STROKE;paint.strokeWidth=1f;paint.color=Color.argb(75,255,255,255);c.drawRoundRect(l,t,r,b,18f,18f,paint);paint.style=Paint.Style.FILL}
 override fun onDraw(c:Canvas){val w=width/density;val ch=(height-topInset-bottomInset)/density;c.save();c.clipRect(0f,topInset.toFloat(),width.toFloat(),height-bottomInset.toFloat());c.translate(0f,topInset.toFloat());c.scale(density,density);paint.shader=LinearGradient(0f,0f,w,ch,Color.rgb(9,5,29),Color.rgb(65,31,122),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,ch,paint);paint.shader=null;paint.color=Color.argb(90,185,145,255);stars.forEach{(x,y)->c.drawCircle(x*w,y*(ch-70f),if(x>.7f)2f else 1.1f,paint)};paint.color=Color.argb(38,190,150,255);paint.style=Paint.Style.STROKE;paint.strokeWidth=2f;val p=Path();p.moveTo(-20f,ch*.30f);p.cubicTo(w*.25f,ch*.18f,w*.65f,ch*.40f,w+20f,ch*.25f);c.drawPath(p,paint);p.reset();p.moveTo(-20f,ch*.72f);p.cubicTo(w*.28f,ch*.58f,w*.65f,ch*.85f,w+20f,ch*.68f);c.drawPath(p,paint);paint.style=Paint.Style.FILL;text(c,"‹",18f,48f,36f,245);text(c,"Settings",54f,43f,27f,255,true);text(c,"Shape Veyra around the way you live.",20f,73f,12.5f,195);val top=94f;val rowH=73f;rows.forEachIndexed{i,pair->val y=top+i*rowH;glass(c,16f,y,w-16f,y+65f);text(c,pair.first,30f,y+27f,15.5f,255,true);text(c,pair.second.take(60),30f,y+49f,10.5f,175);text(c,"›",w-40f,y+39f,24f,180)};text(c,"Your data stays on this device unless you export a backup.",20f,ch-27f,10f,140);c.restore()}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x/density;val y=(e.y-topInset)/density;val h=(height-topInset-bottomInset)/density;if((x<70f&&y<80f)||y>h-58f){onBack();return true};val i=((y-94f)/73f).toInt();if(i in rows.indices&&y>=88f&&y<94f+rows.size*73f){if(i==4){val view=CalendarView(activity);activity.setContentView(view);VeyraMotion.enter(view,18f)}else{val mapped=if(i>=5)i-1 else i;activity.settingsAction(mapped)}};return true}
}
