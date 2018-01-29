package com.eulersbridge.isegoria.util.transformation

import android.graphics.*
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.eulersbridge.isegoria.APP_ID
import java.security.MessageDigest

class TintTransformation @JvmOverloads constructor(
    private val alpha: Double = 0.65,
    @param:ColorInt @field:ColorInt private val colour: Int = Color.rgb(
        49,
        62,
        77
    )
) : BitmapTransformation() {

    companion object {
        private const val ID = "$APP_ID.TintTransformation"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }

    public override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {

        val bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        canvas.drawBitmap(toTransform, 0f, 0f, paint)

        val ratio = 1.0f - alpha.toFloat()
        /* White with Multiply blend mode is effectively transparent,
            whereas an actual color with 0% alpha causes a grey canvas. */
        @ColorInt val blendedColor = ColorUtils.blendARGB(colour, Color.WHITE, ratio)

        canvas.drawColor(blendedColor, PorterDuff.Mode.MULTIPLY)

        return bitmap
    }

    override fun equals(other: Any?) = other is TintTransformation

    override fun hashCode() = ID.hashCode()

    override fun updateDiskCacheKey(messageDigest: MessageDigest) = messageDigest.update(ID_BYTES)
}
