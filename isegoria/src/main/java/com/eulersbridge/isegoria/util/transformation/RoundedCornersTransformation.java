package com.eulersbridge.isegoria.util.transformation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.eulersbridge.isegoria.util.Constants;

import java.security.MessageDigest;

public class RoundedCornersTransformation extends BitmapTransformation {
    private static final String ID = String.format("%s.RoundedCornersTransformation", Constants.APP_ID);
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    // Radius of rounded corners in DP
    private final int cornerRadius;

    private static float screenDensity = Resources.getSystem().getDisplayMetrics().density;

    public RoundedCornersTransformation() {
        this(0);
    }

    @SuppressWarnings("WeakerAccess")
    public RoundedCornersTransformation(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap bitmap = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        final RectF rect = new RectF(0, 0, outWidth, outHeight);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setColor(Color.BLACK);

        int cornerRadiusPx = Math.round((float)cornerRadius * screenDensity);
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(toTransform, 0, 0, paint);

        return bitmap;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RoundedCornersTransformation;
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
