package com.veyra.app

import android.app.AlarmManager
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.MotionEvent
import java.util.Locale

class NudgeConfigView(private val activity: MainActivity, private val nudge: Nudge) : VeyraGlassPage(activity) {
    private val store = NudgeStore(activity)
    private var interval = nudge.intervalMinutes.coerceAtLeast(15)
    private var startHour = nudge.startHour.coerceIn(0, 23)
    private var endHour = nudge.endHour.coerceIn(0, 23)
    private var enabled = nudge.enabled

    override fun onDraw(c: Canvas) = page(c) { cc, w, h ->
        base(cc, "Nudge Settings", nudge.name, w, h)
        glass(cc, 16f, 94f, w - 16f, 232f, 72)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 12f
        paint.color = Color.argb(205, 235, 225, 255)
        cc.drawText("REPEAT EVERY", w / 2, 121f, paint)
        paint.textSize = 34f
        paint.color = Color.WHITE
        paint.setShadowLayer(10f, 0f, 0f, Color.argb(170, 165, 75, 255))
        cc.drawText(label(interval), w / 2, 172f, paint)
        paint.clearShadowLayer()
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(165, 230, 220, 250)
        cc.drawText("during the active window", w / 2, 201f, paint)
        button(cc, "−", 24f, 246f, 132f, 296f)
        button(cc, "+", w - 132f, 246f, w - 24f, 296f)
        glass(cc, 16f, 310f, w - 16f, 418f, 55)
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.argb(165, 235, 225, 255)
        cc.drawText("ACTIVE WINDOW", 30f, 333f, paint)
        paint.textSize = 17f
        paint.color = Color.WHITE
        cc.drawText("${clock(startHour)} – ${clock(endHour)}", 30f, 360f, paint)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(155, 230, 220, 250)
        cc.drawText(if (startHour == endHour) "All day" else "No nudges outside this window", 30f, 388f, paint)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        button(cc, "START −1H", 22f, 430f, w / 2f - 7f, 478f)
        button(cc, "START +1H", w / 2f + 7f, 430f, w - 22f, 478f)
        button(cc, "END −1H", 22f, 488f, w / 2f - 7f, 536f)
        button(cc, "END +1H", w / 2f + 7f, 488f, w - 22f, 536f)
        button(cc, if (enabled) "TURN OFF" else "TURN ON", 22f, 550f, w / 2f - 7f, 600f)
        button(cc, "SAVE", w / 2f + 7f, 550f, w - 22f, 600f)
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.argb(165, 230, 220, 250)
        cc.drawText("Veyra uses exact device-clock alarms and background protection settings.", w / 2, 626f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun label(m: Int) = when { m < 60 -> "$m MIN"; m % 60 == 0 -> if (m / 60 == 1) "1 HOUR" else "${m / 60} HOURS"; else -> String.format(Locale.US, "%dH %dM", m / 60, m % 60) }
    private fun clock(hour: Int): String { val h = hour % 12; return String.format(Locale.US, "%02d:00 %s", if (h == 0) 12 else h, if (hour >= 12) "PM" else "AM") }

    private fun requestExactAlarmAccessIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarm = activity.getSystemService(AlarmManager::class.java)
        if (alarm.canScheduleExactAlarms()) return
        VeyraGlassDialog.showConfirm(activity,"Sync to your device clock?","Allow Veyra's Alarms & reminders access so nudges can fire on the exact clock schedule even while the phone is idle.","OPEN SETTINGS") { runCatching { activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply { data = Uri.parse("package:${activity.packageName}") }) } }
    }

    override fun handleTap(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x=e.x/density; val y=(e.y-topInset)/density; val w=width/density
        when {
            y in 238f..304f && x < w/2 -> interval=when{interval<=15->15;interval<=30->15;interval<=60->30;interval<=120->60;interval<=180->120;else->180}
            y in 238f..304f && x >= w/2 -> interval=when{interval<30->30;interval<60->60;interval<120->120;interval<180->180;else->240}
            y in 424f..484f && x < w/2 -> startHour=(startHour+23)%24
            y in 424f..484f && x >= w/2 -> startHour=(startHour+1)%24
            y in 484f..542f && x < w/2 -> endHour=(endHour+23)%24
            y in 484f..542f && x >= w/2 -> endHour=(endHour+1)%24
            y in 544f..608f && x < w/2 -> enabled=!enabled
            y in 544f..608f && x >= w/2 -> { val updated=nudge.copy(intervalMinutes=interval,startHour=startHour,endHour=endHour,enabled=enabled);store.save(updated);NudgeScheduler.schedule(activity,updated);if(updated.enabled){requestExactAlarmAccessIfNeeded();NotificationReliability.promptIfNeeded(activity)};activity.showNudges();return true }
        }
        invalidate(); return true
    }
}
