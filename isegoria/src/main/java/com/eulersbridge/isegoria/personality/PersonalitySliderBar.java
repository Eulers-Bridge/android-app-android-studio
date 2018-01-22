package com.eulersbridge.isegoria.personality;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.BaseSliderBar;
import com.eulersbridge.isegoria.util.ui.SliderBarPoint;

import java.util.List;

public class PersonalitySliderBar extends BaseSliderBar {
    private Paint disagreePaint;
    private Paint centrePaint;
    private Paint agreePaint;

    private Paint notchPaint;

    public PersonalitySliderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setupPoints();
    }

    private void setupPoints() {
        int parentWidth = getParentWidth();
        int pointY = getParentHeight() / 2;
        Resources resources = getResources();

        addPoint(new SliderBarPoint(horizontalPadding, pointY, resources.getString(R.string.personality_slider_disagree_strongly)));
        addPoint(new SliderBarPoint(parentWidth/6, pointY, resources.getString(R.string.personality_slider_disagree_moderately)));
        addPoint(new SliderBarPoint((parentWidth/6)*2, pointY, resources.getString(R.string.personality_slider_disagree_a_little)));
        addPoint(new SliderBarPoint((parentWidth/6)*3, pointY, resources.getString(R.string.personality_slider_neither)));
        addPoint(new SliderBarPoint((parentWidth/6)*4, pointY, resources.getString(R.string.personality_slider_agree_a_little)));
        addPoint(new SliderBarPoint((parentWidth/6)*5, pointY, resources.getString(R.string.personality_slider_agree_moderately)));
        addPoint(new SliderBarPoint(parentWidth - horizontalPadding, pointY, resources.getString(R.string.personality_slider_agree_strongly)));
    }

    @Override
    protected void setupPaints() {
        super.setupPaints();

        disagreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        disagreePaint.setColor(ContextCompat.getColor(getContext(), R.color.lightRed));
        disagreePaint.setStrokeWidth(4);

        centrePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centrePaint.setColor(ContextCompat.getColor(getContext(), R.color.lightGrey));
        centrePaint.setStrokeWidth(4);

        agreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        agreePaint.setColor(ContextCompat.getColor(getContext(), R.color.lightGreen));
        agreePaint.setStrokeWidth(4);

        notchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notchPaint.setColor(ContextCompat.getColor(getContext(), R.color.barBackground));
        notchPaint.setStrokeWidth(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int lineY = getParentHeight() / 2;
        int parentWidth = getParentWidth();

        // Disagree segment
        canvas.drawLine(horizontalPadding, lineY, (parentWidth/3), lineY, disagreePaint);

        // Neutral/weak segment
        canvas.drawLine((parentWidth/3), lineY,
                (parentWidth/3)*2, lineY, centrePaint);

        // Agree segment
        canvas.drawLine((parentWidth/3)*2, lineY,
                parentWidth - horizontalPadding, lineY, agreePaint);


        final List<SliderBarPoint> points = getPoints();

        // Notches
        for (SliderBarPoint point : points) {
            canvas.drawLine(point.getX(), point.getY() - 10, point.getX(), point.getY() + 10,
                    notchPaint);
        }

        // Have superclass draw circle on top
        super.onDraw(canvas);
    }
}
