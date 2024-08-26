package com.gergo225.litterrobotcontroller

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ScanState {
    SCANNING,
    CONNECTING,
    CONNECTED
}

class ScanViewModel(private val application: Application) : AndroidViewModel(application) {
    private var scanner: BluetoothLeScanner? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var connectedDevice: BluetoothGatt? = null

    private val _scanState = MutableStateFlow(ScanState.SCANNING)
    val scanState = _scanState.asStateFlow()

    companion object {
        private const val MOTOR_CONTROLLER_DEVICE_NAME = "Litter Robot Motor"
        private val MOTOR_CONTROLLER_SERVICE_UUID =
            UUID.fromString("91bad492-b950-4226-aa2b-4ede9fa42f59")
        private val MOTOR_CONTROLLER_CHARACTERISTIC_UUID =
            UUID.fromString("ca73b3ba-39f6-4ab3-91ae-186dc9577d99")
        private const val MOTOR_ROTATE_LEFT_VALUE = "left"
        private const val MOTOR_ROTATE_RIGHT_VALUE = "right"
        private const val MOTOR_STOP_VALUE = "stop"
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
        @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                // TODO: handle connection error (display on UI)
                Log.w("ScanViewModel (connection callback)", "Failed connecting to device")
            }

            if (newState == BluetoothGatt.STATE_CONNECTED && gatt != null) {
                connectedToDevice(gatt)
            }
        }

        @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            discoveredServices(gatt?.services.orEmpty())
        }
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_SCAN)
    fun initialize(bluetoothManager: BluetoothManager?) {
        scanner = bluetoothManager?.adapter?.bluetoothLeScanner
        startScanning()
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    fun rotateLeft() {
        writeToMotorControllerCharacteristic(MOTOR_ROTATE_LEFT_VALUE)
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    fun rotateRight() {
        writeToMotorControllerCharacteristic(MOTOR_ROTATE_RIGHT_VALUE)
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    fun stopMotor() {
        writeToMotorControllerCharacteristic(MOTOR_STOP_VALUE)
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

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    private fun connectedToDevice(gatt: BluetoothGatt) {
        connectedDevice = gatt
        connectedDevice?.discoverServices()
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    private fun discoveredServices(services: List<BluetoothGattService>) {
        if (services.find { it.uuid == MOTOR_CONTROLLER_SERVICE_UUID } == null) {
            // TODO: handle state when didn't find needed service
            return
        }
        _scanState.value = ScanState.CONNECTED
    }

    @RequiresPermission(BluetoothPermissions.BLUETOOTH_CONNECT)
    @Suppress("DEPRECATION")
    private fun writeToMotorControllerCharacteristic(valueToWrite: String) {
        val service = connectedDevice?.getService(MOTOR_CONTROLLER_SERVICE_UUID) ?: return
        val characteristic =
            service.getCharacteristic(MOTOR_CONTROLLER_CHARACTERISTIC_UUID) ?: return
        characteristic.value = valueToWrite.toByteArray()
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        connectedDevice?.writeCharacteristic(characteristic)
    }
}
