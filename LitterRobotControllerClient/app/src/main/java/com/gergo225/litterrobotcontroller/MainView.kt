package com.gergo225.litterrobotcontroller

import android.annotation.SuppressLint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("MissingPermission")
@Composable
fun MainView(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    val permissionsGranted by viewModel.permissionsGranted.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.initialize(haveAllBlePermissions(context))
    }

    when (permissionsGranted) {
        false -> {
            PermissionsRequiredPage { viewModel.onPermissionsGranted() }
        }

        true -> {
            ScanPage()
        }

        else -> {
            CircularProgressIndicator()
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainPreview() {
    MainView()
}
