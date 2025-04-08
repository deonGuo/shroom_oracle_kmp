package com.deon.shroom_oracle_kmp

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers


@Composable
fun ImagePickerScreen() {
    val context = LocalContext.current
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var oracleResult by remember { mutableStateOf<OracleResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val pickImage = rememberImagePicker { bytes ->
        imageBytes = bytes
        oracleResult = null
        errorMessage = null

        bitmap = bytes?.let {
            val uri = byteArrayToTempUri(context, it)
            uri?.let { decodeImage(context, it) }
        }

        if (bytes != null) {
            scope.launch {
                isLoading = true
                try {
                    val compressedBytes = compressImageBytes(context, bytes)
                    val result = OracleApi.sendImageToOracle(compressedBytes)
                    oracleResult = result
                } catch (e: Exception) {
                    errorMessage = "Failed to contact oracle: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { pickImage() }) {
            Text("Reveal thy fungal truth, O Oracle")
        }

        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 16.dp)
            )
        }

        when {
            isLoading -> CircularProgressIndicator()

            oracleResult != null -> {
                Text("🍄 Name: ${oracleResult!!.name}", fontSize = 18.sp)
                Text("📊 Confidence: ${(oracleResult!!.confidence * 100).toInt()}%", fontSize = 16.sp)
                Text("✅ Edible: ${if (oracleResult!!.isEdible) "Yes" else "No"}", fontSize = 16.sp)
                oracleResult!!.infoUrl?.let { url ->
                    Text("🔗 Info: $url", fontSize = 14.sp)
                }
            }

            errorMessage != null -> {
                Text("❌ $errorMessage", color = MaterialTheme.colors.error)
            }
        }
    }
}

suspend fun compressImageBytes(context: Context, originalBytes: ByteArray): ByteArray {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = ByteArrayInputStream(originalBytes)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            originalBytes // fallback: return original if compression fails
        }
    }
}

fun decodeImage(context: Context, uri: Uri): ImageBitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source).asImageBitmap()
        } else {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.asImageBitmap()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Optional utility: convert image bytes to a temp URI so we can decode it
fun byteArrayToTempUri(context: Context, bytes: ByteArray): Uri? {
    return try {
        val file = File.createTempFile("shroom_", ".jpg", context.cacheDir)
        file.writeBytes(bytes)
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

