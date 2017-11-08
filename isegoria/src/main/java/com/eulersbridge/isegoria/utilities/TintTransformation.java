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

/**
 * Created by Seb on 04/11/2017.
 */

public class TintTransformation extends BitmapTransformation {

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {

        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(Color.argb(128, 0,0,0), PorterDuff.Mode.SRC_ATOP));

        Bitmap bitmap = pool.get(outWidth, outHeight, Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(toTransform, 0, 0, paint);

        return bitmap;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update("au.com.isegoria.app.TintTransformation".getBytes());
    }
}
