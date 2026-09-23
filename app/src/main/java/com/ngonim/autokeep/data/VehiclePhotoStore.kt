package com.ngonim.autokeep.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

object VehiclePhotoStore {
    fun save(context: Context, source: Uri): String {
        val directory = File(context.filesDir, "vehicle_photos").apply { mkdirs() }
        val destination = File(directory, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not read the selected photo")
        return destination.absolutePath
    }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    fun decode(path: String?, maxWidth: Int = 1200): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val sample = sampleSize(bounds.outWidth, maxWidth)
        return BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sample },
        )
    }

    private fun sampleSize(width: Int, maxWidth: Int): Int {
        if (width <= maxWidth || width <= 0) return 1
        var size = 1
        while (width / size > maxWidth) size *= 2
        return size
    }
}
