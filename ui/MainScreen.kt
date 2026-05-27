package com.app.flashcontrol.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val isOn by viewModel.isOn.collectAsState()
    val isBlinking by viewModel.isBlinking.collectAsState()
    val blinkInterval by viewModel.blinkInterval.collectAsState()
    val brightness by viewModel.brightness.collectAsState()

    val canControlBrightness = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && viewModel.maxBrightness > 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FlashControl",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 48.dp)
        )

        // Power Button
        Button(
            onClick = { viewModel.toggleFlashlight() },
            modifier = Modifier
                .size(160.dp)
                .shadow(if (isOn) 20.dp else 0.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = if (isOn) "ON" else "OFF",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Blink Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Blink Mode", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
            Switch(
                checked = isBlinking,
                onCheckedChange = { viewModel.setBlinkMode(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isBlinking) {
            Text(
                text = "Interval: ${blinkInterval.toInt()} ms",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )
            Slider(
                value = blinkInterval,
                onValueChange = { viewModel.setBlinkInterval(it) },
                valueRange = 50f..2000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Brightness Control
        if (canControlBrightness) {
            Text(
                text = "Brightness Level: ${brightness.toInt()}",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )
            Slider(
                value = brightness,
                onValueChange = { viewModel.setBrightness(it) },
                valueRange = 1f..viewModel.maxBrightness.toFloat(),
                steps = viewModel.maxBrightness - 2,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        } else {
            Text(
                text = "Brightness control requires Android 13+ and supported hardware.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Safety Warning
        Text(
            text = "WARNING: Continuous use of the flashlight at high brightness may cause the device to overheat. Use with caution.",
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
