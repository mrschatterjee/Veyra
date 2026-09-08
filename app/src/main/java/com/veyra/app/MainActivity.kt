package com.veyra.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import java.util.Random

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(OpeningView(this) { setContentView(VeyraHomeView(this)) })
    }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
    fun textInput(title: String, hint: String, onSave: (String) -> Unit) {
        val input = EditText(this).apply { this.hint = hint; setSingleLine(false); minLines = 2 }
        AlertDialog.Builder(this).setTitle(title).setView(input).setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ -> input.text.toString().trim().takeIf { it.isNotEmpty() }?.let(onSave) }.show()
    }
}

private class OpeningView(ctx: Context, private val onFinished: () -> Unit) : View(ctx) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stars = List(70) { Random(it + 91L).nextFloat() to Random(it + 143L).nextFloat() }
    private val handler = Handler(Looper.getMainLooper())
    private var stage = 0; private var alpha = 0f; private var finished = false
    init { handler.postDelayed({ stage = 1; animateIn() }, 250) }
    private fun animateIn() {
        alpha = 0f; val start = System.currentTimeMillis()
        fun tick() {
            alpha = ((System.currentTimeMillis() - start).toFloat() / 650L).coerceIn(0f, 1f); invalidate()
            if (alpha < 1f) handler.postDelayed({ tick() }, 16L)
            else if (stage < 3) handler.postDelayed({ stage++; animateIn() }, 650L)
            else handler.postDelayed({ finishOnce() }, 950L)
        }; tick()
    }
    private fun finishOnce() { if (!finished) { finished = true; onFinished() } }
    override fun onDetachedFromWindow() { handler.removeCallbacksAndMessages(null); super.onDetachedFromWindow() }
    override fun onTouchEvent(event: MotionEvent): Boolean { if (event.action == MotionEvent.ACTION_UP) finishOnce(); return true }
    override fun onDraw(c: Canvas) {
        val w=width.toFloat(); val h=height.toFloat(); paint.shader=LinearGradient(0f,0f,w,h,Color.rgb(8,5,26),Color.rgb(64,30,118),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,paint);paint.shader=null
        paint.color=Color.argb(100,185,145,255);stars.forEach{(x,y)->c.drawCircle(x*w,y*h,1.2f,paint)}
        paint.color=Color.argb(45,190,150,255);paint.style=Paint.Style.STROKE;paint.strokeWidth=2f;val p=Path();p.moveTo(-20f,h*.52f);p.cubicTo(w*.25f,h*.38f,w*.62f,h*.65f,w+20f,h*.48f);c.drawPath(p,paint);paint.style=Paint.Style.FILL
        val label=when(stage){1->"VEYRA";2->"GOOD MORNING";else->"Build your universe."};paint.color=Color.argb((alpha*255).toInt(),255,255,255);paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create("sans",if(stage==1)Typeface.BOLD else Typeface.NORMAL);paint.textSize=if(stage==1)42f else if(stage==2)24f else 19f;c.drawText(label,w/2f,h/2f+8f,paint);paint.textAlign=Paint.Align.LEFT
    }
}
