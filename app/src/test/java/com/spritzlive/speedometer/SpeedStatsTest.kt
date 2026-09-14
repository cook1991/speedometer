package com.spritzlive.speedometer

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the trip/speed accumulation and — the point of this suite —
 * the two independent reset paths. No Android runtime required.
 */
class SpeedStatsTest {

    /** A moving fix: has speed, moved [distanceDeltaMeters], with a good accuracy fix. */
    private fun moving(speedMps: Float, distanceDeltaMeters: Float, accuracyMeters: Float? = 5f) =
        SpeedSample(
            speedMetersPerSec = speedMps,
            accuracyMeters = accuracyMeters,
            speedAccuracyMps = 0.4f,
            distanceDeltaMeters = distanceDeltaMeters,
            hasSpeed = true,
        )

    @Test
    fun `trip accumulates distance while genuinely moving`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 20f))
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 20f))
        assertEquals(40.0, stats.tripMeters, 1e-6)
    }

    @Test
    fun `stationary fixes do not add trip distance`() {
        val stats = SpeedStats()
        // hasSpeed=false: the GNSS chip reported no speed at all.
        stats.onSample(
            SpeedSample(
                speedMetersPerSec = 0f,
                accuracyMeters = 5f,
                speedAccuracyMps = null,
                distanceDeltaMeters = 3f, // jitter delta
                hasSpeed = false,
            )
        )
        // hasSpeed=true but essentially parked (below the moving threshold).
        stats.onSample(moving(speedMps = 0.2f, distanceDeltaMeters = 3f))
        assertEquals(0.0, stats.tripMeters, 1e-6)
    }

    @Test
    fun `poor accuracy fixes are excluded from trip distance`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 50f, accuracyMeters = 100f))
        assertEquals(0.0, stats.tripMeters, 1e-6)
    }

    @Test
    fun `null accuracy is trusted when clearly moving`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 20f, accuracyMeters = null))
        assertEquals(20.0, stats.tripMeters, 1e-6)
    }

    @Test
    fun `max and average track speed samples`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 10f, distanceDeltaMeters = 10f))
        stats.onSample(moving(speedMps = 30f, distanceDeltaMeters = 30f))
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 20f))
        assertEquals(30f, stats.maxSpeedMps, 1e-4f)
        assertEquals(20f, stats.averageSpeedMps, 1e-4f) // (10+30+20)/3
    }

    @Test
    fun `resetTrip clears trip but leaves max and average intact`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 10f, distanceDeltaMeters = 10f))
        stats.onSample(moving(speedMps = 30f, distanceDeltaMeters = 30f))

        stats.resetTrip()

        assertEquals("trip should be cleared", 0.0, stats.tripMeters, 1e-6)
        assertEquals("max must survive a trip reset", 30f, stats.maxSpeedMps, 1e-4f)
        assertEquals("average must survive a trip reset", 20f, stats.averageSpeedMps, 1e-4f)
    }

    @Test
    fun `resetSpeeds clears max and average but leaves trip intact`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 10f, distanceDeltaMeters = 10f))
        stats.onSample(moving(speedMps = 30f, distanceDeltaMeters = 30f))

        stats.resetSpeeds()

        assertEquals("trip must survive a speed reset", 40.0, stats.tripMeters, 1e-6)
        assertEquals("max should be cleared", 0f, stats.maxSpeedMps, 1e-4f)
        assertEquals("average should be cleared", 0f, stats.averageSpeedMps, 1e-4f)
    }

    @Test
    fun `trip keeps accumulating after a trip reset`() {
        val stats = SpeedStats()
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 20f))
        stats.resetTrip()
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 15f))
        assertEquals(15.0, stats.tripMeters, 1e-6)
    }

    @Test
    fun `restoreTrip seeds the trip and further movement adds to it`() {
        val stats = SpeedStats()
        stats.restoreTrip(1000.0)
        stats.onSample(moving(speedMps = 20f, distanceDeltaMeters = 25f))
        assertEquals(1025.0, stats.tripMeters, 1e-6)
    }
}
