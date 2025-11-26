package com.example.lab_week_11_b

import android.content.ContentValues
import android.provider.MediaStore

class MediaContentHelper {

    fun getImageContentUri() = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    fun getVideoContentUri() = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    fun generateImageContentValues(fileInfo: FileInfo): ContentValues {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileInfo.name)
        values.put(MediaStore.Images.Media.MIME_TYPE, fileInfo.mimeType)
        values.put(MediaStore.Images.Media.RELATIVE_PATH, getPicturesFolder())

        return values
    }

    fun generateVideoContentValues(fileInfo: FileInfo): ContentValues {
        val values = ContentValues()

        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileInfo.name)
        values.put(MediaStore.Video.Media.MIME_TYPE, fileInfo.mimeType)
        values.put(MediaStore.Video.Media.RELATIVE_PATH, getVideosFolder())

        return values
    }
}
