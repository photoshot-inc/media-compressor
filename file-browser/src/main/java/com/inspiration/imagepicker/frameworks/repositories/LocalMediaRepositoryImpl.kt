package com.inspiration.imagepicker.frameworks.repositories

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.inspiration.imagepicker.data.repositories.LocalMediaRepository
import com.inspiration.imagepicker.data.models.FileDataModel
import java.lang.Exception
import java.lang.IllegalArgumentException

class LocalMediaRepositoryImpl(private val context: Context) : LocalMediaRepository {
    //todo enabled for file absolute path
    private val pathOrRelative =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.DATA
        else
            MediaStore.Images.Media.DATA

    private val projectionImage = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.TITLE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.ORIENTATION,
        MediaStore.Images.Media.SIZE,
        pathOrRelative,
    )
    private val projectionVideo = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME, 
        MediaStore.Video.Media.SIZE,
        pathOrRelative,
    )

    override fun getAllImages(): List<FileDataModel> {
        context.contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projectionImage,
                null,
                null,
                MediaStore.Images.Media._ID + " DESC"

            ).use { cursor ->
                if (cursor == null) return emptyList()
                val items = mutableListOf<FileDataModel>()
                var c = 0

                while (cursor.moveToNext()) {
                    parseImageCursor(cursor)?.let {
                        items.add(it)
                    }
                }
                return items

            }
    }
    override fun getAllVideos(): List<FileDataModel> {
        context.contentResolver
            .query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projectionVideo,
                null,
                null,
                MediaStore.Images.Media._ID + " DESC"

            ).use { cursor ->
                if (cursor == null) return emptyList()
                val items = mutableListOf<FileDataModel>()
                var c = 0
                while (cursor.moveToNext()) {
                    parseVideoCursor(cursor)?.let {
                        items.add(it)
                    }
                }
                return items
            }
    }

    override fun getImageDetails(uri: Uri): FileDataModel {
        return context.contentResolver
            .query(
                uri,
                projectionImage,
                null,
                null,
                MediaStore.Images.Media._ID + " DESC"
            ).use { cursor ->
                if (cursor == null) throw IllegalArgumentException("Details not found")

                cursor.moveToNext()
                return@use parseImageCursor(cursor)!!

            }
    }

    private fun parseImageCursor(cursor: Cursor): FileDataModel? {
        try {
            val id = cursor.getLong(0) //id
            val title: String = cursor.getString(1) //title
            val width = cursor.getString(2) // width
            val height = cursor.getString(3) //height
            val folderName = cursor.getString(4) // bucket display name
            val orientation = cursor.getString(5) //orientation
            val size = cursor.getString(6) //size
            val pathOrRelativePath = cursor.getString(7) //size
            val uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            return FileDataModel(
                uri.toString(),
                title,
                width,
                height,
                folderName,
                orientation,
                size,
                pathOrRelativePath
            )
        } catch (ex: Exception) {
            return null
        }
    }

    private fun parseVideoCursor(cursor: Cursor): FileDataModel? {
        try {
            val id = cursor.getLong(0) //id
            val title: String = cursor.getString(1) //title
            val width = cursor.getString(2) // width
            val height = cursor.getString(3) //height
            val folderName = cursor.getString(4) // bucket display name
//            val orientation = cursor.getString(5) //orientation
            val size = cursor.getString(5) //size
            val pathOrRelativePath = cursor.getString(6) //size
            val uri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                id
            )
            return FileDataModel(
                uri.toString(),
                title,
                width,
                height,
                folderName,
                "0",
                size,
                pathOrRelativePath
            )
        } catch (ex: Exception) {
            return null
        }
    }
}