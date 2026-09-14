package com.spritzlive.speedometer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A single GPS sample, normalized to SI units. Speed is in metres/second as
 * reported by the platform; the UI converts to mph/km-h for display.
 */
data class SpeedSample(
    val speedMetersPerSec: Float,
    /** Horizontal accuracy in metres, or null if the platform did not report it. */
    val accuracyMeters: Float?,
    /** Estimated speed accuracy (1σ) in metres/second, or null if not reported. */
    val speedAccuracyMps: Float?,
    /** Great-circle distance from the previous fix in metres; 0 for the first fix of a run. */
    val distanceDeltaMeters: Float,
    /** true when the fix carries a real speed value from the GNSS chip. */
    val hasSpeed: Boolean,
)

/**
 * Wraps FusedLocationProviderClient as a cold [Flow]. Collection starts location
 * updates; cancelling collection removes them. The caller is responsible for
 * having been granted a location permission before collecting.
 */
class LocationProvider(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun speedUpdates(): Flow<SpeedSample> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(0f)
            .build()

        // Previous fix, kept to measure the distance travelled between updates.
        // Resets to null each time collection (re)starts, so a paused stretch is not counted.
        var previous: Location? = null

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                val delta = previous?.distanceTo(location) ?: 0f
                previous = location
                trySend(
                    SpeedSample(
                        speedMetersPerSec = if (location.hasSpeed()) location.speed else 0f,
                        accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                        speedAccuracyMps =
                            if (location.hasSpeedAccuracy()) location.speedAccuracyMetersPerSecond else null,
                        distanceDeltaMeters = delta,
                        hasSpeed = location.hasSpeed(),
                    )
                )
            }
        }

        client.requestLocationUpdates(request, callback, null)
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        const val UPDATE_INTERVAL_MS = 1000L
        const val MIN_UPDATE_INTERVAL_MS = 500L
    }
}
