package com.vajo.cameraapp.ui

import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberImagePainter
import com.vajo.booktrack.ui.view_model.CameraViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService


@Composable
fun Gallery(cameraViewModel: CameraViewModel = CameraViewModel()){
    val camState = cameraViewModel.cameraState.collectAsState()

    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize()){
            for(i in camState.value.photosListState){
                Row(modifier = Modifier.height(100.dp)){
                    Image(
                        painter = rememberImagePainter(i),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight
                    )
                    Text(text = i.toString())
                }
            }
        }

        Button(
            modifier = Modifier.padding(25.dp),
            onClick = { cameraViewModel.enableCameraPreview(true) }
        ) {
            Icon(Icons.Default.Add, "Open Camera Preview")
        }
    }
}

@Composable
fun CameraView(cameraViewModel: CameraViewModel, previewView: PreviewView, imageCapture: ImageCapture, cameraExecutor: ExecutorService, directory: File){
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()){
        AndroidView({previewView}, modifier = Modifier.fillMaxSize())
        Button(
            modifier = Modifier.padding(25.dp),
            onClick = {
                val photoFile = File(
                    directory,
                    SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
                )

                imageCapture.takePicture(
                    ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                    cameraExecutor,
                    object: ImageCapture.OnImageSavedCallback {
                        override fun onError(exception: ImageCaptureException) {
                            Log.e("camApp","Error when capturing image")
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            cameraViewModel.setNewUri(Uri.fromFile(photoFile))
                        }
                    }
                )
            }
        ) {
            Icon(Icons.Default.AddCircle, "Take Photo", tint = Color.White)
        }
    }
}
