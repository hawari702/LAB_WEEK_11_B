package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    // Request code for permission request to external storage
    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // Helper class to manage files in MediaStore
    private lateinit var providerFileManager: ProviderFileManager

    // Data model for the file
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null

    // Flag to indicate whether the user is capturing a photo or video
    private var isCapturingVideo = false

    // Activity result launcher to capture images and videos
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the ProviderFileManager
        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        // Initialize the activity result launchers
        // TakePicture() and CaptureVideo() are the built-in contracts
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) {
                // setelah foto diambil, simpan ke MediaStore
                providerFileManager.insertImageToStore(photoInfo)
            }

        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
                // setelah video diambil, simpan ke MediaStore
                providerFileManager.insertVideoToStore(videoInfo)
            }

        // Button capture photo
        findViewById<Button>(R.id.photo_button).setOnClickListener {
            // User is capturing a photo
            isCapturingVideo = false

            // Check the storage permission, then open camera
            checkStoragePermission {
                openImageCapture()
            }
        }

        // Button capture video
        findViewById<Button>(R.id.video_button).setOnClickListener {
            // User is capturing a video
            isCapturingVideo = true

            // Check the storage permission, then open camera
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    // Open the camera to capture an image
    // Open the camera to capture an image
    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())

        photoInfo?.uri?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    // Open the camera to capture a video
    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())

        videoInfo?.uri?.let { uri ->
            takeVideoLauncher.launch(uri)
        }
    }

    // Check the storage permission
    // For Android 10 and above, the permission is not required
    // For Android 9 and below, the permission is required
    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Check for the WRITE_EXTERNAL_STORAGE permission
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                // If the permission is granted
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                // If the permission is not granted, request the permission
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            // Android 10+ tidak butuh permission ini
            onPermissionGranted()
        }
    }

    // For Android 9 and below: handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
            }
            else -> {
                // do nothing for other request codes
            }
        }
    }
}
