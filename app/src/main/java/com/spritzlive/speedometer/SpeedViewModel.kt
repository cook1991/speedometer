package com.spritzlive.speedometer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Everything the UI needs to render one frame of the speedometer. */
data class SpeedUiState(
    val unit: SpeedUnit = SpeedUnit.MPH,
    /** Current speed in the selected unit. */
    val currentSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val averageSpeed: Float = 0f,
    val accuracyMeters: Float? = null,
    /** Estimated speed accuracy (±) in the selected unit, or null if not reported. */
    val speedAccuracy: Float? = null,
    /** Accumulated trip distance in the selected distance unit (mi/km). */
    val tripDistance: Float = 0f,
    /** true once we have received at least one usable fix with a speed value. */
    val hasFix: Boolean = false,
    val isTracking: Boolean = false,
)

class SpeedViewModel(app: Application) : AndroidViewModel(app) {

    private val locationProvider = LocationProvider(app)
    private val prefs = SpeedPrefs(app)

    private val _state = MutableStateFlow(SpeedUiState(unit = prefs.unit))
    val state: StateFlow<SpeedUiState> = _state.asStateFlow()

    private var trackingJob: Job? = null

    // Accumulation kept in SI units (metres, m/s) so unit switches don't distort it.
    private val stats = SpeedStats()
    private var speedAccuracyMps: Float? = null

    init {
        // Restore any trip that was in progress when the app was last killed.
        stats.restoreTrip(prefs.tripMeters.toDouble())
        _state.value = _state.value.copy(
            tripDistance = (stats.tripMeters * prefs.unit.distanceFromMeters).toFloat(),
        )
    }

    fun startTracking() {
        if (trackingJob != null) return
        _state.value = _state.value.copy(isTracking = true)
        trackingJob = viewModelScope.launch {
            locationProvider.speedUpdates().collect { sample -> onSample(sample) }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        // Persist the trip so it survives the process being killed while backgrounded.
        prefs.tripMeters = stats.tripMeters.toFloat()
        _state.value = _state.value.copy(isTracking = false)
    }

    private fun onSample(sample: SpeedSample) {
        stats.onSample(sample)
        speedAccuracyMps = sample.speedAccuracyMps
        val mps = sample.speedMetersPerSec.coerceAtLeast(0f)
        val unit = _state.value.unit
        _state.value = _state.value.copy(
            currentSpeed = mps * unit.factorFromMps,
            maxSpeed = stats.maxSpeedMps * unit.factorFromMps,
            averageSpeed = stats.averageSpeedMps * unit.factorFromMps,
            accuracyMeters = sample.accuracyMeters,
            speedAccuracy = sample.speedAccuracyMps?.let { it * unit.factorFromMps },
            tripDistance = (stats.tripMeters * unit.distanceFromMeters).toFloat(),
            hasFix = _state.value.hasFix || sample.hasSpeed,
        )
    }

    fun toggleUnit() {
        val next = _state.value.unit.other()
        prefs.unit = next
        // Re-derive displayed values in the new unit from the stored SI values.
        _state.value = _state.value.copy(
            unit = next,
            currentSpeed = _state.value.currentSpeed / _state.value.unit.factorFromMps * next.factorFromMps,
            maxSpeed = stats.maxSpeedMps * next.factorFromMps,
            averageSpeed = stats.averageSpeedMps * next.factorFromMps,
            speedAccuracy = speedAccuracyMps?.let { it * next.factorFromMps },
            tripDistance = (stats.tripMeters * next.distanceFromMeters).toFloat(),
        )
    }

    /** Single reset: clears MAX, AVG, and the trip together. */
    fun resetStats() {
        stats.resetSpeeds()
        stats.resetTrip()
        prefs.tripMeters = 0f
        _state.value = _state.value.copy(maxSpeed = 0f, averageSpeed = 0f, tripDistance = 0f)
    }
}
