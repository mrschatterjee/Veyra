package com.veyra.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private lateinit var v: VeyraView
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); v = VeyraView(this); setContentView(v) }
    fun textInput(title: String, hint: String, onSave: (String) -> Unit) {
        val input = EditText(this).apply { this.hint = hint; setSingleLine(false); minLines = 2 }
        AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
            val s = input.text.toString().trim(); if (s.isNotEmpty()) onSave(s)
        }.show()
    }
}

private class VeyraView(private val ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prefs = ctx.getSharedPreferences("veyra", Context.MODE_PRIVATE)
    private var tab = 0
    private val tabs = listOf("Today", "Habits", "Goals", "Journal", "Stats")
    private val habits = mutableListOf("Drink water", "Study", "Move 20 min")
    private val completed = BooleanArray(20)
    private var mood = prefs.getInt("mood", 0)
    private var xp = prefs.getInt("xp", 0)
    private var streak = prefs.getInt("streak", 0)
    private var journal = prefs.getString("journal", "") ?: ""
    private var goal = prefs.getString("goal", "Build a better day") ?: "Build a better day"
    private val stars = List(75) { Random(it + 17).nextFloat() to Random(it + 71).nextFloat() }
    private val sdf = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

    init { isFocusable = true; p.typeface = Typeface.create("sans", Typeface.NORMAL) }
    private fun save() { prefs.edit().putInt("mood", mood).putInt("xp", xp).putInt("streak", streak).putString("journal", journal).putString("goal", goal).apply() }
    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, alpha: Int = 255, bold: Boolean = false) { p.color = Color.argb(alpha,255,255,255); p.textSize=size; p.typeface=Typeface.create("sans", if(bold) Typeface.BOLD else Typeface.NORMAL); c.drawText(s,x,y,p) }
    private fun round(c: Canvas,l:Float,t:Float,r:Float,b:Float,rad:Float,fill:Int,stroke:Int=Color.TRANSPARENT) { p.color=fill; p.style=Paint.Style.FILL; c.drawRoundRect(l,t,r,b,rad,rad,p); if(stroke!=Color.TRANSPARENT){p.color=stroke;p.style=Paint.Style.STROKE;p.strokeWidth=1f;c.drawRoundRect(l,t,r,b,rad,rad,p);p.style=Paint.Style.FILL} }

    override fun onDraw(c: Canvas) {
        super.onDraw(c); val w=width.toFloat(); val h=height.toFloat()
        val g=LinearGradient(0f,0f,w,h,Color.rgb(10,6,30),Color.rgb(61,31,112),Shader.TileMode.CLAMP); p.shader=g;c.drawRect(0f,0f,w,h,p);p.shader=null
        p.color=Color.argb(90,170,130,255); for((sx,sy) in stars) c.drawCircle(sx*w, sy*(h-90), if((sx*10).toInt()%3==0) 2f else 1f,p)
        p.color=Color.argb(55,185,130,255);p.style=Paint.Style.STROKE;p.strokeWidth=2f;val path=Path();path.moveTo(-20f,h*.27f);path.cubicTo(w*.25f,h*.16f,w*.58f,h*.38f,w+20,h*.24f);c.drawPath(path,p);path.reset();path.moveTo(-20f,h*.62f);path.cubicTo(w*.3f,h*.5f,w*.65f,h*.78f,w+20,h*.61f);c.drawPath(path,p);p.style=Paint.Style.FILL
        drawHeader(c,w)
        when(tab){0->today(c,w);1->habits(c,w);2->goals(c,w);3->journalPage(c,w);4->stats(c,w)}
        drawNav(c,w,h)
    }
    private fun drawHeader(c:Canvas,w:Float){ text(c,"VEYRA",24f,45f,22f,255,true); text(c,"Build your universe.",24f,68f,13f,190); text(c,sdf.format(Date()),w-145f,44f,11f,190) }
    private fun glass(c:Canvas,l:Float,t:Float,r:Float,b:Float){ round(c,l,t,r,b,24f,Color.argb(55,255,255,255),Color.argb(70,255,255,255)) }
    private fun today(c:Canvas,w:Float){
        text(c,"Good morning.",24f,112f,30f,255,true); text(c,"One small action at a time.",24f,137f,14f,180)
        glass(c,20f,158f,w-20f,265f); text(c,"TODAY",38f,188f,11f,170,true); text(c,"Your universe is",38f,220f,17f); text(c,"${completed.count{it}} habits complete",38f,244f,25f,255,true)
        text(c,"Mood",w-100f,188f,11f,170); val faces=listOf("☹","😐","🙂","😄"); faces.forEachIndexed{ i,f->text(c,f,w-112f+i*25,218f,18f,if(mood==i)255 else 130)}
        text(c,"Focus",24f,301f,20f,255,true); glass(c,20f,318f,w-20f,418f); text(c,goal,38f,350f,16f); text(c,"${xp} XP  •  ${streak} day streak",38f,381f,13f,180)
        text(c,"Quick actions",24f,458f,20f,255,true); action(c,20f,475f,w/2-8,"+ Habit"); action(c,w/2+8,475f,w-20f,"Write journal")
    }
    private fun action(c:Canvas,l:Float,t:Float,r:Float,s:String){glass(c,l,t,r,t+62);text(c,s,l+18,t+38,15f,255,true)}
    private fun habits(c:Canvas,w:Float){ text(c,"Habits",24f,112f,30f,255,true);text(c,"Consistency compounds.",24f,137f,14f,180); habits.forEachIndexed{ i,s->val y=165f+i*78;glass(c,20f,y,w-20f,y+62);val done=completed.getOrNull(i)==true;round(c,38f,y+17,64f,y+43,13f,if(done)Color.rgb(151,112,255) else Color.argb(20,255,255,255));if(done)text(c,"✓",43f,y+38,17f);text(c,s,80f,y+27,16f);text(c,if(done)"Complete":"Tap to complete",80f,y+48,11f,170)}; action(c,20f,165f+habits.size*78,w-20f,"+ Add a habit") }
    private fun goals(c:Canvas,w:Float){ text(c,"Goals",24f,112f,30f,255,true);text(c,"Turn intentions into direction.",24f,137f,14f,180);glass(c,20f,165f,w-20f,290f);text(c,"CURRENT GOAL",38f,195f,11f,170,true);text(c,goal,38f,226f,20f,255,true);round(c,38f,248,w-38f,258,5f,Color.argb(45,255,255,255));round(c,38f,248,38f+(w-76f)*0.35f,258,5f,Color.rgb(167,139,250));text(c,"35% complete",38f,278f,12f,180);action(c,20f,315,w-20f,"Edit goal") }
    private fun journalPage(c:Canvas,w:Float){ text(c,"Journal",24f,112f,30f,255,true);text(c,"A quiet place for your thoughts.",24f,137f,14f,180);glass(c,20f,165f,w-20f,410f);text(c,"TODAY",38f,195f,11f,170,true); if(journal.isBlank()){text(c,"Nothing written yet.",38f,235f,17f,180);text(c,"Tap below to capture the day.",38f,261f,13f,150)} else {var y=230f; journal.split("\n").take(8).forEach{line->text(c,line.take(48),38f,y,14f,220);y+=22}};action(c,20f,430,w-20f,"+ Write entry") }
    private fun stats(c:Canvas,w:Float){ text(c,"Stats",24f,112f,30f,255,true);text(c,"See the pattern, not just the day.",24f,137f,14f,180);glass(c,20f,165f,w-20f,335f);text(c,"LEVEL",38f,195f,11f,170,true);text(c,"${1+xp/100}",38f,235f,42f,255,true);text(c,"${xp%100}/100 XP",100f,229f,14f,180);round(c,100f,246,w-38f,256,5f,Color.argb(45,255,255,255));round(c,100f,246,100f+(w-138f)*((xp%100)/100f),256,5f,Color.rgb(167,139,250));text(c,"7D",38f,305f,12f,180,true);text(c,"${completed.count{it}} completed",100f,305f,15f);text(c,"Streak",38f,370f,12f,180,true);text(c,"${streak} days",100f,370f,15f); }
    private fun drawNav(c:Canvas,w:Float,h:Float){ val top=h-78;round(c,12f,top,w-12f,h-8,25f,Color.argb(85,10,6,35),Color.argb(80,255,255,255));val step=w/tabs.size;tabs.forEachIndexed{i,s->val x=step*i+step/2;text(c,s,x-p.measureText(s)/2,top+44,10f,if(i==tab)255 else 135,i==tab)} }

    override fun onTouchEvent(e: MotionEvent): Boolean { if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x;val y=e.y;val w=width.toFloat();val h=height.toFloat()
        if(y>h-90){tab=((x/(w/tabs.size)).toInt()).coerceIn(0,4);invalidate();return true}
        when(tab){
            0->{if(y in 158f..265f && x>w-145){mood=((mood+1)%4);save();invalidate()} else if(y in 475f..537f){if(x<w/2){addHabit()}else{writeJournal()}}}
            1->{if(y in 165f..(165f+habits.size*78)){val i=((y-165)/78).toInt();if(i<habits.size){completed[i]=!completed[i];if(completed[i]){xp+=10;streak= maxOf(streak,1)}else xp=maxOf(0,xp-10);save();invalidate()}} else if(y>165+habits.size*78){addHabit()}}
            2->{if(y in 315f..390f){editGoal()}}
            3->{if(y in 430f..500f)writeJournal()}
        };return true }
    private fun addHabit(){(ctx as MainActivity).textInput("Add a habit","e.g. Read 20 minutes"){habits.add(it);invalidate()}}
    private fun editGoal(){(ctx as MainActivity).textInput("Edit goal",goal){goal=it;save();invalidate()}}
    private fun writeJournal(){(ctx as MainActivity).textInput("Today's journal","What happened today?"){journal=it;save();invalidate()}}
}
