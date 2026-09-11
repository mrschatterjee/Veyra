package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent

class LoginView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private var name = ""

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Welcome to Veyra", "Build your universe.", w, h)
        glass(cc, 16f, 110f, w - 16f, 390f, 72)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 30f
        paint.color = Color.WHITE
        paint.setShadowLayer(12f, 0f, 0f, Color.argb(180, 165, 75, 255))
        cc.drawText("VEYRA", w / 2f, 165f, paint)
        paint.clearShadowLayer()
        paint.textSize = 15f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(230, 240, 230, 255)
        cc.drawText("What should we call you?", w / 2f, 225f, paint)
        paint.textSize = 11f
        paint.color = Color.argb(165, 230, 220, 250)
        cc.drawText("Your name stays saved on this device", w / 2f, 255f, paint)
        cc.drawText("and will be used throughout Veyra.", w / 2f, 273f, paint)
        button(cc, if (name.isBlank()) "ENTER YOUR NAME" else name.take(24), 22f, 305f, w - 22f, 361f)
        button(cc, "CONTINUE", 22f, 375f, w - 22f, 431f)
        paint.textSize = 10.5f
        paint.color = Color.argb(165, 230, 220, 250)
        cc.drawText("You can change your name later in Settings.", w / 2f, 470f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun editName() {
        activity.textInput("Your name", "Enter your name") { value ->
            name = value.trim().take(40)
            invalidate()
        }
    }

    private fun continueToApp() {
        val clean = name.trim().take(40)
        if (clean.isBlank()) {
            editName()
            return
        }
        activity.saveUserName(clean)
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        when {
            y in 296f..368f && x in 16f..w - 16f -> editName()
            y in 368f..442f && x in 16f..w - 16f -> continueToApp()
        }
        return true
    }
}
