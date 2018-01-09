package com.eulersbridge.isegoria.election;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.eulersbridge.isegoria.views.BaseSliderBar;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.views.SliderBarPoint;

import java.util.ArrayList;
import java.util.List;

public class SelfEfficacySliderBar extends BaseSliderBar {

    private ArrayList<Paint> paints;
    private Paint notchPaint;

    public SelfEfficacySliderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setupPoints();
    }

    private void setupPoints() {
        if (getPoints().size() != 5) {
            int parentWidth = getParentWidth();
            int pointY = getParentHeight() / 2;
            Resources resources = getResources();

            addPoint(new SliderBarPoint(horizontalPadding, pointY, resources.getString(R.string.self_efficacy_slider_not_at_all)));
            addPoint(new SliderBarPoint(parentWidth/4, pointY, resources.getString(R.string.self_efficacy_slider_unlikely)));
            addPoint(new SliderBarPoint((parentWidth/4)*2, pointY, resources.getString(R.string.self_efficacy_slider_neutral)));
            addPoint(new SliderBarPoint((parentWidth/4)*3, pointY, resources.getString(R.string.self_efficacy_slider_likely)));
            addPoint(new SliderBarPoint(parentWidth - horizontalPadding, pointY, resources.getString(R.string.self_efficacy_slider_completely)));
        }
    }

    @Override
    protected void setupPaints() {
        super.setupPaints();

        Context context = getContext();

        int[] lineColours = new int[] {
                ContextCompat.getColor(context, R.color.lightBlue),
                ContextCompat.getColor(context, R.color.self_efficacy_slider_2),
                ContextCompat.getColor(context, R.color.self_efficacy_slider_3),
                ContextCompat.getColor(context, R.color.lightRed)
        };

        paints = new ArrayList<>();

        for (int i = 0; i < 4; i ++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(lineColours[i]);
            paint.setStrokeWidth(4);
            paints.add(paint);
        }

        notchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notchPaint.setColor(ContextCompat.getColor(context, R.color.barBackground));
        notchPaint.setStrokeWidth(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int lineY = getParentHeight() / 2;

        final List<SliderBarPoint> points = getPoints();

        for (int i = 0; i < points.size(); i++) {
            SliderBarPoint point = points.get(i);

            // Draw coloured line
            if (i < points.size() - 1) {
                canvas.drawLine(point.getX(), lineY, point.getX() + (getParentWidth() / 4), lineY, paints.get(i));
            }

            // Draw notch
            canvas.drawLine(point.getX(), point.getY() - 10, point.getX(), point.getY() + 10,
                    notchPaint);
        }

        // Have superclass draw circle on top
        super.onDraw(canvas);
    }
}
