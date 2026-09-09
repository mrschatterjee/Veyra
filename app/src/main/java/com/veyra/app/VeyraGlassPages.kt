package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

abstract class VeyraGlassPage(private val activity: MainActivity) : View(activity) {
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val density = resources.displayMetrics.density
    protected var topInset = 0
    protected var bottomInset = 0
    protected val bgStars = listOf(
        0.08f to 0.12f, 0.18f to 0.33f, 0.31f to 0.08f, 0.47f to 0.25f,
        0.63f to 0.11f, 0.78f to 0.29f, 0.90f to 0.08f, 0.14f to 0.74f,
        0.38f to 0.88f, 0.66f to 0.70f, 0.84f to 0.90f
    )

    init {
        setOnApplyWindowInsetsListener { _, insets ->
            topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
            bottomInset = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            invalidate()
            insets
        }
    }

    protected fun page(c: Canvas, draw: (Canvas, Float, Float) -> Unit) {
        val w = width / density
        val h = (height - topInset - bottomInset) / density
        c.save()
        c.clipRect(0f, topInset.toFloat(), width.toFloat(), height - bottomInset.toFloat())
        c.translate(0f, topInset.toFloat())
        c.scale(density, density)
        draw(c, w, h)
        c.restore()
    }

    protected fun base(c: Canvas, title: String, subtitle: String, w: Float, h: Float) {
        paint.shader = LinearGradient(0f, 0f, w, h, Color.rgb(7, 4, 24), Color.rgb(43, 20, 86), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.color = Color.argb(100, 184, 139, 255)
        for (star in bgStars) {
            c.drawCircle(star.first * w, star.second * h, if (star.first > 0.7f) 2.2f else 1.2f, paint)
        }
        paint.color = Color.argb(55, 150, 90, 255)
        c.drawCircle(w * 0.92f, h * 0.18f, w * 0.25f, paint)
        paint.color = Color.argb(42, 100, 80, 255)
        c.drawCircle(w * 0.08f, h * 0.86f, w * 0.30f, paint)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create("sans", Typeface.BOLD)
        paint.textSize = 26f
        paint.color = Color.WHITE
        c.drawText(title, 54f, 43f, paint)
        paint.typeface = Typeface.create("sans", Typeface.NORMAL)
        paint.textSize = 12f
        paint.color = Color.argb(200, 235, 225, 255)
        c.drawText(subtitle, 20f, 72f, paint)
        paint.textSize = 36f
        paint.color = Color.argb(235, 255, 255, 255)
        c.drawText("‹", 17f, 47f, paint)
    }

    protected fun glass(c: Canvas, l: Float, t: Float, r: Float, b: Float, alpha: Int = 48) {
        paint.shader = LinearGradient(l, t, r, b, Color.argb(alpha, 145, 115, 220), Color.argb(20, 80, 55, 150), Shader.TileMode.CLAMP)
        c.drawRoundRect(RectF(l, t, r, b), 18f, 18f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(75, 205, 185, 255)
        c.drawRoundRect(RectF(l, t, r, b), 18f, 18f, paint)
        paint.style = Paint.Style.FILL
    }

    protected fun button(c: Canvas, label: String, l: Float, t: Float, r: Float, b: Float) {
        glass(c, l, t, r, b, 75)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("sans", Typeface.BOLD)
        paint.textSize = 12.5f
        paint.color = Color.WHITE
        c.drawText(label, (l + r) / 2f, (t + b) / 2f + 4f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP && e.y < topInset + 82f * density && e.x < 88f * density) {
            activity.showSettings()
            return true
        }
        return handleTap(e)
    }

    protected abstract fun handleTap(e: MotionEvent): Boolean
}

class HabitHistoryView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)

    override fun onDraw(c: Canvas) {
        page(c) { cc, w, h ->
            base(cc, "Habit History", "Your consistency, visualized over the last 14 days.", w, h)
            val habits = store.habits()
            val top = 94f
            val ids = habits.map { it.id }
            glass(cc, 16f, top, w - 16f, top + 72f, 65)

            paint.textSize = 24f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText(store.completedCount(14, ids).toString(), 32f, top + 33f, paint)
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(195, 235, 225, 255)
            cc.drawText("actions completed", 32f, top + 55f, paint)

            paint.textSize = 24f
            paint.typeface = Typeface.DEFAULT_BOLD
            cc.drawText(habits.size.toString(), w / 2f - 28f, top + 33f, paint)
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            cc.drawText("active habits", w / 2f - 28f, top + 55f, paint)

            paint.textSize = 24f
            paint.typeface = Typeface.DEFAULT_BOLD
            cc.drawText(store.streak(ids).toString(), w - 100f, top + 33f, paint)
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            cc.drawText("day streak", w - 100f, top + 55f, paint)

            var y = top + 84f
            val dayNumber = SimpleDateFormat("d", Locale.US)
            val dayName = SimpleDateFormat("EEE", Locale.US)
            for (habit in habits) {
                val cardH = 106f
                glass(cc, 16f, y, w - 16f, y + cardH, 48)
                paint.textSize = 16f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = Color.WHITE
                cc.drawText(habit.name, 30f, y + 29f, paint)
                paint.textSize = 10.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.argb(190, 225, 215, 255)
                cc.drawText("${store.completionDates(habit.id, 14).size} / 14 completed", 30f, y + 50f, paint)

                // Show the 14-day timeline in chronological order: oldest on the left, today on the right.
                val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -13) }
                for (i in 0 until 14) {
                    val x = 31f + i * ((w - 62f) / 13f)
                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.time)
                    val ok = store.isCompleted(habit.id, key)
                    paint.style = Paint.Style.FILL
                    paint.color = if (ok) Color.rgb(147, 102, 245) else Color.argb(45, 255, 255, 255)
                    cc.drawCircle(x, y + 73f, 7f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    paint.color = Color.argb(90, 210, 195, 255)
                    cc.drawCircle(x, y + 73f, 7f, paint)
                    paint.style = Paint.Style.FILL
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = 7.5f
                    paint.color = Color.argb(195, 240, 235, 255)
                    cc.drawText(dayNumber.format(date.time), x, y + 92f, paint)
                    paint.textSize = 6.5f
                    cc.drawText(dayName.format(date.time), x, y + 102f, paint)
                    paint.textAlign = Paint.Align.LEFT
                    date.add(Calendar.DAY_OF_YEAR, 1)
                }
                y += cardH + 10f
            }

            if (habits.isEmpty()) {
                paint.textSize = 14f
                paint.color = Color.WHITE
                cc.drawText("No habits yet. Add one from the Habits page.", 22f, y + 25f, paint)
            }
        }
    }

    override fun handleTap(e: MotionEvent): Boolean = true
}

class AchievementsView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)

    override fun onDraw(c: Canvas) {
        page(c) { cc, w, h ->
            base(cc, "Achievements", "Milestones for the habits that shape your universe.", w, h)
            val items = store.achievements()
            var y = 94f
            for (item in items) {
                glass(cc, 16f, y, w - 16f, y + 92f, if (item.unlocked) 70 else 35)
                paint.textSize = 27f
                paint.color = if (item.unlocked) Color.rgb(190, 145, 255) else Color.argb(110, 220, 210, 235)
                paint.typeface = Typeface.DEFAULT_BOLD
                cc.drawText(if (item.unlocked) "✦" else "○", 30f, y + 45f, paint)
                paint.textSize = 16f
                paint.color = Color.WHITE
                cc.drawText(item.title, 68f, y + 31f, paint)
                paint.textSize = 10.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.argb(190, 230, 220, 250)
                cc.drawText(item.description, 68f, y + 53f, paint)
                paint.textSize = 9.5f
                paint.color = if (item.unlocked) Color.rgb(194, 156, 255) else Color.argb(135, 230, 220, 245)
                cc.drawText(if (item.unlocked) "UNLOCKED" else "IN PROGRESS", 68f, y + 74f, paint)
                y += 102f
            }
        }
    }

    override fun handleTap(e: MotionEvent): Boolean = true
}

class ReminderView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private var hour = VeyraReminder.hour(activity)
    private var minute = VeyraReminder.minute(activity)
    private var enabled = VeyraReminder.isEnabled(activity)

    override fun onDraw(c: Canvas) {
        page(c) { cc, w, h ->
            base(cc, "Daily Reminder", "A small nudge, at a time that works for you.", w, h)
            glass(cc, 16f, 94f, w - 16f, 198f, 65)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 36f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText(String.format(Locale.US, "%02d:%02d", hour, minute), w / 2f, 143f, paint)
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(195, 230, 220, 250)
            cc.drawText(if (enabled) "Reminder is active" else "Reminder is off", w / 2f, 164f, paint)
            button(cc, "−  1 HOUR", 24f, 170f, 132f, 204f)
            button(cc, "+  1 HOUR", w - 132f, 170f, w - 24f, 204f)

            glass(cc, 16f, 216f, w - 16f, 316f, 45)
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText("Minute", 32f, 244f, paint)
            button(cc, "− 5", 24f, 258f, 124f, 294f)
            button(cc, "+ 5", w - 124f, 258f, w - 24f, 294f)

            button(cc, if (enabled) "TURN OFF" else "TURN ON", 22f, 334f, w / 2f - 6f, 380f)
            button(cc, "SAVE REMINDER", w / 2f + 6f, 334f, w - 22f, 380f)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(155, 230, 220, 250)
            cc.drawText("Veyra will gently remind you to take one small action.", w / 2f, 410f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        when {
            y in 168f..210f && x < w / 2f -> hour = (hour + 23) % 24
            y in 168f..210f && x >= w / 2f -> hour = (hour + 1) % 24
            y in 254f..300f && x < w / 2f -> minute = (minute + 55) % 60
            y in 254f..300f && x >= w / 2f -> minute = (minute + 5) % 60
            y in 328f..388f && x < w / 2f -> enabled = !enabled
            y in 328f..388f && x >= w / 2f -> {
                if (enabled) VeyraReminder.set(activity, true, hour, minute) else VeyraReminder.set(activity, false)
                activity.showSettings()
                return true
            }
        }
        invalidate()
        return true
    }
}

class ManageHabitsView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)

    override fun onDraw(c: Canvas) {
        page(c) { cc, w, h ->
            base(cc, "Your Habits", "Shape the daily actions that earn your XP.", w, h)
            var y = 94f
            for (habit in store.habits()) {
                glass(cc, 16f, y, w - 16f, y + 70f, 55)
                paint.textSize = 16f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = Color.WHITE
                cc.drawText(habit.name, 30f, y + 30f, paint)
                paint.textSize = 10.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.argb(175, 225, 215, 245)
                cc.drawText("Tap to manage", 30f, y + 51f, paint)
                paint.textSize = 24f
                paint.color = Color.argb(200, 210, 190, 255)
                cc.drawText("›", w - 42f, y + 42f, paint)
                y += 82f
            }
            button(cc, "+  ADD A HABIT", 22f, y + 10f, w - 22f, y + 58f)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(150, 230, 220, 250)
            cc.drawText("Keep it small. Make it repeatable. Build your universe.", w / 2f, y + 90f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val y0 = (e.y - topInset) / density
        var y = 94f
        for (habit in store.habits()) {
            if (y0 in y..(y + 70f)) {
                activity.habitActionsFromPage(habit.id)
                return true
            }
            y += 82f
        }
        if (y0 in (y + 10f)..(y + 64f)) activity.addHabitFromPage()
        return true
    }
}
