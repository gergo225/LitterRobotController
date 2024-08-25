package com.gergo225.litterrobotcontroller

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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
        ScanState.CONNECTED -> ConnectedToDeviceScreen()
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
private fun ConnectedToDeviceScreen() {
    Text("Connected to 'LitterRobot Motor' device")
}