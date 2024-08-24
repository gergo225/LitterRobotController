package com.gergo225.litterrobotcontroller

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainView(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    var allPermissionGranted by remember {
        mutableStateOf(haveAllBlePermissions(context))
    }

    if (!allPermissionGranted) {
        PermissionsRequiredScreen { allPermissionGranted = true }
    } else {
        Text("Permissions granted")
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainPreview() {
    MainView()
}
