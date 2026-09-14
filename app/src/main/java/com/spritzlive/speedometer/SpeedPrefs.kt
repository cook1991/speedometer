package com.spritzlive.speedometer

import android.content.Context

/** Speed unit for display. Conversion factors are from metres/second (speed) and metres (distance). */
enum class SpeedUnit(
    val label: String,
    val factorFromMps: Float,
    val gaugeMax: Float,
    /** Short label for the matching distance unit, e.g. "mi" for mph. */
    val distanceLabel: String,
    /** Multiply metres by this to get the display distance unit. */
    val distanceFromMeters: Float,
) {
    MPH("mph", 2.2369363f, 120f, "mi", 1f / 1609.344f),
    KMH("km/h", 3.6f, 200f, "km", 0.001f);

    fun other(): SpeedUnit = if (this == MPH) KMH else MPH
}

/** Tiny SharedPreferences wrapper for the one setting we persist. */
class SpeedPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("speedometer", Context.MODE_PRIVATE)

    var unit: SpeedUnit
        get() = runCatching { SpeedUnit.valueOf(prefs.getString(KEY_UNIT, SpeedUnit.MPH.name)!!) }
            .getOrDefault(SpeedUnit.MPH)
        set(value) = prefs.edit().putString(KEY_UNIT, value.name).apply()

    /** Accumulated trip distance in metres, persisted so a trip survives the app being killed. */
    var tripMeters: Float
        get() = prefs.getFloat(KEY_TRIP, 0f)
        set(value) = prefs.edit().putFloat(KEY_TRIP, value).apply()

    private companion object {
        const val KEY_UNIT = "unit"
        const val KEY_TRIP = "trip_meters"
    }
}
