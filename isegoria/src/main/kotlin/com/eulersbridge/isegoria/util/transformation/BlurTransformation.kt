package com.eulersbridge.isegoria.util.transformation

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.support.annotation.FloatRange
import android.support.annotation.Px
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.eulersbridge.isegoria.APP_ID
import java.lang.Math.min
import java.lang.Math.round
import java.lang.ref.WeakReference
import java.security.MessageDigest

class BlurTransformation @JvmOverloads constructor(
    context: Context, // Blur radius in DP
    @param:FloatRange(from = 1.0, to = 25.0) private val blurRadius: Double = 25.0
) : BitmapTransformation() {

    companion object {
        private const val ID = "$APP_ID.BlurTransformation"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
        val screenDensity = Resources.getSystem().displayMetrics.density
    }

    private val weakContext: WeakReference<Context> = WeakReference(context)

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val context = weakContext.get()
        if (context == null || blurRadius <= 0)
            return toTransform

        @Px val blurRadius = min(round(blurRadius * screenDensity), 25)

        if (blurRadius <= 0)
            return toTransform

        val renderScript = RenderScript.create(context)
        val outputBitmap = toTransform.copy(toTransform.config, true)
        val tmpIn = Allocation.createFromBitmap(renderScript, toTransform)
        val tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)

        val blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blur.apply {
            setRadius(blurRadius.toFloat())
            setInput(tmpIn)
            forEach(tmpOut)
        }

        tmpOut.copyTo(outputBitmap)

        return outputBitmap
    }

    override fun equals(other: Any?) = other is BlurTransformation

    override fun hashCode() = ID.hashCode()

    override fun updateDiskCacheKey(messageDigest: MessageDigest) = messageDigest.update(ID_BYTES)
}
