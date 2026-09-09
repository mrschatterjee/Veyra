package com.veyra.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarView(private val activity: MainActivity) : VeyraGlassPage(activity) {
    private val store = VeyraStore(activity)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    private var selected = Calendar.getInstance()

    private fun key(cal: Calendar): String = dateFormat.format(cal.time)
    private fun dayCal(day: Int): Calendar = Calendar.getInstance().apply {
        timeInMillis = selected.timeInMillis
        set(Calendar.DAY_OF_MONTH, day)
    }
    private fun ids() = store.habits().map { it.id }
    private fun completedOn(date: String): Int = ids().count { store.isCompleted(it, date) }
    private fun streakEnding(date: String): Int {
        val habitIds = ids()
        if (habitIds.isEmpty()) return 0
        val c = Calendar.getInstance().apply { timeInMillis = selected.timeInMillis }
        c.timeInMillis = dateFormat.parse(date)!!.time
        var n = 0
        while (habitIds.all { store.isCompleted(it, key(c)) }) {
            n++
            c.add(Calendar.DAY_OF_YEAR, -1)
        }
        return n
    }

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Calendar", "Your history, one day at a time.", w, h)
        val month = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selected.time)
        glass(cc, 16f, 94f, w - 16f, 326f, 62)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 16f
        paint.color = Color.WHITE
        cc.drawText(month, w / 2f, 120f, paint)
        paint.textSize = 28f
        cc.drawText("‹", 42f, 121f, paint)
        cc.drawText("›", w - 42f, 121f, paint)

        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
        val first = Calendar.getInstance().apply { timeInMillis = selected.timeInMillis; set(Calendar.DAY_OF_MONTH, 1) }
        val offset = first.get(Calendar.DAY_OF_WEEK) - 1
        val max = selected.getActualMaximum(Calendar.DAY_OF_MONTH)
        val ids = ids()
        val xStep = (w - 68f) / 6f
        for (i in 0 until 7) {
            paint.textSize = 9f
            paint.color = Color.argb(155, 230, 220, 250)
            cc.drawText(weekdays[i], 34f + i * xStep, 151f, paint)
        }
        for (d in 1..max) {
            val day = dayCal(d)
            val date = key(day)
            val index = offset + d - 1
            val row = index / 7
            val col = index % 7
            val x = 34f + col * xStep
            val y = 181f + row * 38f
            val done = ids.count { store.isCompleted(it, date) }
            val some = done > 0
            val complete = ids.isNotEmpty() && done == ids.size
            val isSelected = d == selected.get(Calendar.DAY_OF_MONTH)
            if (isSelected || some) {
                paint.style = Paint.Style.FILL
                paint.color = if (isSelected) Color.rgb(157, 104, 255) else Color.argb(55, 170, 100, 255)
                if (isSelected) paint.setShadowLayer(10f, 0f, 0f, Color.argb(170, 160, 70, 255))
                cc.drawCircle(x, y - 5f, 14f, paint)
                paint.clearShadowLayer()
            }
            paint.textSize = 11f
            paint.color = Color.WHITE
            paint.typeface = Typeface.DEFAULT_BOLD
            cc.drawText(d.toString(), x, y - 1f, paint)
            if (complete) {
                paint.color = Color.argb(230, 220, 190, 255)
                cc.drawCircle(x, y + 8f, 2.5f, paint)
            }
        }

        val date = key(selected)
        val done = ids.count { store.isCompleted(it, date) }
        val total = ids.size
        val rate = VeyraStats.completionRate(done, total)
        val dayXp = done * 10
        val streak = streakEnding(date)
        val today = key(Calendar.getInstance())
        val isFuture = date > today
        val goal = store.goal()
        val names = ids.mapNotNull { id -> store.habits().firstOrNull { it.id == id }?.takeIf { store.isCompleted(it.id, date) }?.name }
        val journal = store.journal(date).isNotBlank()
        val goalStatus = if (total > 0 && done == total && !isFuture) "Completed" else if (isFuture) "Upcoming" else "In progress"

        paint.textAlign = Paint.Align.LEFT
        glass(cc, 16f, 340f, w - 16f, 414f, 70)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.argb(165, 235, 225, 255)
        cc.drawText("SELECTED DAY", 30f, 362f, paint)
        paint.textSize = 20f
        paint.color = Color.WHITE
        cc.drawText(SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(selected.time), 30f, 390f, paint)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(170, 230, 220, 250)
        cc.drawText(if (journal) "Journal entry saved" else "No journal entry", 30f, 407f, paint)

        glass(cc, 16f, 426f, w - 16f, 548f, 52)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.argb(165, 235, 225, 255)
        cc.drawText("DAY STATS", 30f, 448f, paint)
        paint.textSize = 13f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.WHITE
        cc.drawText("Habits  $done / $total", 30f, 474f, paint)
        cc.drawText("Completion  $rate%", 30f, 500f, paint)
        cc.drawText("XP earned  $dayXp", w / 2f, 474f, paint)
        cc.drawText("Streak  $streak day${if (streak == 1) "" else "s"}", w / 2f, 500f, paint)
        paint.textSize = 9.5f
        paint.color = Color.argb(145, 230, 220, 250)
        cc.drawText("XP is based on completed habits for that day.", 30f, 528f, paint)

        glass(cc, 16f, 560f, w - 16f, 674f, 45)
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.argb(155, 235, 225, 255)
        cc.drawText("HABITS COMPLETED", 30f, 581f, paint)
        paint.textSize = 11.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.WHITE
        cc.drawText(if (names.isEmpty()) "No habits completed" else names.joinToString(" • ").take(62), 30f, 605f, paint)
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.argb(155, 235, 225, 255)
        cc.drawText("GOAL FOCUS", 30f, 628f, paint)
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.WHITE
        cc.drawText(goal.take(46), 30f, 646f, paint)
        paint.textSize = 9.5f
        paint.color = Color.argb(170, 230, 220, 250)
        cc.drawText("Status: $goalStatus", 30f, 663f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x / density
        val y = (e.y - topInset) / density
        val w = width / density
        if (y in 96f..145f) {
            when {
                x < w * .3f -> selected.add(Calendar.MONTH, -1)
                x > w * .7f -> selected.add(Calendar.MONTH, 1)
            }
            invalidate()
            return true
        }
        if (y in 145f..330f) {
            val first = Calendar.getInstance().apply { timeInMillis = selected.timeInMillis; set(Calendar.DAY_OF_MONTH, 1) }
            val offset = first.get(Calendar.DAY_OF_WEEK) - 1
            val col = ((x - 20f) / ((w - 40f) / 7f)).toInt().coerceIn(0, 6)
            val row = ((y - 162f) / 38f).toInt().coerceIn(0, 5)
            val day = row * 7 + col - offset + 1
            if (day in 1..selected.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                selected.set(Calendar.DAY_OF_MONTH, day)
                invalidate()
            }
        }
        return true
    }
}
