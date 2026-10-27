package com.spritzlive.speedometer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spritzlive.speedometer.ui.PermissionScreen
import com.spritzlive.speedometer.ui.SpeedScreen
import com.spritzlive.speedometer.ui.SpeedometerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep the display awake while the speedometer is on screen.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            SpeedometerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpeedometerApp()
                }
            }
        }
    }
}

private fun hasLocationPermission(activity: ComponentActivity): Boolean =
    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@androidx.compose.runtime.Composable
private fun SpeedometerApp() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val vm: SpeedViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var granted by remember { mutableStateOf(hasLocationPermission(activity)) }
    var deniedPermanently by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val ok = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        granted = ok
        if (!ok) {
            deniedPermanently =
                !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Start/stop tracking with the composition lifecycle once permission is present.
    androidx.compose.runtime.LaunchedEffect(granted) {
        if (granted) vm.startTracking() else vm.stopTracking()
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { vm.stopTracking() }
    }

    if (granted) {
        SpeedScreen(
            state = state,
            onToggleUnit = vm::toggleUnit,
            onReset = vm::resetStats,
        )
    } else {
        PermissionScreen(
            deniedPermanently = deniedPermanently,
            onRequest = {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                context.startActivity(intent)
            },
        )
    }
}
