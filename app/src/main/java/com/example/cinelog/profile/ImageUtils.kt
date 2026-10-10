package com.example.cinelog.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageUtils {

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return runCatching {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            inputStream = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()
            bitmap
        }.getOrNull()
    }

    fun cropAndResizeBitmap(
        source: Bitmap,
        cropCenterXPercent: Float,
        cropCenterYPercent: Float,
        zoomScale: Float,
        targetSize: Int = 256,
    ): Bitmap {
        val srcWidth = source.width
        val srcHeight = source.height

        val minSide = kotlin.math.min(srcWidth, srcHeight)
        val cropSide = (minSide / max(1f, zoomScale)).toInt().coerceIn(1, minSide)

        val centerX = (srcWidth * cropCenterXPercent).toInt()
        val centerY = (srcHeight * cropCenterYPercent).toInt()

        val left = (centerX - cropSide / 2).coerceIn(0, srcWidth - cropSide)
        val top = (centerY - cropSide / 2).coerceIn(0, srcHeight - cropSide)

        val cropped = Bitmap.createBitmap(source, left, top, cropSide, cropSide)
        return Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 82): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val bytes = outputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
