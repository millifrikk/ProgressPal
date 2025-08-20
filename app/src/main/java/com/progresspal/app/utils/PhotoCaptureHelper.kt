package com.progresspal.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for capturing photos from camera or selecting from gallery
 * Handles permissions, file creation, and image compression
 */
class PhotoCaptureHelper(private val context: Context) {
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val STORAGE_PERMISSION_REQUEST = 101
        private const val IMAGE_QUALITY = 80 // JPEG compression quality
        private const val MAX_IMAGE_WIDTH = 1080
        private const val MAX_IMAGE_HEIGHT = 1080
    }
    
    private var currentPhotoFile: File? = null
    private var onPhotoReadyCallback: ((File) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
    // Store launcher references for permission callbacks
    private var cameraLauncherRef: ActivityResultLauncher<Intent>? = null
    private var galleryLauncherRef: ActivityResultLauncher<Intent>? = null
    
    /**
     * Request camera permission and capture photo
     */
    fun capturePhoto(
        cameraLauncher: ActivityResultLauncher<Intent>,
        onPhotoReady: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onPhotoReadyCallback = onPhotoReady
        this.onErrorCallback = onError
        
        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera(cameraLauncher)
        } else {
            // Store callbacks for permission result handling
            this.cameraLauncherRef = cameraLauncher
            ActivityCompat.requestPermissions(
                context as FragmentActivity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }
    
    /**
     * Select photo from gallery
     */
    fun selectFromGallery(
        galleryLauncher: ActivityResultLauncher<Intent>,
        onPhotoReady: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onPhotoReadyCallback = onPhotoReady
        this.onErrorCallback = onError
        
        // Check storage permission (for Android 13+ we need READ_MEDIA_IMAGES, for older versions READ_EXTERNAL_STORAGE)
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            launchGallery(galleryLauncher)
        } else {
            // Store callbacks for permission result handling  
            this.galleryLauncherRef = galleryLauncher
            ActivityCompat.requestPermissions(
                context as FragmentActivity,
                arrayOf(permission),
                STORAGE_PERMISSION_REQUEST
            )
        }
    }
    
    private fun launchCamera(cameraLauncher: ActivityResultLauncher<Intent>) {
        try {
            currentPhotoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                currentPhotoFile!!
            )
            
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            
            cameraLauncher.launch(cameraIntent)
        } catch (e: Exception) {
            onErrorCallback?.invoke("Failed to start camera: ${e.message}")
        }
    }
    
    private fun launchGallery(galleryLauncher: ActivityResultLauncher<Intent>) {
        val galleryIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(galleryIntent)
    }
    
    /**
     * Handle camera result - file is already saved to currentPhotoFile
     */
    fun handleCameraResult(success: Boolean) {
        if (success && currentPhotoFile != null && currentPhotoFile!!.exists()) {
            try {
                val compressedFile = compressImage(currentPhotoFile!!)
                onPhotoReadyCallback?.invoke(compressedFile)
            } catch (e: Exception) {
                onErrorCallback?.invoke("Failed to process photo: ${e.message}")
            }
        } else {
            onErrorCallback?.invoke("Failed to capture photo")
        }
    }
    
    /**
     * Handle gallery result - copy and compress the selected image
     */
    fun handleGalleryResult(uri: Uri?) {
        if (uri != null) {
            onPhotoReadyCallback?.let { callback ->
                onErrorCallback?.let { errorCallback ->
                    copyGalleryImageToFile(uri, callback, errorCallback)
                }
            }
        } else {
            onErrorCallback?.invoke("No image selected")
        }
    }
    
    /**
     * Copy image from gallery URI to app's file storage with compression
     * Fixed: Replaced deprecated getBitmap, added proper bitmap recycling, moved to background thread
     */
    fun copyGalleryImageToFile(
        uri: Uri,
        onPhotoReady: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        // Ensure we have FragmentActivity context for coroutines
        val activity = context as? FragmentActivity
        if (activity == null) {
            onError("Context must be FragmentActivity for background processing")
            return
        }
        
        activity.lifecycleScope.launch {
            try {
                val compressedFile = withContext(Dispatchers.IO) {
                    // Use modern ImageDecoder for API 28+ or BitmapFactory with input stream
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        } ?: throw IOException("Failed to open input stream for URI")
                    }
                    
                    try {
                        val file = saveBitmapToFile(bitmap)
                        bitmap.recycle() // Properly recycle the original bitmap
                        file
                    } catch (e: Exception) {
                        bitmap.recycle() // Ensure cleanup even on error
                        throw e
                    }
                }
                
                // Switch back to main thread for callback
                withContext(Dispatchers.Main) {
                    onPhotoReady(compressedFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to process selected image: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Create a unique image file in the app's pictures directory
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "PROGRESS_${timeStamp}"
        val storageDir = File(context.getExternalFilesDir(null), "photos")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, "${imageFileName}.jpg")
    }
    
    /**
     * Compress image to reduce file size while maintaining quality
     * Fixed: Added proper bitmap recycling
     */
    private fun compressImage(imageFile: File): File {
        val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        return try {
            val compressedFile = saveBitmapToFile(originalBitmap)
            originalBitmap.recycle() // Properly recycle original bitmap
            compressedFile
        } catch (e: Exception) {
            originalBitmap.recycle() // Ensure cleanup even on error
            throw e
        }
    }
    
    /**
     * Save bitmap to file with compression and size optimization
     */
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        // Calculate scale factor to maintain aspect ratio
        val scaleFactor = calculateScaleFactor(bitmap.width, bitmap.height)
        
        // Scale bitmap if needed
        val scaledBitmap = if (scaleFactor < 1.0f) {
            val newWidth = (bitmap.width * scaleFactor).toInt()
            val newHeight = (bitmap.height * scaleFactor).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
        
        // Create output file
        val outputFile = createImageFile()
        
        try {
            FileOutputStream(outputFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
            }
        } catch (e: IOException) {
            throw IOException("Failed to save compressed image: ${e.message}")
        } finally {
            // Always recycle the scaled bitmap if it's different from original
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            // Note: Original bitmap is recycled by the caller
        }
        
        return outputFile
    }
    
    /**
     * Calculate scale factor to fit image within max dimensions
     */
    private fun calculateScaleFactor(width: Int, height: Int): Float {
        val widthScale = MAX_IMAGE_WIDTH.toFloat() / width
        val heightScale = MAX_IMAGE_HEIGHT.toFloat() / height
        return minOf(widthScale, heightScale, 1.0f)
    }
    
    /**
     * Delete temporary photo file if capture was cancelled
     */
    fun cleanup() {
        currentPhotoFile?.let { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        currentPhotoFile = null
        onPhotoReadyCallback = null
        onErrorCallback = null
        cameraLauncherRef = null
        galleryLauncherRef = null
    }
    
    /**
     * Handle permission request results
     * Call this method from the Activity's onRequestPermissionsResult
     */
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncherRef?.let { launcher ->
                        launchCamera(launcher)
                    }
                } else {
                    onErrorCallback?.invoke("Camera permission is required to take photos")
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleryLauncherRef?.let { launcher ->
                        launchGallery(launcher)
                    }
                } else {
                    onErrorCallback?.invoke("Storage permission is required to select photos")
                }
            }
        }
    }
}