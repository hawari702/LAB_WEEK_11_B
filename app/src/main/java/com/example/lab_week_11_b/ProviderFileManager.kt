package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import java.io.File
import java.util.concurrent.Executor

class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {

    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"

        // Folder dari fungsi global (bukan dari FileHelper)
        val folder = getPicturesFolder()

        val file = File(
            context.getExternalFilesDir(folder),
            name
        )

        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            folder,
            "image/jpeg"
        )
    }

    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"

        val folder = getVideosFolder()

        val file = File(
            context.getExternalFilesDir(folder),
            name
        )

        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            folder,
            "video/mp4"
        )
    }

    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let { info ->
            insertToStore(
                info,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(info)
            )
        }
    }

    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let { info ->
            insertToStore(
                info,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(info)
            )
        }
    }

    private fun insertToStore(
        fileInfo: FileInfo,
        contentUri: Uri,
        contentValues: ContentValues
    ) {
        executor.execute {
            val insertedUri = contentResolver.insert(contentUri, contentValues)

            insertedUri?.let { uri ->
                val input = contentResolver.openInputStream(fileInfo.uri)
                val output = contentResolver.openOutputStream(uri)

                input?.use { inputStream ->
                    output?.use { outputStream ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (true) {
                            bytesRead = inputStream.read(buffer)
                            if (bytesRead == -1) break
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
        }
    }
}
