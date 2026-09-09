package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import java.util.Locale

class HabitReminderView(private val activity: MainActivity, private val habitId: Long) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)
    private val habit = store.habits().firstOrNull { it.id == habitId }
    private var hour = if (VeyraReminder.habitId(activity) == habitId) VeyraReminder.hour(activity) else 20
    private var minute = if (VeyraReminder.habitId(activity) == habitId) VeyraReminder.minute(activity) else 0
    private var enabled = VeyraReminder.habitId(activity) == habitId && VeyraReminder.isEnabled(activity)

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Habit Reminder", habit?.name ?: "Habit", w, h)
        glass(cc, 16f, 94f, w - 16f, 230f, 72)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 14f
        paint.color = Color.argb(205, 235, 225, 255)
        cc.drawText("EVERY DAY", w / 2f, 123f, paint)
        paint.textSize = 40f
        paint.color = Color.WHITE
        paint.setShadowLayer(11f, 0f, 0f, Color.argb(175, 165, 75, 255))
        cc.drawText(String.format(Locale.US, "%02d:%02d", hour, minute), w / 2f, 174f, paint)
        paint.clearShadowLayer()
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(190, 230, 220, 250)
        cc.drawText(if (enabled) "Reminder is active" else "Reminder is off", w / 2f, 200f, paint)
        paint.textAlign = Paint.Align.LEFT

        button(cc, "−  1 HOUR", 24f, 246f, 132f, 292f)
        button(cc, "+  1 HOUR", w - 132f, 246f, w - 24f, 292f)
        button(cc, "−  5 MIN", 24f, 306f, 132f, 352f)
        button(cc, "+  5 MIN", w - 132f, 306f, w - 24f, 352f)
        button(cc, if (enabled) "TURN OFF" else "TURN ON", 22f, 372f, w / 2f - 6f, 422f)
        button(cc, "SAVE", w / 2f + 6f, 372f, w - 22f, 422f)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10.5f
        paint.color = Color.argb(165, 230, 220, 250)
        cc.drawText("Veyra will notify you at this time every day.", w / 2f, 452f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        when {
            y in 240f..298f && x < w / 2f -> hour = (hour + 23) % 24
            y in 240f..298f && x >= w / 2f -> hour = (hour + 1) % 24
            y in 300f..358f && x < w / 2f -> minute = (minute + 55) % 60
            y in 300f..358f && x >= w / 2f -> minute = (minute + 5) % 60
            y in 366f..430f && x < w / 2f -> enabled = !enabled
            y in 366f..430f && x >= w / 2f -> {
                if (habit != null) VeyraReminder.set(activity, enabled, hour, minute, habit.id, habit.name)
                activity.showSettings()
                return true
            }
        }
        invalidate()
        return true
    }
}
