package com.gergo225.litterrobotcontroller

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@RequiresPermission(allOf = [BluetoothPermissions.BLUETOOTH_SCAN, BluetoothPermissions.BLUETOOTH_CONNECT])
fun ScanPage(viewModel: ScanViewModel = viewModel()) {
    val context = LocalContext.current

    val scanState by viewModel.scanState.collectAsState()

    LaunchedEffect(viewModel) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        viewModel.initialize(bluetoothManager)
    }


    when (scanState) {
        ScanState.SCANNING -> ScanInProgressScreen()
        ScanState.CONNECTING -> ConnectingToDeviceScreen()
        ScanState.CONNECTED -> ConnectedToDeviceScreen(
            onRotateRight = { viewModel.rotateRight() },
            onRotateLeft = { viewModel.rotateLeft() },
            onStop = { viewModel.stopMotor() }
        )
    }
}

@Composable
private fun ScanInProgressScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Searching for device")
        CircularProgressIndicator()
    }
}

@Composable
private fun ConnectingToDeviceScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Found device")
        Text("Connecting to device...")
        CircularProgressIndicator()
    }
}

@Composable
private fun ConnectedToDeviceScreen(
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Text("Connected to 'LitterRobot Motor' device")
            Spacer(modifier = Modifier.height(100.dp))
        }

        MotorControlsView(
            onRotateLeft = onRotateLeft,
            onRotateRight = onRotateRight,
            onStop = onStop
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun ConnectedToDevicePreview() {
    ConnectedToDeviceScreen({}, {}, {})
}
