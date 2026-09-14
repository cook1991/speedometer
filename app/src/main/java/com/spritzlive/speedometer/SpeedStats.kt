package com.spritzlive.speedometer

/**
 * Pure, Android-free accumulator for the running speed/trip figures. Kept
 * separate from [SpeedViewModel] so the gating and reset rules can be unit
 * tested without a device, SharedPreferences, or Play Services.
 *
 * All values are in SI units (metres, metres/second); unit conversion for
 * display is the caller's job.
 */
class SpeedStats {

    var maxSpeedMps = 0f
        private set

    /** Accumulated trip distance in metres. */
    var tripMeters = 0.0
        private set

    private var speedSumMps = 0.0
    private var sampleCount = 0L

    /** Mean of every speed-bearing sample since the last speed reset. */
    val averageSpeedMps: Float
        get() = if (sampleCount > 0) (speedSumMps / sampleCount).toFloat() else 0f

    fun onSample(sample: SpeedSample) {
        val mps = sample.speedMetersPerSec.coerceAtLeast(0f)
        if (sample.hasSpeed) {
            if (mps > maxSpeedMps) maxSpeedMps = mps
            speedSumMps += mps
            sampleCount++
        }
        // Only count distance for genuine movement with a decent fix, so a parked
        // phone's GPS jitter doesn't quietly rack up mileage.
        if (sample.hasSpeed && mps > MIN_MOVING_MPS &&
            (sample.accuracyMeters == null || sample.accuracyMeters <= MAX_DISTANCE_ACCURACY_M)
        ) {
            tripMeters += sample.distanceDeltaMeters
        }
    }

    /** Clears MAX and AVG. Leaves the trip untouched. */
    fun resetSpeeds() {
        speedSumMps = 0.0
        sampleCount = 0L
        maxSpeedMps = 0f
    }

    /** Clears the trip distance. Leaves MAX and AVG untouched. */
    fun resetTrip() {
        tripMeters = 0.0
    }

    /** Seed the trip from a persisted value (e.g. after the app was killed). */
    fun restoreTrip(meters: Double) {
        tripMeters = meters
    }

    companion object {
        // Below this speed a fix is treated as noise for distance purposes (~1.1 mph).
        const val MIN_MOVING_MPS = 0.5f

        // Fixes worse than this horizontal accuracy are too imprecise to trust for distance.
        const val MAX_DISTANCE_ACCURACY_M = 25f
    }
}
