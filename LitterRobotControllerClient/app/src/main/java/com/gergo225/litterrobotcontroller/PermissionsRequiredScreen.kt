package com.gergo225.litterrobotcontroller

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PermissionsRequiredScreen(onPermissionsGranted: () -> Unit) {
    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Need permissions to access Bluetooth")
            GrantBlePermissionsButton(onPermissionGranted = onPermissionsGranted)
        }
    }
}
