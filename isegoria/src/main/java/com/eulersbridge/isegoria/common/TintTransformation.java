package com.eulersbridge.isegoria.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class TintTransformation extends BitmapTransformation {
    private static final String ID = String.format("%s.TintTransformation", Constant.APP_ID);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final double alpha;
    private final @ColorInt int colour;

    public TintTransformation() {
        this(0.65);
    }

    public TintTransformation(double alpha) {
        this(alpha, Color.rgb(49, 62, 77));
    }

    public TintTransformation(double alpha, @ColorInt int colour) {
        this.alpha = alpha;
        this.colour = colour;
    }

    @Override
    public Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        Bitmap bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(toTransform, 0, 0, paint);

        float ratio = 1.0f - (float)alpha;
        /* White with Multiply blend mode is effectively transparent,
            whereas an actual color with 0% alpha causes a grey canvas. */
        @ColorInt int blendedColor = ColorUtils.blendARGB(colour, Color.WHITE, ratio);

        canvas.drawColor(blendedColor, PorterDuff.Mode.MULTIPLY);

        return bitmap;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TintTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
