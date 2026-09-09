package com.veyra.app

import org.json.JSONObject

fun Nudge.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("interval", intervalMinutes)
    put("start", startHour)
    put("end", endHour)
    put("enabled", enabled)
}
