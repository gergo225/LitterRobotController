package com.gergo225.litterrobotcontroller

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {

    private val _permissionsGranted = MutableStateFlow<Boolean?>(null)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    fun initialize(allPermissionsGranted: Boolean) {
        _permissionsGranted.value = allPermissionsGranted
    }

    fun onPermissionsGranted() {
        _permissionsGranted.value = true
    }
}
