package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent

class LoginView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private var busy = false
    private var message = "Sign in to keep your Veyra universe synced across devices."

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Welcome to Veyra", "Build your universe.", w, h)
        glass(cc, 16f, 110f, w - 16f, 350f, 72)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 30f
        paint.color = Color.WHITE
        paint.setShadowLayer(12f, 0f, 0f, Color.argb(180, 165, 75, 255))
        cc.drawText("VEYRA", w / 2f, 165f, paint)
        paint.clearShadowLayer()
        paint.textSize = 13f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(220, 235, 225, 255)
        cc.drawText("Your habits, goals and progress", w / 2f, 205f, paint)
        cc.drawText("belong to your account.", w / 2f, 225f, paint)
        paint.textSize = 10.5f
        paint.color = Color.argb(165, 230, 220, 250)
        cc.drawText("Google sign-in uses Firebase Authentication.", w / 2f, 270f, paint)
        cc.drawText("Veyra never stores your Google password.", w / 2f, 290f, paint)
        button(cc, if (busy) "SIGNING IN…" else "CONTINUE WITH GOOGLE", 22f, 382f, w - 22f, 438f)
        button(cc, "CONTINUE OFFLINE", 22f, 452f, w - 22f, 508f)
        paint.textSize = 10.5f
        paint.color = Color.argb(175, 230, 220, 250)
        cc.drawText(message.take(76), w / 2f, 550f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    fun startGoogle() {
        if (busy) return
        busy = true
        message = "Opening Google sign-in…"
        invalidate()
        activity.signInWithGoogle { success, result ->
            busy = false
            message = if (success) "Signed in successfully." else result
            invalidate()
        }
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        when {
            y in 374f..445f && x in 16f..w - 16f -> startGoogle()
            y in 445f..518f && x in 16f..w - 16f -> activity.continueOffline()
        }
        return true
    }
}
