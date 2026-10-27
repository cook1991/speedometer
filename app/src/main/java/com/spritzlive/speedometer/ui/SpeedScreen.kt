package com.spritzlive.speedometer.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spritzlive.speedometer.SpeedUiState
import kotlin.math.roundToInt

@Composable
fun SpeedScreen(
    state: SpeedUiState,
    onToggleUnit: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (landscape) {
        LandscapeLayout(state, onToggleUnit, onReset, modifier)
    } else {
        PortraitLayout(state, onToggleUnit, onReset, modifier)
    }
}

@Composable
private fun PortraitLayout(
    state: SpeedUiState,
    onToggleUnit: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        StatusLine(state)
        SpeedReadout(state, modifier = Modifier.fillMaxWidth())
        StatsRow(state)
        Controls(state, onToggleUnit, onReset)
    }
}

@Composable
private fun LandscapeLayout(
    state: SpeedUiState,
    onToggleUnit: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SpeedReadout(
            state,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            fitToHeight = true,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatusLine(state)
            StatsRow(state)
            Controls(state, onToggleUnit, onReset)
        }
    }
}

/** The analog gauge with the big current-speed number centered on top. */
@Composable
private fun SpeedReadout(
    state: SpeedUiState,
    modifier: Modifier = Modifier,
    fitToHeight: Boolean = false,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SpeedGauge(
            speed = state.currentSpeed,
            maxScale = state.unit.gaugeMax,
            accent = MaterialTheme.colorScheme.primary,
            warm = MaterialTheme.colorScheme.secondary,
            trackColor = Color(0xFF223040),
            tickColor = Color(0xFF556677),
            modifier = if (fitToHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
            fitToHeight = fitToHeight,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.currentSpeed.roundToInt().toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = state.unit.label,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun Controls(
    state: SpeedUiState,
    onToggleUnit: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilledTonalButton(
            onClick = onToggleUnit,
            modifier = Modifier.weight(1f),
        ) {
            Text("Switch to ${state.unit.other().label}")
        }
        OutlinedButton(onClick = onReset) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reset stats")
            Spacer(Modifier.width(6.dp))
            Text("Reset")
        }
    }
}

@Composable
private fun StatusLine(state: SpeedUiState) {
    val (text, color) = when {
        !state.isTracking -> "Paused" to Color(0xFF8899AA)
        !state.hasFix -> "Acquiring GPS…" to MaterialTheme.colorScheme.secondary
        state.accuracyMeters != null && state.accuracyMeters > 20f ->
            "Weak signal" to MaterialTheme.colorScheme.secondary
        else -> {
            val acc = state.speedAccuracy?.let { " ±${"%.1f".format(it)} ${state.unit.label}" } ?: ""
            "GPS locked$acc" to MaterialTheme.colorScheme.primary
        }
    }
    Text(
        text = text,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StatsRow(state: SpeedUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Stat("MAX", state.maxSpeed.roundToInt().toString(), state.unit.label)
        Stat("AVG", state.averageSpeed.roundToInt().toString(), state.unit.label)
        Stat("TRIP", "%.1f".format(state.tripDistance), state.unit.distanceLabel)
    }
}

@Composable
private fun Stat(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 13.sp, color = Color(0xFF8899AA), fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(unit, fontSize = 12.sp, color = Color(0xFF8899AA))
    }
}
