package com.gergo225.litterrobotcontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScanViewModel : ViewModel() {
    private var scanner: BluetoothLeScanner? = null

    private val _foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            result ?: return
            val device = result.device
            if (!foundDevices.value.contains(device)) {
                _foundDevices.update { it + device }
            }

            // TODO: see if found device with needed UUID
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.w("ScanViewModel (scan callback)", "Scan failed")
        }
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_SCAN)
    fun initialize(bluetoothManager: BluetoothManager?) {
        scanner = bluetoothManager?.adapter?.bluetoothLeScanner
        startScanning()
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_SCAN)
    private fun startScanning() {
        scanner?.startScan(scanCallback)
    }
}
