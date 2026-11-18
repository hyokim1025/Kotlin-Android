package com.example.bookmarket

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.AtomicFile
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.roundToInt

internal class ProfilePhotoStore(context: Context) {
    private val app = context.applicationContext
    private val resolver = app.contentResolver
    private val savedPhoto = AtomicFile(File(app.filesDir, "profile_photo.jpg"))
    val captureFile = File(app.cacheDir, "profile_capture/pending.jpg")

    fun prepareCapture() {
        val directory = captureFile.parentFile ?: throw IOException("No capture directory")
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create capture directory")
        }
        if (captureFile.exists() && !captureFile.delete()) {
            throw IOException("Cannot replace previous capture file")
        }
    }

    fun read(): Bitmap? = synchronized(fileLock) {
        try {
            savedPhoto.openRead().use { BitmapFactory.decodeStream(it) }
        } catch (_: FileNotFoundException) {
            null
        } catch (_: IOException) {
            null
        }
    }

    fun save(uri: Uri): Bitmap {
        val photo = if (Build.VERSION.SDK_INT >= 28) decodeModern(uri) else decodeLegacy(uri)

        synchronized(fileLock) {
            val output = savedPhoto.startWrite()
            var completed = false
            try {
                if (!photo.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                    throw IOException("Could not encode profile photo")
                }
                savedPhoto.finishWrite(output)
                completed = true
            } finally {
                if (!completed) savedPhoto.failWrite(output)
            }
        }

        return photo
    }

    @RequiresApi(28)
    private fun decodeModern(uri: Uri): Bitmap {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val (width, height) = profileSize(info.size.width, info.size.height)
            decoder.setTargetSize(width, height)
        }
    }

    private fun decodeLegacy(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Invalid image")

        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > PROFILE_SIZE * 2) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = open(uri).use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Invalid image")

        val orientation = try {
            open(uri).use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (_: IOException) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val rotation = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(-90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            }
        }
        val oriented = if (rotation.isIdentity) decoded else {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, rotation, true)
        }
        val (width, height) = profileSize(oriented.width, oriented.height)
        return Bitmap.createScaledBitmap(oriented, width, height, true)
    }

    private fun open(uri: Uri) = resolver.openInputStream(uri)
        ?: throw IOException("Could not open selected image")

    private fun profileSize(width: Int, height: Int): Pair<Int, Int> {
        val scale = minOf(1.0, PROFILE_SIZE.toDouble() / maxOf(width, height))
        return (width * scale).roundToInt().coerceAtLeast(1) to
            (height * scale).roundToInt().coerceAtLeast(1)
    }

    private companion object {
        const val PROFILE_SIZE = 512
        val fileLock = Any()
    }
}
