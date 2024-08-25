package com.gergo225.litterrobotcontroller

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ScanState {
    SCANNING,
    CONNECTING,
    CONNECTED
}

class ScanViewModel(private val application: Application) : AndroidViewModel(application) {
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothDevice: BluetoothDevice? = null

    private val _scanState = MutableStateFlow(ScanState.SCANNING)
    val scanState = _scanState.asStateFlow()

    companion object {
        private const val MOTOR_CONTROLLER_DEVICE_NAME = "Litter Robot Motor"
    }

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(allOf = [BluetoothPermissions.BLUETOOTH_CONNECT, BluetoothPermissions.BLUETOOTH_SCAN])
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            result ?: return
            val device = result.device
            if (device.name == MOTOR_CONTROLLER_DEVICE_NAME) {
                foundBluetoothDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.w("ScanViewModel (scan callback)", "Scan failed")
        }
    }

    private val connectCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                // TODO: handle connection error (display on UI)
                Log.w("ScanViewModel (connection callback)", "Failed connecting to device")
            }

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                connectedToDevice()
            }
        }
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_SCAN)
    fun initialize(bluetoothManager: BluetoothManager?) {
        scanner = bluetoothManager?.adapter?.bluetoothLeScanner
        startScanning()
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_SCAN)
    private fun startScanning() {
        _scanState.value = ScanState.SCANNING
        scanner?.startScan(scanCallback)
    }

    @RequiresPermission(allOf = [BluetoothPermissions.BLUETOOTH_SCAN, BluetoothPermissions.BLUETOOTH_CONNECT])
    private fun foundBluetoothDevice(device: BluetoothDevice) {
        scanner?.stopScan(scanCallback)
        bluetoothDevice = device
        connectToDevice()
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    private fun connectToDevice() {
        _scanState.value = ScanState.CONNECTING
        bluetoothDevice?.connectGatt(application, false, connectCallback)
    }

    private fun connectedToDevice() {
        _scanState.value = ScanState.CONNECTED
    }
}
