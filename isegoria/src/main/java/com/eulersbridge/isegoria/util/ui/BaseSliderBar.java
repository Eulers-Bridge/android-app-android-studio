package com.eulersbridge.isegoria.util.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.eulersbridge.isegoria.R;

import java.util.ArrayList;
import java.util.List;

public class BaseSliderBar extends View {

    private int parentWidth;
    private int parentHeight;

    private int y;

    private final int circleRadius = 20;
    private final int circleStrokeWidth = 4;
    protected final int horizontalPadding = (circleRadius + (6 * circleStrokeWidth))/2;

    private final List<SliderBarPoint> points = new ArrayList<>();

    private int dragX = -1;
    private SliderBarPoint currentPoint;

    private Paint circleFillPaint;
    private Paint circleStrokePaint;

    private Paint textPaint;

    public BaseSliderBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupPaints();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec) - horizontalPadding;
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        y = (parentHeight/2);
    }

    protected void addPoint(SliderBarPoint point) {
        points.add(point);
    }

    protected List<SliderBarPoint> getPoints() {
        return points;
    }

    protected int getParentWidth() {
        return parentWidth;
    }

    protected int getParentHeight() {
        return parentHeight;
    }

    protected void setupPaints() {
        Context context = getContext();

        circleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleFillPaint.setColor(ContextCompat.getColor(context, R.color.white));
        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setStrokeWidth(circleStrokeWidth);

        circleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleStrokePaint.setColor(ContextCompat.getColor(context, R.color.lightBlue));
        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setStrokeWidth(circleStrokeWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(context, R.color.slider_text));

        // Convert text size from SP (screen density dependent) to pixels
        int pixelSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                getResources().getDisplayMetrics());

        textPaint.setTextSize(pixelSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentPoint == null)
            currentPoint = points.get((points.size() - 1) / 2); // Centre point

        int lineY = parentHeight / 2;

        // Circle
        int circleX;
        if (dragX >= 0) {
            circleX = dragX;
        } else {
            circleX = currentPoint.getX();
        }

        canvas.drawCircle(circleX, y, circleRadius + 8, circleFillPaint);
        canvas.drawCircle(circleX, y, circleRadius, circleStrokePaint);

        String answer = currentPoint.getAnswer();
        canvas.drawText(answer, (parentWidth - textPaint.measureText(answer))/2, lineY + 50,
                textPaint);
    }

    /**
     * Snap to the closest notch/point once the user has released the slider / finished draggin
     */
    private void snapToPoint(int x) {
        int currentDistance = parentWidth;
        SliderBarPoint newPoint = null;

        for (SliderBarPoint point : points) {
            if (Math.abs(point.getX() - x) < currentDistance) {
                currentDistance = Math.abs(point.getX() - x);
                newPoint = point;
            }
        }

        if (newPoint != null) {
            currentPoint = newPoint;

            invalidate();
        }
    }

    public int getScore() {
        return points.indexOf(currentPoint) + 1;
    }

    public void setScore(int score) {
        currentPoint = points.get(score - 1);
    }

    @Override
    public boolean performClick() {
        /*
            Calls the super implementation, which generates an AccessibilityEvent
            and calls the onClick() listener on the view, if any
         */
        super.performClick();

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();

        // Let the user drag the slider, and snap to a point once they let go
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            dragX = -1;
            snapToPoint(x);

            performClick();

        } else if (x >= horizontalPadding && x <= parentWidth - horizontalPadding) {
            dragX = x;
            invalidate();

            return true; // Interested in any more events in this gesture (to wait for touch up/cancel)
        }

        return false; // Not interested in any further events in this gesture
    }
}
