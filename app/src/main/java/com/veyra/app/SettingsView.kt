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
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private val stars = List(70) { Random(it * 91 + 13).nextFloat() to Random(it * 37 + 7).nextFloat() }
    private val rows = listOf(
        "Daily reminder" to "Choose when Veyra reminds you to take one small action.",
        "Manage habits" to "Rename or remove your habits.",
        "Habit history" to "See your recent completion history.",
        "Achievements" to "View your unlocked milestones.",
        "Backup data" to "Export your Veyra data as a backup file.",
        "Restore backup" to "Restore data from a Veyra backup.",
        "About Veyra" to "Version 1.2 and information about the app.",
        "Reset Veyra data" to "Clear all locally stored Veyra progress."
    )
    private var topInset = 0
    private var bottomInset = 0

    init {
        setOnApplyWindowInsetsListener { _, insets ->
            topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
            bottomInset = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            invalidate()
            insets
        }
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, alpha: Int = 255, bold: Boolean = false) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(alpha, 255, 255, 255)
        paint.textSize = size
        paint.typeface = Typeface.create("sans", if (bold) Typeface.BOLD else Typeface.NORMAL)
        c.drawText(s, x, y, paint)
    }

    private fun glass(c: Canvas, l: Float, t: Float, r: Float, b: Float) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(58, 255, 255, 255)
        c.drawRoundRect(l, t, r, b, 18f, 18f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(75, 255, 255, 255)
        c.drawRoundRect(l, t, r, b, 18f, 18f, paint)
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas) {
        val w = width / density
        val contentH = (height - topInset - bottomInset) / density
        c.save()
        c.clipRect(0f, topInset.toFloat(), width.toFloat(), height - bottomInset.toFloat())
        c.translate(0f, topInset.toFloat())
        c.scale(density, density)
        paint.shader = LinearGradient(0f, 0f, w, contentH, Color.rgb(9, 5, 29), Color.rgb(65, 31, 122), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, contentH, paint)
        paint.shader = null
        paint.color = Color.argb(90, 185, 145, 255)
        stars.forEach { (x, y) -> c.drawCircle(x * w, y * (contentH - 70f), if (x > .7f) 2f else 1.1f, paint) }
        paint.color = Color.argb(38, 190, 150, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        val p = Path()
        p.moveTo(-20f, contentH * .30f)
        p.cubicTo(w * .25f, contentH * .18f, w * .65f, contentH * .40f, w + 20f, contentH * .25f)
        c.drawPath(p, paint)
        p.reset()
        p.moveTo(-20f, contentH * .72f)
        p.cubicTo(w * .28f, contentH * .58f, w * .65f, contentH * .85f, w + 20f, contentH * .68f)
        c.drawPath(p, paint)
        paint.style = Paint.Style.FILL

        text(c, "‹", 22f, 43f, 34f, 240)
        text(c, "Settings", 58f, 39f, 24f, 255, true)
        text(c, "Shape Veyra around the way you live.", 22f, 68f, 12f, 180)

        val top = 92f
        val rowH = 69f
        rows.forEachIndexed { i, pair ->
            val y = top + i * rowH
            glass(c, 18f, y, w - 18f, y + 61f)
            text(c, pair.first, 32f, y + 25f, 14f, 255, true)
            text(c, pair.second.take(60), 32f, y + 46f, 9f, 155)
            text(c, "›", w - 42f, y + 36f, 22f, 170)
        }
        text(c, "Your data stays on this device unless you export a backup.", 22f, contentH - 27f, 9f, 130)
        c.restore()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        val h = (height - topInset - bottomInset) / density
        if ((x < 70f && y < 75f) || y > h - 58f) {
            onBack()
            return true
        }
        val index = ((y - 92f) / 69f).toInt()
        if (index in rows.indices && y >= 86f && y < 92f + rows.size * 69f) {
            activity.settingsAction(index)
        }
        return true
    }
}
