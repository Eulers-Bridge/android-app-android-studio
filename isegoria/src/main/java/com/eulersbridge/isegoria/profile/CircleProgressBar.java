/*
 * 
 * Copyright 2013 Matt Joseph
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * 
 * This custom view/widget was inspired and guided by:
 * 
 * HoloCircleSeekBar - Copyright 2012 Jes�s Manzano
 * HoloColorPicker - Copyright 2012 Lars Werkman (Designed by Marie Schweiz)
 * 
 * Although I did not used the code from either project directly, they were both used as 
 * reference material, and as a result, were extremely helpful.
 */

package com.eulersbridge.isegoria.profile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.eulersbridge.isegoria.R;

public class CircleProgressBar extends View {

    /**
     * Default (fallback) base color for the drawn circle.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final @ColorInt int DEFAULT_BASE_COLOR = Color.LTGRAY;

    /**
     * Default (fallback) base color for the fill (progress) of the drawn circle.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final @ColorInt int DEFAULT_FILLED_COLOR = Color.GREEN;

    /**
     * The font size of the top text (SP).
     */
    private static final float TOP_TEXT_FONT_SIZE_SP = 21;

    /**
     * The font size of the bottom text (SP).
     */
    private static final float BOTTOM_TEXT_FONT_SIZE_SP = 11;

    /**
     * The thickness of the circle (DP).
     */
    private static final int THICKNESS_DP = 6;

    /**
     * The duration of the fill/progress animation in milliseconds.
     */
    private static final int ANIMATION_DURATION_MS = 750;

    private RectF bounds;
    private Paint circlePaint;

    private String topText;
    private Paint topTextPaint;

    private String bottomText;
    private Paint bottomTextPaint;

    /**
     * The current value represented by the progress bar.
     *
     * @see #getProgress()
     */
    private int value;

    /**
     * The maximum value of the progress bar.
     */
    private int maximumValue;

    private ValueAnimator animator;
    private float fillEndAngle = -90;
    private int animatedAlpha = 0;

    /**
     * Base color of the drawn progress circle.
     */
    private @ColorInt int baseColor;

    /**
     * Color of the fill (progress) of the drawn circle.
     */
    private @ColorInt int filledColor;

    public CircleProgressBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        init(context, attrs, defStyle);
    }

    private void init(Context context, @Nullable AttributeSet attributeSet, int defStyle) {
        if (!isInEditMode() && attributeSet != null && context != null) {
            final TypedArray attrArray = context
                    .getTheme().obtainStyledAttributes(attributeSet, R.styleable.CircleProgressBar, defStyle, 0);

            try {
                baseColor = attrArray.getColor(R.styleable.CircleProgressBar_base_color, 0);
                filledColor = attrArray.getColor(R.styleable.CircleProgressBar_progress_color, 0);

            } catch (Exception e) {
                e.printStackTrace();
            }

            attrArray.recycle();
        }

        if (baseColor == 0)
            baseColor = DEFAULT_BASE_COLOR;

        if (filledColor == 0)
            filledColor = DEFAULT_FILLED_COLOR;

        // Paint re-used for both base stroke & stroke to fill progress of circle
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(baseColor);
        circlePaint.setStrokeWidth(getThickness());
        circlePaint.setStrokeCap(Paint.Cap.ROUND);

        topTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topTextPaint.setColor(Color.BLACK);
        topTextPaint.setTextSize(getTopTextFontSize());
        topTextPaint.setFakeBoldText(true);

        bottomTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottomTextPaint.setColor(Color.GRAY);
        bottomTextPaint.setTextSize(getBottomTextFontSize());

        if (context != null && !isInEditMode()) {
            final Typeface museoTypeface = ResourcesCompat.getFont(context, R.font.museo);
            topTextPaint.setTypeface(museoTypeface);
        }

        if (isInEditMode()) {
            value = 3;
            maximumValue = 10;

            topText = "3";
            bottomText = "10";
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        circlePaint.setStrokeWidth(getThickness());
        topTextPaint.setTextSize(getTopTextFontSize());
        bottomTextPaint.setTextSize(getBottomTextFontSize());

        invalidate();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable("superState", super.onSaveInstanceState());

        bundle.putInt("value", value);
        bundle.putInt("maximumValue", maximumValue);
        bundle.putString("topText", topText);
        bundle.putString("bottomText", bottomText);
        bundle.putInt("baseColor", baseColor);
        bundle.putInt("filledColor", filledColor);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            value = bundle.getInt("value");
            maximumValue = bundle.getInt("maximumValue");
            topText = bundle.getString("topText");
            bottomText = bundle.getString("bottomText");
            baseColor = bundle.getInt("baseColor");
            filledColor = bundle.getInt("filledColor");

            state = bundle.getParcelable("superState");
        }

        super.onRestoreInstanceState(state);
    }

    private float getDensity() {
        return isInEditMode()? 1.5f : getResources().getDisplayMetrics().density;
    }

    /**
     * @return Font size of the top text (PX).
     */
    private float getTopTextFontSize() {
        return TOP_TEXT_FONT_SIZE_SP * getDensity();
    }

    /**
     * @return Font size of the bottom text (PX).
     */
    private float getBottomTextFontSize() {
        return BOTTOM_TEXT_FONT_SIZE_SP * getDensity();
    }

    /**
     * @return Thickness of the circle & progress bar (PX).
     */
    private int getThickness() {
        return Math.round(THICKNESS_DP * getDensity());
    }

    private void invalidateIfNecessary() {
        if (maximumValue != 0)
            invalidate();
    }

    @SuppressWarnings("unused")
    public void setFilledColor(@ColorInt int color) {
        this.filledColor = color;
        invalidateIfNecessary();
    }

    private int getValue() {
        return value;
    }

    /**
     * @return The sweep (end) angle of the fill path of the circle, in degrees.
     */
    private @FloatRange(from = 0, to = 360) float getSweepAngle() {
        return (getProgress()) * 360f;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setValue(int value) {
        setValue(value, false);
    }

    /**
     * Updates the current value of the progress bar, and therefore its progress, and forces
     * the progress bar to be redrawn.
     * @param value The new value of the progress bar.
     * @param animate If true, animates progress from empty (0%) to its current value.
     */
    public void setValue(int value, boolean animate) {

        float oldProgress = getProgress();
        float newProgress = getProgress((float)value, maximumValue);

        boolean shouldRedraw = oldProgress != newProgress;

        this.value = value;

        if (animate && shouldRedraw) {
            final float endSweepAngle = getSweepAngle();

            PropertyValuesHolder propertyRadius = PropertyValuesHolder.ofInt("alpha", 0, 255);
            PropertyValuesHolder propertyRotate = PropertyValuesHolder.ofFloat("angle", 0, endSweepAngle);

            animator = new ValueAnimator();
            animator.setValues(propertyRadius, propertyRotate);
            animator.setDuration(ANIMATION_DURATION_MS);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animator = null;
                }
            });
            animator.addUpdateListener(animation -> {
                animatedAlpha = (int) animation.getAnimatedValue("alpha");
                fillEndAngle = (float) animation.getAnimatedValue("angle");

                invalidateIfNecessary();
            });

            animator.start();

        } else {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }

            if (shouldRedraw) {
                fillEndAngle = getSweepAngle();
                invalidateIfNecessary();
            }
        }
    }

    /**
     * Set the text String drawn inside the circle, at the top.
     */
    public void setTopLine(String text) {
        this.topText = text;
    }

    /**
     * Set the text String drawn inside the circle, at the bottom.
     */
    public void setBottomLine(String text) {
        this.bottomText = text;
    }

    public void setMaximumValue(int maximumValue) {
        if (maximumValue < 0) {
            Log.w(getClass().getSimpleName(), "Set maximum value set to unsupported value (< 0)");
        }

        this.maximumValue = maximumValue;
        invalidateIfNecessary();
    }

    /**
     * @return A float representing the progress made, between 0 and 1 (inclusive)
     * [`value` divided by `maximumValue`].
     */
    private @FloatRange(from = 0, to = 1) float getProgress() {
        return getProgress((float)value, (float)maximumValue);
    }

    private @FloatRange(from = 0, to = 1) float getProgress(float value, float total) {
        if (value == 0 || total == 0) return 0.0f;

        float progress = value / total;

        // 0 <= progress <= 1
        return Math.min(Math.max(progress, 0.0f), 1.0f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // getLeft(), getTop(), etc. are 0 until this point.
        if (bounds == null) {
            final float halfThickness = getThickness() / 2;

            // `(THICKNESS_PX / 2)` as path stroke is centred, rather than outside or inside
            bounds = new RectF(halfThickness,
                    halfThickness,
                    getWidth() - halfThickness,
                    getHeight() - halfThickness);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        final float halfThickness = getThickness() / 2;

        // `(THICKNESS_PX / 2)` as path stroke is centred, rather than outside or inside
        bounds = new RectF(halfThickness,
                halfThickness,
                w - halfThickness,
                h - halfThickness);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Draws a text String (`text`) on a Canvas (`canvas`) using a given Paint style (`paint`),
     * centred horizontally at y co-ordinate `y`.
     * @param canvas The Canvas to draw text on.
     * @param text The String text to draw on the canvas (`canvas`)
     * @param y The y co-ordinate to draw the text String at on the canvas.
     * @param paint The Paint to use when drawing text.
     */
    private void drawText(@NonNull Canvas canvas, @NonNull String text, float viewWidth, float y, @NonNull Paint paint) {
        float textWidth = paint.measureText(text);
        float x = (viewWidth - textWidth) / 2;

        canvas.drawText(text, x, y, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the base circle
        circlePaint.setColor(baseColor);
        circlePaint.setAlpha(128); // 255 = 100% opacity, or fully opaque
        canvas.drawArc(bounds, 0, 360, false, circlePaint);

        if (getValue() > 0) {
            /* If progress has been made (value is > 0), draw an arc with the current progress
                inside the circle */
            circlePaint.setColor(filledColor);
            circlePaint.setAlpha(animatedAlpha);
            canvas.drawArc(bounds, -90, fillEndAngle, false, circlePaint);
        }

        final float thickness = getThickness();

        if (!TextUtils.isEmpty(topText)) {
            float y = thickness * 6;
            drawText(canvas, topText, bounds.right, y, topTextPaint);
        }

        if (!TextUtils.isEmpty(bottomText)) {
            float y = bounds.bottom - (thickness * 3);
            drawText(canvas, bottomText, bounds.right, y, bottomTextPaint);
        }
    }
}