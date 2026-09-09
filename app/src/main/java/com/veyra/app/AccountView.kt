package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import kotlinx.coroutines.launch

class AccountView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val user get() = VeyraAuth.currentUser()
    private var busy = false

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Account", "Your Veyra identity and cloud connection.", w, h)
        glass(cc, 16f, 100f, w - 16f, 310f, 72)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 24f
        paint.color = Color.WHITE
        cc.drawText(user?.displayName?.takeIf { it.isNotBlank() } ?: "Veyra user", w / 2f, 155f, paint)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(195, 230, 220, 250)
        cc.drawText(user?.email ?: "No account connected", w / 2f, 184f, paint)
        paint.textSize = 10.5f
        paint.color = Color.argb(160, 230, 220, 250)
        cc.drawText(if (user != null) "Google account connected" else "Offline mode", w / 2f, 222f, paint)
        cc.drawText("Your Veyra data is associated with your Firebase user ID.", w / 2f, 250f, paint)
        button(cc, if (busy) "SIGNING OUT…" else "SIGN OUT", 24f, 338f, w - 24f, 392f)
        paint.textSize = 10f
        paint.color = Color.argb(150, 230, 220, 250)
        cc.drawText("Signing out stops automatic cloud sync on this device.", w / 2f, 430f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP || busy) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        if (y in 326f..404f && x in 18f..w - 18f && user != null) {
            busy = true
            invalidate()
            activity.accountScope.launch {
                VeyraAuth.signOut(activity)
                busy = false
                activity.showLogin()
            }
        }
        return true
    }
}
