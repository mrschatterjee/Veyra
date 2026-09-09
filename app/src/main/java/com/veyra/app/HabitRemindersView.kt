package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import java.util.Locale

class HabitRemindersView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)
    private val reminders = HabitReminderStore(activity)

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Habit Reminders", "Give each habit its own daily moment.", w, h)
        val habits = store.habits()
        var y = 94f
        for (habit in habits) {
            val reminder = reminders.get(habit.id)
            glass(cc, 16f, y, w - 16f, y + 82f, if (reminder?.enabled == true) 68 else 46)
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = 15f
            paint.color = Color.WHITE
            cc.drawText(habit.name, 30f, y + 29f, paint)
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 10.5f
            paint.color = Color.argb(190, 230, 220, 250)
            val detail = when {
                reminder == null -> "No reminder set"
                reminder.enabled -> "Every day  •  ${formatTime(reminder.hour, reminder.minute)}"
                else -> "Reminder off  •  ${formatTime(reminder.hour, reminder.minute)}"
            }
            cc.drawText(detail, 30f, y + 52f, paint)
            paint.textSize = 9.5f
            paint.color = if (reminder?.enabled == true) Color.rgb(205, 165, 255) else Color.argb(145, 225, 220, 240)
            cc.drawText(if (reminder?.enabled == true) "ACTIVE" else "OFF", w - 72f, y + 29f, paint)
            paint.textSize = 18f
            paint.color = Color.argb(190, 235, 225, 255)
            cc.drawText("›", w - 39f, y + 54f, paint)
            y += 92f
        }
        if (habits.isEmpty()) {
            glass(cc, 16f, y, w - 16f, y + 90f, 48)
            paint.textSize = 13f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = Color.WHITE
            cc.drawText("No habits yet", 30f, y + 35f, paint)
            paint.textSize = 10.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.argb(175, 230, 220, 250)
            cc.drawText("Add habits from Manage habits first.", 30f, y + 58f, paint)
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val h = when (val value = hour % 12) { 0 -> 12; else -> value }
        return String.format(Locale.US, "%02d:%02d %s", h, minute, if (hour >= 12) "PM" else "AM")
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val y = (e.y - topInset) / density
        var row = 94f
        for (habit in store.habits()) {
            if (y in row..row + 82f) {
                activity.showHabitReminderFromOverview(habit.id)
                return true
            }
            row += 92f
        }
        return true
    }
}
