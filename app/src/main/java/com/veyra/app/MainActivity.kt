package com.veyra.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class MainActivity : Activity() {
    private lateinit var veyraView: VeyraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        veyraView = VeyraView(this)
        setContentView(veyraView)
    }

    fun textInput(title: String, hint: String, onSave: (String) -> Unit) {
        val input = EditText(this).apply {
            this.hint = hint
            setSingleLine(false)
            minLines = 2
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty()) onSave(value)
            }
            .show()
    }
}

private class VeyraView(private val ctx: Context) : View(ctx) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prefs = ctx.getSharedPreferences("veyra", Context.MODE_PRIVATE)
    private var tab = 0
    private val tabs = listOf("Today", "Habits", "Goals", "Journal", "Stats")
    private val habits = mutableListOf("Drink water", "Study", "Move 20 min")
    private val completed = BooleanArray(20)
    private var mood = prefs.getInt("mood", 0).coerceIn(0, 3)
    private var xp = prefs.getInt("xp", 0).coerceAtLeast(0)
    private var streak = prefs.getInt("streak", 0).coerceAtLeast(0)
    private var journal = prefs.getString("journal", "") ?: ""
    private var goal = prefs.getString("goal", "Build a better day") ?: "Build a better day"
    private val stars = List(75) {
        Random((it + 17).toLong()).nextFloat() to Random((it + 71).toLong()).nextFloat()
    }
    private val dateFormat = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())

    init {
        isFocusable = true
        paint.typeface = Typeface.create("sans", Typeface.NORMAL)
    }

    private fun save() {
        prefs.edit()
            .putInt("mood", mood)
            .putInt("xp", xp)
            .putInt("streak", streak)
            .putString("journal", journal)
            .putString("goal", goal)
            .apply()
    }

    private fun text(
        canvas: Canvas,
        value: String,
        x: Float,
        y: Float,
        size: Float,
        alpha: Int = 255,
        bold: Boolean = false
    ) {
        paint.color = Color.argb(alpha, 255, 255, 255)
        paint.textSize = size
        paint.typeface = Typeface.create("sans", if (bold) Typeface.BOLD else Typeface.NORMAL)
        canvas.drawText(value, x, y, paint)
    }

    private fun round(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float,
        fill: Int,
        stroke: Int = Color.TRANSPARENT
    ) {
        paint.color = fill
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        if (stroke != Color.TRANSPARENT) {
            paint.color = stroke
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
            paint.style = Paint.Style.FILL
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        val gradient = LinearGradient(
            0f, 0f, width, height,
            Color.rgb(10, 6, 30),
            Color.rgb(61, 31, 112),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        paint.color = Color.argb(90, 170, 130, 255)
        for ((sx, sy) in stars) {
            canvas.drawCircle(sx * width, sy * (height - 90f), if ((sx * 10f).toInt() % 3 == 0) 2f else 1f, paint)
        }

        paint.color = Color.argb(55, 185, 130, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        val path = Path()
        path.moveTo(-20f, height * 0.27f)
        path.cubicTo(width * 0.25f, height * 0.16f, width * 0.58f, height * 0.38f, width + 20f, height * 0.24f)
        canvas.drawPath(path, paint)
        path.reset()
        path.moveTo(-20f, height * 0.62f)
        path.cubicTo(width * 0.3f, height * 0.5f, width * 0.65f, height * 0.78f, width + 20f, height * 0.61f)
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        drawHeader(canvas, width)
        when (tab) {
            0 -> drawToday(canvas, width)
            1 -> drawHabits(canvas, width)
            2 -> drawGoals(canvas, width)
            3 -> drawJournal(canvas, width)
            4 -> drawStats(canvas, width)
        }
        drawNavigation(canvas, width, height)
    }

    private fun drawHeader(canvas: Canvas, width: Float) {
        text(canvas, "VEYRA", 24f, 45f, 22f, bold = true)
        text(canvas, "Build your universe.", 24f, 68f, 13f, 190)
        text(canvas, dateFormat.format(Date()), width - 145f, 44f, 11f, 190)
    }

    private fun glass(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        round(canvas, left, top, right, bottom, 24f, Color.argb(55, 255, 255, 255), Color.argb(70, 255, 255, 255))
    }

    private fun drawToday(canvas: Canvas, width: Float) {
        text(canvas, "Good morning.", 24f, 112f, 30f, bold = true)
        text(canvas, "One small action at a time.", 24f, 137f, 14f, 180)

        glass(canvas, 20f, 158f, width - 20f, 265f)
        text(canvas, "TODAY", 38f, 188f, 11f, 170, true)
        text(canvas, "Your universe is", 38f, 220f, 17f)
        text(canvas, "${completed.count { it }} habits complete", 38f, 244f, 25f, bold = true)
        text(canvas, "Mood", width - 100f, 188f, 11f, 170)
        val faces = listOf("☹", "😐", "🙂", "😄")
        faces.forEachIndexed { index, face ->
            text(canvas, face, width - 112f + index * 25f, 218f, 18f, if (mood == index) 255 else 130)
        }

        text(canvas, "Focus", 24f, 301f, 20f, bold = true)
        glass(canvas, 20f, 318f, width - 20f, 418f)
        text(canvas, goal, 38f, 350f, 16f)
        text(canvas, "${xp} XP  •  ${streak} day streak", 38f, 381f, 13f, 180)

        text(canvas, "Quick actions", 24f, 458f, 20f, bold = true)
        action(canvas, 20f, 475f, width / 2f - 8f, "+ Habit")
        action(canvas, width / 2f + 8f, 475f, width - 20f, "Write journal")
    }

    private fun action(canvas: Canvas, left: Float, top: Float, right: Float, label: String) {
        glass(canvas, left, top, right, top + 62f)
        text(canvas, label, left + 18f, top + 38f, 15f, bold = true)
    }

    private fun drawHabits(canvas: Canvas, width: Float) {
        text(canvas, "Habits", 24f, 112f, 30f, bold = true)
        text(canvas, "Consistency compounds.", 24f, 137f, 14f, 180)

        habits.forEachIndexed { index, label ->
            val top = 165f + index * 78f
            glass(canvas, 20f, top, width - 20f, top + 62f)
            val done = completed.getOrNull(index) == true
            round(
                canvas, 38f, top + 17f, 64f, top + 43f, 13f,
                if (done) Color.rgb(151, 112, 255) else Color.argb(20, 255, 255, 255)
            )
            if (done) text(canvas, "✓", 43f, top + 38f, 17f)
            text(canvas, label, 80f, top + 27f, 16f)
            text(canvas, if (done) "Complete" else "Tap to complete", 80f, top + 48f, 11f, 170)
        }

        val addTop = 165f + habits.size * 78f
        action(canvas, 20f, addTop, width - 20f, "+ Add a habit")
    }

    private fun drawGoals(canvas: Canvas, width: Float) {
        text(canvas, "Goals", 24f, 112f, 30f, bold = true)
        text(canvas, "Turn intentions into direction.", 24f, 137f, 14f, 180)
        glass(canvas, 20f, 165f, width - 20f, 290f)
        text(canvas, "CURRENT GOAL", 38f, 195f, 11f, 170, true)
        text(canvas, goal, 38f, 226f, 20f, bold = true)
        round(canvas, 38f, 248f, width - 38f, 258f, 5f, Color.argb(45, 255, 255, 255))
        round(canvas, 38f, 248f, 38f + (width - 76f) * 0.35f, 258f, 5f, Color.rgb(167, 139, 250))
        text(canvas, "35% complete", 38f, 278f, 12f, 180)
        action(canvas, 20f, 315f, width - 20f, "Edit goal")
    }

    private fun drawJournal(canvas: Canvas, width: Float) {
        text(canvas, "Journal", 24f, 112f, 30f, bold = true)
        text(canvas, "A quiet place for your thoughts.", 24f, 137f, 14f, 180)
        glass(canvas, 20f, 165f, width - 20f, 410f)
        text(canvas, "TODAY", 38f, 195f, 11f, 170, true)
        if (journal.isBlank()) {
            text(canvas, "Nothing written yet.", 38f, 235f, 17f, 180)
            text(canvas, "Tap below to capture the day.", 38f, 261f, 13f, 150)
        } else {
            var y = 230f
            journal.split("\n").take(8).forEach { line ->
                text(canvas, line.take(48), 38f, y, 14f, 220)
                y += 22f
            }
        }
        action(canvas, 20f, 430f, width - 20f, "+ Write entry")
    }

    private fun drawStats(canvas: Canvas, width: Float) {
        text(canvas, "Stats", 24f, 112f, 30f, bold = true)
        text(canvas, "See the pattern, not just the day.", 24f, 137f, 14f, 180)
        glass(canvas, 20f, 165f, width - 20f, 335f)
        text(canvas, "LEVEL", 38f, 195f, 11f, 170, true)
        text(canvas, "${1 + xp / 100}", 38f, 235f, 42f, bold = true)
        text(canvas, "${xp % 100}/100 XP", 100f, 229f, 14f, 180)
        round(canvas, 100f, 246f, width - 38f, 256f, 5f, Color.argb(45, 255, 255, 255))
        val progress = (xp % 100) / 100f
        round(canvas, 100f, 246f, 100f + (width - 138f) * progress, 256f, 5f, Color.rgb(167, 139, 250))
        text(canvas, "7D", 38f, 305f, 12f, 180, true)
        text(canvas, "${completed.count { it }} completed", 100f, 305f, 15f)
        text(canvas, "Streak", 38f, 370f, 12f, 180, true)
        text(canvas, "${streak} days", 100f, 370f, 15f)
    }

    private fun drawNavigation(canvas: Canvas, width: Float, height: Float) {
        val top = height - 78f
        round(canvas, 12f, top, width - 12f, height - 8f, 25f, Color.argb(85, 10, 6, 35), Color.argb(80, 255, 255, 255))
        val step = width / tabs.size.toFloat()
        tabs.forEachIndexed { index, label ->
            val x = step * index + step / 2f
            paint.textSize = 10f
            paint.typeface = Typeface.create("sans", if (index == tab) Typeface.BOLD else Typeface.NORMAL)
            val labelWidth = paint.measureText(label)
            text(canvas, label, x - labelWidth / 2f, top + 44f, 10f, if (index == tab) 255 else 135, index == tab)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true

        val x = event.x
        val y = event.y
        val width = width.toFloat()
        val height = height.toFloat()

        if (y > height - 90f) {
            tab = (x / (width / tabs.size.toFloat())).toInt().coerceIn(0, 4)
            invalidate()
            return true
        }

        when (tab) {
            0 -> {
                if (y in 158f..265f && x > width - 145f) {
                    mood = (mood + 1) % 4
                    save()
                    invalidate()
                } else if (y in 475f..537f) {
                    if (x < width / 2f) addHabit() else writeJournal()
                }
            }
            1 -> {
                val habitsBottom = 165f + habits.size * 78f
                if (y in 165f until habitsBottom) {
                    val index = ((y - 165f) / 78f).toInt()
                    if (index in habits.indices) {
                        completed[index] = !completed[index]
                        if (completed[index]) {
                            xp += 10
                            streak = maxOf(streak, 1)
                        } else {
                            xp = maxOf(0, xp - 10)
                        }
                        save()
                        invalidate()
                    }
                } else if (y >= habitsBottom) {
                    addHabit()
                }
            }
            2 -> if (y in 315f..390f) editGoal()
            3 -> if (y in 430f..500f) writeJournal()
        }
        return true
    }

    private fun addHabit() {
        (ctx as MainActivity).textInput("Add a habit", "e.g. Read 20 minutes") {
            habits.add(it)
            invalidate()
        }
    }

    private fun editGoal() {
        (ctx as MainActivity).textInput("Edit goal", goal) {
            goal = it
            save()
            invalidate()
        }
    }

    private fun writeJournal() {
        (ctx as MainActivity).textInput("Today's journal", "What happened today?") {
            journal = it
            save()
            invalidate()
        }
    }
}
