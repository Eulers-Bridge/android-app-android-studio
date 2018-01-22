package com.eulersbridge.isegoria.util.transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.eulersbridge.isegoria.util.Constants;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;

public class BlurTransformation extends BitmapTransformation {
    private static final String ID = String.format("%s.BlurTransformation", Constants.APP_ID);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    // Blur radius in DP
    private final int blurRadius;
    public static float screenDensity = 2.0f;

    private final WeakReference<Context> weakContext;

    public BlurTransformation(Context context) {
        this(context, 25);
    }

    public BlurTransformation(Context context, @IntRange(from = 1, to = 25) int blurRadius) {
        this.weakContext = new WeakReference<>(context);
        this.blurRadius = blurRadius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Context context = weakContext.get();
        if (context == null || blurRadius <= 0) return toTransform;

        int blurRadiusPx = Math.round((float)blurRadius * screenDensity);
        if (blurRadiusPx > 25) blurRadiusPx = 25;

        if (blurRadiusPx <= 0) return toTransform;

        Bitmap outputBitmap = toTransform.copy(toTransform.getConfig(), true);

        final RenderScript renderScript = RenderScript.create(context);

        Allocation tmpIn = Allocation.createFromBitmap(renderScript, toTransform);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        final ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blur.setRadius(blurRadiusPx);
        blur.setInput(tmpIn);
        blur.forEach(tmpOut);

        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlurTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
