package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent

class AccountView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Profile", "Your name and personal Veyra identity.", w, h)
        glass(cc, 16f, 100f, w - 16f, 330f, 72)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 25f
        paint.color = Color.WHITE
        cc.drawText(store.userName().ifBlank { "Veyra user" }, w / 2f, 158f, paint)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(195, 230, 220, 250)
        cc.drawText("This name is saved locally on this device.", w / 2f, 193f, paint)
        paint.textSize = 10.5f
        paint.color = Color.argb(160, 230, 220, 250)
        cc.drawText("It will be used throughout your Veyra experience.", w / 2f, 224f, paint)
        button(cc, "CHANGE NAME", 24f, 275f, w - 24f, 331f)
        paint.textSize = 10f
        paint.color = Color.argb(150, 230, 220, 250)
        cc.drawText("No Google account or password is required.", w / 2f, 380f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        if (y in 264f..343f && x in 18f..w - 18f) {
            activity.textInput("Change your name", "Enter your name") { value ->
                val clean = value.trim().take(40)
                if (clean.isNotBlank()) {
                    store.setUserName(clean)
                    invalidate()
                }
            }
        }
        return true
    }
}
