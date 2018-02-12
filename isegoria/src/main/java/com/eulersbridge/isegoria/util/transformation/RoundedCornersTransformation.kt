package com.eulersbridge.isegoria.util.transformation

import android.graphics.*
import android.support.annotation.IntRange
import android.support.annotation.Px
import androidx.graphics.applyCanvas
import androidx.graphics.toXfermode
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.eulersbridge.isegoria.APP_ID
import java.security.MessageDigest

class RoundedCornersTransformation(
    // Radius of rounded corners in DP
    @IntRange(from = 1) private val cornerRadius: Int
) : BitmapTransformation() {

    companion object {
        private const val ID = "$APP_ID.RoundedCornersTransformation"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)

        var screenDensity = 2.0f
    }

    constructor() : this(1)

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)

        return bitmap.applyCanvas {
            val rect = RectF(0f, 0f, outWidth.toFloat(), outHeight.toFloat())
            @Px val cornerRadiusPx = Math.round(cornerRadius.toFloat() * screenDensity)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
            paint.color = Color.BLACK

            drawRoundRect(rect, cornerRadiusPx.toFloat(), cornerRadiusPx.toFloat(), paint)

            paint.xfermode = PorterDuff.Mode.SRC_IN.toXfermode()

            drawBitmap(toTransform, 0f, 0f, paint)
        }
    }

    override fun equals(other: Any?) = other is RoundedCornersTransformation

    override fun hashCode() = ID.hashCode()

    override fun updateDiskCacheKey(messageDigest: MessageDigest) = messageDigest.update(ID_BYTES)
}
