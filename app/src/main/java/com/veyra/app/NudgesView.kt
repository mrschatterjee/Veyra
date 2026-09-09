package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.view.MotionEvent

class NudgesView(private val activity:MainActivity):VeyraGlassPage(activity){
    private val store=NudgeStore(activity)
    private var draft=Nudge(0,"",60,8,22,true)
    override fun onDraw(c:Canvas)=page(c){cc,w,h->
        base(cc,"Nudges","Small reminders that keep your day moving.",w,h)
        var y=94f
        for(n in store.all()){
            glass(cc,16f,y,w-16f,y+74f,64);paint.typeface=Typeface.DEFAULT_BOLD;paint.textSize=15f;paint.color=Color.WHITE;cc.drawText(n.name,30f,y+27f,paint)
            paint.typeface=Typeface.DEFAULT;paint.textSize=10.5f;paint.color=Color.argb(180,230,220,250);cc.drawText("Every ${format(n.intervalMinutes)}  •  ${n.startHour}:00–${n.endHour}:00",30f,y+49f,paint)
            paint.color=if(n.enabled)Color.argb(230,210,180,255)else Color.argb(120,220,220,230);paint.textSize=10f;cc.drawText(if(n.enabled)"ON" else "OFF",w-54f,y+28f,paint);y+=84f
        }
        button(cc,"+  ADD A NUDGE",22f,y+10f,w-22f,y+58f)
    }
    private fun format(m:Int)=when{m%60==0->"${m/60} hour${if(m>60)"s" else ""}";else->"$m min"}
    override fun handleTap(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x/density;val y=(e.y-topInset)/density;val w=width/density;var row=94f;for(n in store.all()){if(y in row..row+74f){draft=n;showEditor();return true};row+=84f};if(y>=row+4f&&y<=row+70f)showEditor();return true}
    private fun showEditor(){VeyraGlassDialog.showInput(activity,"Nudge name",if(draft.name.isBlank())"Drink Water" else draft.name){name->val clean=name.trim();if(clean.isNotBlank()){val n=Nudge(if(draft.id==0L)store.nextId() else draft.id,clean,draft.intervalMinutes,draft.startHour,draft.endHour,draft.enabled);store.save(n)}activity.showNudges()}}
}
