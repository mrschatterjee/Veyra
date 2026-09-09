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
        paint.textSize = 24f
        paint.color = Color.WHITE
        c.drawText(title, 58f, 40f, paint)
        paint.typeface = Typeface.create("sans", Typeface.NORMAL)
        paint.textSize = 10.5f
        paint.color = Color.argb(185, 235, 225, 255)
        c.drawText(subtitle, 22f, 68f, paint)
        paint.textSize = 34f
        paint.color = Color.argb(235, 255, 255, 255)
        c.drawText("‹", 20f, 44f, paint)
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
        paint.textSize = 11f
        paint.color = Color.WHITE
        c.drawText(label, (l + r) / 2f, (t + b) / 2f + 4f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP && e.y < topInset + 78f * density && e.x < 85f * density) {
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
            val top = 88f
            val ids = habits.map { it.id }
            glass(cc, 18f, top, w - 18f, top + 68f, 65)

            paint.textSize = 22f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText(store.completedCount(14, ids).toString(), 34f, top + 31f, paint)
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(185, 235, 225, 255)
            cc.drawText("actions completed", 34f, top + 51f, paint)

            paint.textSize = 22f
            paint.typeface = Typeface.DEFAULT_BOLD
            cc.drawText(habits.size.toString(), w / 2f - 28f, top + 31f, paint)
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            cc.drawText("active habits", w / 2f - 28f, top + 51f, paint)

            paint.textSize = 22f
            paint.typeface = Typeface.DEFAULT_BOLD
            cc.drawText(store.streak(ids).toString(), w - 105f, top + 31f, paint)
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            cc.drawText("day streak", w - 105f, top + 51f, paint)

            var y = top + 80f
            val dayNumber = SimpleDateFormat("d", Locale.US)
            val dayName = SimpleDateFormat("EEE", Locale.US)
            for (habit in habits) {
                val cardH = 98f
                glass(cc, 18f, y, w - 18f, y + cardH, 48)
                paint.textSize = 14f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = Color.WHITE
                cc.drawText(habit.name, 32f, y + 27f, paint)
                paint.textSize = 9.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.argb(185, 225, 215, 255)
                cc.drawText("${store.completionDates(habit.id, 14).size} / 14 completed", 32f, y + 46f, paint)

                val date = Calendar.getInstance()
                for (i in 0 until 14) {
                    val x = 34f + i * ((w - 68f) / 13f)
                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.time)
                    val ok = store.isCompleted(habit.id, key)
                    paint.style = Paint.Style.FILL
                    paint.color = if (ok) Color.rgb(147, 102, 245) else Color.argb(45, 255, 255, 255)
                    cc.drawCircle(x, y + 69f, 6.5f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    paint.color = Color.argb(90, 210, 195, 255)
                    cc.drawCircle(x, y + 69f, 6.5f, paint)
                    paint.style = Paint.Style.FILL
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = 7f
                    paint.color = Color.argb(190, 240, 235, 255)
                    cc.drawText(dayNumber.format(date.time), x, y + 87f, paint)
                    paint.textSize = 6f
                    cc.drawText(dayName.format(date.time), x, y + 96f, paint)
                    paint.textAlign = Paint.Align.LEFT
                    date.add(Calendar.DAY_OF_YEAR, -1)
                }
                y += cardH + 10f
            }

            if (habits.isEmpty()) {
                paint.textSize = 13f
                paint.color = Color.WHITE
                cc.drawText("No habits yet. Add one from the Habits page.", 24f, y + 25f, paint)
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
            var y = 88f
            for (item in items) {
                glass(cc, 18f, y, w - 18f, y + 86f, if (item.unlocked) 70 else 35)
                paint.textSize = 25f
                paint.color = if (item.unlocked) Color.rgb(190, 145, 255) else Color.argb(110, 220, 210, 235)
                paint.typeface = Typeface.DEFAULT_BOLD
                cc.drawText(if (item.unlocked) "✦" else "○", 34f, y + 42f, paint)
                paint.textSize = 14f
                paint.color = Color.WHITE
                cc.drawText(item.title, 70f, y + 29f, paint)
                paint.textSize = 9.5f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.argb(185, 230, 220, 250)
                cc.drawText(item.description, 70f, y + 49f, paint)
                paint.textSize = 8.5f
                paint.color = if (item.unlocked) Color.rgb(194, 156, 255) else Color.argb(120, 230, 220, 245)
                cc.drawText(if (item.unlocked) "UNLOCKED" else "IN PROGRESS", 70f, y + 68f, paint)
                y += 96f
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
            glass(cc, 18f, 88f, w - 18f, 188f, 65)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 34f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText(String.format(Locale.US, "%02d:%02d", hour, minute), w / 2f, 132f, paint)
            paint.textSize = 9.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(185, 230, 220, 250)
            cc.drawText(if (enabled) "Reminder is active" else "Reminder is off", w / 2f, 151f, paint)
            button(cc, "−  1 HOUR", 28f, 160f, 132f, 194f)
            button(cc, "+  1 HOUR", w - 132f, 160f, w - 28f, 194f)

            glass(cc, 18f, 205f, w - 18f, 300f, 45)
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText("Minute", 34f, 231f, paint)
            button(cc, "− 5", 28f, 247f, 124f, 282f)
            button(cc, "+ 5", w - 124f, 247f, w - 28f, 282f)

            button(cc, if (enabled) "TURN OFF" else "TURN ON", 25f, 318f, w / 2f - 6f, 362f)
            button(cc, "SAVE REMINDER", w / 2f + 6f, 318f, w - 25f, 362f)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(145, 230, 220, 250)
            cc.drawText("Veyra will gently remind you to take one small action.", w / 2f, 390f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        when {
            y in 160f..200f && x < w / 2f -> hour = (hour + 23) % 24
            y in 160f..200f && x >= w / 2f -> hour = (hour + 1) % 24
            y in 247f..287f && x < w / 2f -> minute = (minute + 55) % 60
            y in 247f..287f && x >= w / 2f -> minute = (minute + 5) % 60
            y in 310f..370f && x < w / 2f -> enabled = !enabled
            y in 310f..370f && x >= w / 2f -> {
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
            var y = 88f
            for (habit in store.habits()) {
                glass(cc, 18f, y, w - 18f, y + 65f, 55)
                paint.textSize = 14f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.color = Color.WHITE
                cc.drawText(habit.name, 32f, y + 28f, paint)
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.argb(165, 225, 215, 245)
                cc.drawText("Tap to manage", 32f, y + 47f, paint)
                paint.textSize = 22f
                paint.color = Color.argb(190, 210, 190, 255)
                cc.drawText("›", w - 42f, y + 39f, paint)
                y += 76f
            }
            button(cc, "+  ADD A HABIT", 25f, y + 10f, w - 25f, y + 54f)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(140, 230, 220, 250)
            cc.drawText("Keep it small. Make it repeatable. Build your universe.", w / 2f, y + 84f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val y0 = (e.y - topInset) / density
        var y = 88f
        for (habit in store.habits()) {
            if (y0 in y..(y + 65f)) {
                activity.habitActionsFromPage(habit.id)
                return true
            }
            y += 76f
        }
        if (y0 in (y + 10f)..(y + 60f)) activity.addHabitFromPage()
        return true
    }
}
