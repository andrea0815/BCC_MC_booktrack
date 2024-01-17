package com.vajo.booktrack.ui.view_model

import android.net.Uri

data class CameraState (
    val photosListState: List<Uri> = emptyList(),
    val enableCameraPreview: Boolean = false,
    val cameraPermissionGranted: Boolean = false
)