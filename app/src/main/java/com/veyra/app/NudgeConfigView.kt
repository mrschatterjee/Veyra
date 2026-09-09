package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import java.util.Locale

class NudgeConfigView(private val activity:MainActivity, private val nudge:Nudge):VeyraGlassPage(activity){
 private val store=NudgeStore(activity);private var interval=nudge.intervalMinutes;private var enabled=nudge.enabled
 override fun onDraw(c:Canvas)=page(c){cc,w,h->base(cc,"Nudge Settings",nudge.name,w,h);glass(cc,16f,94f,w-16f,230f,72);paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.DEFAULT_BOLD;paint.textSize=13f;paint.color=Color.argb(205,235,225,255);cc.drawText("REPEAT EVERY",w/2,124f,paint);paint.textSize=38f;paint.color=Color.WHITE;paint.setShadowLayer(10f,0f,0f,Color.argb(170,165,75,255));cc.drawText(label(interval),w/2,176f,paint);paint.clearShadowLayer();paint.textAlign=Paint.Align.LEFT;button(cc,"−",24f,248f,132f,300f);button(cc,"+",w-132f,248f,w-24f,300f);button(cc,if(enabled)"TURN OFF" else "TURN ON",22f,324f,w/2-6,374f);button(cc,"SAVE",w/2+6,324f,w-22f,374f);paint.textAlign=Paint.Align.CENTER;paint.textSize=10.5f;paint.color=Color.argb(165,230,220,250);cc.drawText("Veyra will nudge you repeatedly while this is enabled.",w/2,408f,paint);paint.textAlign=Paint.Align.LEFT}
 private fun label(m:Int)=when{m<60->"$m MIN";m%60==0->if(m/60==1)"1 HOUR" else "${m/60} HOURS";else->String.format(Locale.US,"%dH %dM",m/60,m%60)}
 override fun handleTap(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x/density;val y=(e.y-topInset)/density;val w=width/density;when{y in 240f..306f&&x<w/2->interval=when{interval<=15->15;interval<=30->15;interval<=60->30;interval<=120->60;else->120};y in 240f..306f&&x>=w/2->interval=when{interval<30->30;interval<60->60;interval<120->120;interval<180->180;else->240};y in 318f..382f&&x<w/2->enabled=!enabled;y in 318f..382f&&x>=w/2->{store.save(nudge.copy(intervalMinutes=interval,enabled=enabled));activity.showNudges();return true}};invalidate();return true}
}
