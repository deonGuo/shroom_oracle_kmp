package com.deon.shroom_oracle_kmp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.IOException

@Composable
fun rememberImagePicker(
    onImagePicked: (ByteArray?) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                onImagePicked(bytes) // could be null if reading failed
            } catch (e: IOException) {
                e.printStackTrace()
                onImagePicked(null)
            }
        } else {
            onImagePicked(null) // user cancelled
        }
    }

    return {
        launcher.launch("image/*")
    }
}
