package com.eulersbridge.isegoria.utilities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class TintTransformation extends BitmapTransformation {
    private static final String ID = "au.com.isegoria.app.TintTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final double alphaFactor;

    public TintTransformation() {
        this(0.5);
    }

    public TintTransformation(double alphaFactor) {
        this.alphaFactor = alphaFactor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {

        int alpha = (int) Math.round(alphaFactor * (double)255);

        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(Color.argb(alpha, 0,0,0), PorterDuff.Mode.SRC_ATOP));

        Bitmap bitmap = pool.get(outWidth, outHeight, Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(toTransform, 0, 0, paint);

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
