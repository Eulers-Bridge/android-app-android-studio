package com.eulersbridge.isegoria.vote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Signature extends View {
    private static final float STROKE_WIDTH_PX = 5f;
    private static final float HALF_STROKE_WIDTH_PX = STROKE_WIDTH_PX / 2;
    private final Paint paint;
    private final Path path;

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect;

    /*private LinearLayout mContent;
    private Bitmap mBitmap;
    private File mypath;*/

    public Signature(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH_PX);

        path = new Path();

        dirtyRect = new RectF();
    }

    /*public void save(View v) {
        Log.v(getClass().getSimpleName(), "Width: " + v.getWidth());
        Log.v(getClass().getSimpleName(), "Height: " + v.getHeight());

        if (mBitmap == null)
            mBitmap =  Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(mBitmap);
        try {
            FileOutputStream mFileOutStream = new FileOutputStream(mypath);

            v.draw(canvas);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();

            //String url = Images.Media.insertImage(getContentResolver(), mBitmap, "title", null);
            //Log.v(getClass().getSimpleName(),"url: " + url);

        } catch(Exception e) {
            Log.v(getClass().getSimpleName(), e.toString());
        }
    }*/

    public void clear() {
        path.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;
                return true;

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:

                resetDirtyRect(eventX, eventY);
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }

                path.lineTo(eventX, eventY);

                break;

            default:
                return false;
        }

        invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH_PX),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH_PX),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH_PX),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH_PX));

        lastTouchX = eventX;
        lastTouchY = eventY;

        return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }

        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }
}