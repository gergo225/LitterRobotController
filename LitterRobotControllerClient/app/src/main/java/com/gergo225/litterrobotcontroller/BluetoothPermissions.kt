package com.gergo225.litterrobotcontroller

object BluetoothPermissions {
    //These fields are marked as API >= 31 in the Manifest class, so we can't use those without warning.
    //So we create our own, which prevents over-suppression of the Linter
    const val BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
    const val BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"
}
