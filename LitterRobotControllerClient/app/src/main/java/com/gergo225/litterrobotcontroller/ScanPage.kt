package com.gergo225.litterrobotcontroller

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@RequiresPermission(allOf = [BluetoothPermissions.BLUETOOTH_SCAN, BluetoothPermissions.BLUETOOTH_CONNECT])
fun ScanPage(viewModel: ScanViewModel = viewModel()) {
    val context = LocalContext.current

    val foundDevices by viewModel.foundDevices.collectAsState()

    LaunchedEffect(viewModel) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        viewModel.initialize(bluetoothManager)
    }


    if (foundDevices.isEmpty()) {
        CircularProgressIndicator()
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.Start
        ) {
            items(foundDevices) {
                Text(it.name ?: it.address)
            }
        }
    }
}
