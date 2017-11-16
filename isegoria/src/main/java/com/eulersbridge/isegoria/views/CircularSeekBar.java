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

package com.eulersbridge.isegoria.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.eulersbridge.isegoria.R;

public class CircularSeekBar extends View implements Runnable {

	/**
	 * Used to scale the dp units to pixels
	 */
	private final float DP_TO_PX_SCALE = getResources().getDisplayMetrics().density;

	// Default values
	private static final float DEFAULT_CIRCLE_X_RADIUS = 30f;
	private static final float DEFAULT_CIRCLE_Y_RADIUS = 30f;
	private static final float DEFAULT_POINTER_RADIUS = 7f;
	private static final float DEFAULT_POINTER_HALO_BORDER_WIDTH = 2f;
	private static final float DEFAULT_CIRCLE_STROKE_WIDTH = 5f;
	private static final float DEFAULT_START_ANGLE = 270f; // Geometric (clockwise, relative to 3 o'clock)
	private static final float DEFAULT_END_ANGLE = 270f; // Geometric (clockwise, relative to 3 o'clock)
	private static final int DEFAULT_MAX = 100;
	private static final int DEFAULT_PROGRESS = 0;
	private static final int DEFAULT_CIRCLE_COLOR = Color.parseColor("#64d3d3d3");
	private static final int DEFAULT_CIRCLE_PROGRESS_COLOR = Color.argb(235, 74, 138, 255);
	private static final int DEFAULT_POINTER_COLOR = Color.argb(235, 74, 138, 255);
	private static final int DEFAULT_POINTER_HALO_COLOR = Color.argb(135, 74, 138, 255);
	private static final int DEFAULT_POINTER_HALO_COLOR_ON_TOUCH = Color.argb(135, 74, 138, 255);
	private static final int DEFAULT_CIRCLE_FILL_COLOR = Color.TRANSPARENT;
	private static final int DEFAULT_POINTER_ALPHA = 135;
	private static final int DEFAULT_POINTER_ALPHA_ON_TOUCH = 100;
	private static final boolean DEFAULT_USE_CUSTOM_RADII = false;
	private static final boolean DEFAULT_MAINTAIN_EQUAL_CIRCLE = true;
    private static final boolean DEFAULT_LOCK_ENABLED = true;

	/**
	 * {@code Paint} instance used to draw the inactive circle.
	 */
	private Paint mCirclePaint;

	/**
	 * {@code Paint} instance used to draw the circle fill.
	 */
	private Paint mCircleFillPaint;

	/**
	 * {@code Paint} instance used to draw the active circle (represents progress).
	 */
	private Paint mCircleProgressPaint;

	/**
	 * {@code Paint} instance used to draw the glow from the active circle.
	 */
	private Paint mCircleProgressGlowPaint;

	/**
	 * {@code Paint} instance used to draw the top line of text.
	 */
	private Paint mTopTextPaint;

	/**
	 * {@code Paint} instance used to draw the bottom line of text.
	 */
	private Paint mBottomTextPaint;

	/**
	 * The width of the circle (in pixels).
	 */
	private float mCircleStrokeWidth;

	/**
	 * The X radius of the circle (in pixels).
	 */
	private float mCircleXRadius;

	/**
	 * The Y radius of the circle (in pixels).
	 */
	private float mCircleYRadius;

	/**
	 * The radius of the pointer (in pixels).
	 */
	private float mPointerRadius;

	/**
	 * The width of the pointer halo border (in pixels).
	 */
	private float mPointerHaloBorderWidth;

	/**
	 * Start angle of the CircularSeekBar.
	 * Note: If mStartAngle and mEndAngle are set to the same angle, 0.1 is subtracted
	 * from the mEndAngle to make the circle function properly.
	 */
	private float mStartAngle;

	/**
	 * End angle of the CircularSeekBar.
	 * Note: If mStartAngle and mEndAngle are set to the same angle, 0.1 is subtracted
	 * from the mEndAngle to make the circle function properly.
	 */
	private float mEndAngle;

	/**
	 * {@code RectF} that represents the circle (or ellipse) of the seekbar.
	 */
	private final RectF mCircleRectF = new RectF();

	/**
	 * Holds the color value for {@code mPointerPaint} before the {@code Paint} instance is created.
	 */
	private int mPointerColor = DEFAULT_POINTER_COLOR;

	/**
	 * Holds the color value for {@code mPointerHaloPaint} before the {@code Paint} instance is created.
	 */
	private int mPointerHaloColor = DEFAULT_POINTER_HALO_COLOR;

	/**
	 * Holds the color value for {@code mPointerHaloPaint} before the {@code Paint} instance is created.
	 */
	private int mPointerHaloColorOnTouch = DEFAULT_POINTER_HALO_COLOR_ON_TOUCH;

	/**
	 * Holds the color value for {@code mCirclePaint} before the {@code Paint} instance is created.
	 */
	private int mCircleColor = DEFAULT_CIRCLE_COLOR;

	/**
	 * Holds the color value for {@code mCircleFillPaint} before the {@code Paint} instance is created.
	 */
	private int mCircleFillColor = DEFAULT_CIRCLE_FILL_COLOR;

	/**
	 * Holds the color value for {@code mCircleProgressPaint} before the {@code Paint} instance is created.
	 */
	private int mCircleProgressColor = DEFAULT_CIRCLE_PROGRESS_COLOR;

	/**
	 * Holds the alpha value for {@code mPointerHaloPaint}.
	 */
	private int mPointerAlpha = DEFAULT_POINTER_ALPHA;

	/**
	 * Holds the OnTouch alpha value for {@code mPointerHaloPaint}.
	 */
	private int mPointerAlphaOnTouch = DEFAULT_POINTER_ALPHA_ON_TOUCH;

	/**
	 * Distance (in degrees) that the the circle/semi-circle makes up.
	 * This amount represents the max of the circle in degrees.
	 */
	private float mTotalCircleDegrees;

	/**
	 * Distance (in degrees) that the current progress makes up in the circle.
	 */
	private float mProgressDegrees;

	/**
	 * {@code Path} used to draw the circle/semi-circle.
	 */
	private Path mCirclePath;

	/**
	 * {@code Path} used to draw the progress on the circle.
	 */
	private Path mCircleProgressPath;

	/**
	 * Max value that this CircularSeekBar is representing.
	 */
	private int mMax;

	/**
	 * Progress value that this CircularSeekBar is representing.
	 */
	private int mProgress;
    private int finalMProgress;

	/**
	 * If true, then the user can specify the X and Y radii.
	 * If false, then the View itself determines the size of the CircularSeekBar.
	 */
	private boolean mCustomRadii;

	/**
	 * Maintain a perfect circle (equal x and y radius), regardless of view or custom attributes.
	 * The smaller of the two radii will always be used in this case.
	 * The default is to be a circle and not an ellipse, due to the behavior of the ellipse.
	 */
	private boolean mMaintainEqualCircle;

    /**
     * Used for enabling/disabling the lock option for easier hitting of the 0 progress mark.
     * */
    private boolean lockEnabled = true;

	/**
	 * Represents the counter-clockwise distance from {@code mEndAngle} to the touch angle.
	 * Used when touching the CircularSeekBar.
	 * Currently unused, but kept just in case.
	 */
	@SuppressWarnings("unused")
	private float ccwDistanceFromEnd;

	/**
	 * The width of the circle used in the {@code RectF} that is used to draw it.
	 * Based on either the View width or the custom X radius.
	 */
	private float mCircleWidth;

	/**
	 * The height of the circle used in the {@code RectF} that is used to draw it.
	 * Based on either the View width or the custom Y radius.
	 */
	private float mCircleHeight;

	/**
	 * Represents the progress mark on the circle, in geometric degrees.
	 * This is not provided by the user; it is calculated;
	 */
	private float mPointerPosition;

    private String topLine = "";
    private String bottomLine = "";

	/**
	 * Initialize the CircularSeekBar with the attributes from the XML style.
	 * Uses the defaults defined at the top of this file when an attribute is not specified by the user.
	 * @param attrArray TypedArray containing the attributes.
	 */
	private void initAttributes(TypedArray attrArray) {
		mCircleXRadius = attrArray.getFloat(R.styleable.CircularSeekBar_circle_x_radius, DEFAULT_CIRCLE_X_RADIUS) * DP_TO_PX_SCALE;
		mCircleYRadius = attrArray.getFloat(R.styleable.CircularSeekBar_circle_y_radius, DEFAULT_CIRCLE_Y_RADIUS) * DP_TO_PX_SCALE;
		mPointerRadius = attrArray.getFloat(R.styleable.CircularSeekBar_pointer_radius, DEFAULT_POINTER_RADIUS) * DP_TO_PX_SCALE;
		mPointerHaloBorderWidth = attrArray.getFloat(R.styleable.CircularSeekBar_pointer_halo_border_width, DEFAULT_POINTER_HALO_BORDER_WIDTH) * DP_TO_PX_SCALE;
		mCircleStrokeWidth = attrArray.getFloat(R.styleable.CircularSeekBar_circle_stroke_width, DEFAULT_CIRCLE_STROKE_WIDTH) * DP_TO_PX_SCALE;

		String tempColor = attrArray.getString(R.styleable.CircularSeekBar_pointer_color);
		if (tempColor != null) {
			try {
				mPointerColor = Color.parseColor(tempColor);
			} catch (IllegalArgumentException e) {
				mPointerColor = DEFAULT_POINTER_COLOR;
			}
		}

		tempColor = attrArray.getString(R.styleable.CircularSeekBar_pointer_halo_color);
		if (tempColor != null) {
			try {
				mPointerHaloColor = Color.parseColor(tempColor);
			} catch (IllegalArgumentException e) {
				mPointerHaloColor = DEFAULT_POINTER_HALO_COLOR;
			}
		}

		tempColor = attrArray.getString(R.styleable.CircularSeekBar_pointer_halo_color_on_touch);
		if (tempColor != null) {
			try {
				mPointerHaloColorOnTouch = Color.parseColor(tempColor);
			} catch (IllegalArgumentException e) {
				mPointerHaloColorOnTouch = DEFAULT_POINTER_HALO_COLOR_ON_TOUCH;
			}
		}

		tempColor = attrArray.getString(R.styleable.CircularSeekBar_circle_color);
		if (tempColor != null) {
			try {
				mCircleColor = Color.parseColor(tempColor);
			} catch (IllegalArgumentException e) {
				mCircleColor = DEFAULT_CIRCLE_COLOR;
			}
		}

		tempColor = attrArray.getString(R.styleable.CircularSeekBar_circle_progress_color);
		if (tempColor != null) {
			try {
				mCircleProgressColor = Color.parseColor(tempColor);
			} catch (IllegalArgumentException e) {
				mCircleProgressColor = DEFAULT_CIRCLE_PROGRESS_COLOR;
			}
		}

		tempColor = attrArray.getString(R.styleable.CircularSeekBar_circle_fill);
		if (tempColor != null) {
			try {
				mCircleFillColor = Color.parseColor(tempColor);
			} catch (IllegalArgumentException e) {
				mCircleFillColor = DEFAULT_CIRCLE_FILL_COLOR;
			}
		}

		mPointerAlpha = Color.alpha(mPointerHaloColor);

		mPointerAlphaOnTouch = attrArray.getInt(R.styleable.CircularSeekBar_pointer_alpha_on_touch, DEFAULT_POINTER_ALPHA_ON_TOUCH);
		if (mPointerAlphaOnTouch > 255 || mPointerAlphaOnTouch < 0) {
			mPointerAlphaOnTouch = DEFAULT_POINTER_ALPHA_ON_TOUCH;
		}

		mMax = attrArray.getInt(R.styleable.CircularSeekBar_max, DEFAULT_MAX);
		mProgress = attrArray.getInt(R.styleable.CircularSeekBar_progress, DEFAULT_PROGRESS);
		mCustomRadii = attrArray.getBoolean(R.styleable.CircularSeekBar_use_custom_radii, DEFAULT_USE_CUSTOM_RADII);
		mMaintainEqualCircle = attrArray.getBoolean(R.styleable.CircularSeekBar_maintain_equal_circle, DEFAULT_MAINTAIN_EQUAL_CIRCLE);
        lockEnabled = attrArray.getBoolean(R.styleable.CircularSeekBar_lock_enabled, DEFAULT_LOCK_ENABLED);

		// Modulo 360 right now to avoid constant conversion
		mStartAngle = ((360f + (attrArray.getFloat((R.styleable.CircularSeekBar_start_angle), DEFAULT_START_ANGLE) % 360f)) % 360f);
		mEndAngle = ((360f + (attrArray.getFloat((R.styleable.CircularSeekBar_end_angle), DEFAULT_END_ANGLE) % 360f)) % 360f);

		if (mStartAngle == mEndAngle) {
			//mStartAngle = mStartAngle + 1f;
			mEndAngle = mEndAngle - .1f;
		}


	}

	/**
	 * Initializes the {@code Paint} objects with the appropriate styles.
	 */
	private void initPaints() {
		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setDither(true);
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
		mCirclePaint.setStrokeCap(Paint.Cap.ROUND);

		mCircleFillPaint = new Paint();
		mCircleFillPaint.setAntiAlias(true);
		mCircleFillPaint.setDither(true);
		mCircleFillPaint.setColor(mCircleFillColor);
		mCircleFillPaint.setStyle(Paint.Style.FILL);

		mCircleProgressPaint = new Paint();
		mCircleProgressPaint.setAntiAlias(true);
		mCircleProgressPaint.setDither(true);
		mCircleProgressPaint.setColor(mCircleProgressColor);
		mCircleProgressPaint.setStrokeWidth(mCircleStrokeWidth);
		mCircleProgressPaint.setStyle(Paint.Style.STROKE);
		mCircleProgressPaint.setStrokeJoin(Paint.Join.ROUND);
		mCircleProgressPaint.setStrokeCap(Paint.Cap.ROUND);

		mCircleProgressGlowPaint = new Paint();
		mCircleProgressGlowPaint.set(mCircleProgressPaint);
		mCircleProgressGlowPaint.setMaskFilter(new BlurMaskFilter((5f * DP_TO_PX_SCALE), BlurMaskFilter.Blur.NORMAL));

		int topTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 22.66666667, getResources().getDisplayMetrics());

		mTopTextPaint = new Paint();
		mTopTextPaint.setColor(Color.BLACK);
		mTopTextPaint.setTextSize(topTextSize);
		mTopTextPaint.setFakeBoldText(true);
		mTopTextPaint.setAntiAlias(true);
		mTopTextPaint.setDither(true);

		int bottomTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 7.333333333, getResources().getDisplayMetrics());

		mBottomTextPaint = new Paint();
		mBottomTextPaint.setColor(Color.parseColor("#8A898A"));
		mBottomTextPaint.setTextSize(bottomTextSize);
		mBottomTextPaint.setFakeBoldText(true);
		mBottomTextPaint.setAntiAlias(true);
		mBottomTextPaint.setDither(true);
	}

	/**
	 * Calculates the total degrees between mStartAngle and mEndAngle, and sets mTotalCircleDegrees
	 * to this value.
	 */
	private void calculateTotalDegrees() {
		mTotalCircleDegrees = (360f - (mStartAngle - mEndAngle)) % 360f; // Length of the entire circle/arc
		if (mTotalCircleDegrees <= 0f) {
			mTotalCircleDegrees = 360f;
		}
	}

	/**
	 * Calculate the degrees that the progress represents. Also called the sweep angle.
	 * Sets mProgressDegrees to that value.
	 */
	private void calculateProgressDegrees() {
		mProgressDegrees = mPointerPosition - mStartAngle; // Verified
		mProgressDegrees = (mProgressDegrees < 0 ? 360f + mProgressDegrees : mProgressDegrees); // Verified
	}

	/**
	 * Calculate the pointer position (and the end of the progress arc) in degrees.
	 * Sets mPointerPosition to that value.
	 */
	private void calculatePointerAngle() {
		float progressPercent = ((float)mProgress / (float)mMax);
		mPointerPosition = (progressPercent * mTotalCircleDegrees) + mStartAngle;
		mPointerPosition = mPointerPosition % 360f;
	}

	/**
	 * Initialize the {@code Path} objects with the appropriate values.
	 */
	private void initPaths() {
		mCirclePath = new Path();
		mCirclePath.addArc(mCircleRectF, mStartAngle, mTotalCircleDegrees);

		mCircleProgressPath = new Path();
		mCircleProgressPath.addArc(mCircleRectF, mStartAngle, mProgressDegrees);
	}

	/**
	 * Initialize the {@code RectF} objects with the appropriate values.
	 */
	private void initRects() {
		mCircleRectF.set(-mCircleWidth, -mCircleHeight, mCircleWidth, mCircleHeight);
	}

    public void run() {
        while(mProgress < finalMProgress) {
            mProgress = mProgress + 1;
            recalculateAll();
            try {
                 mainActivity.runOnUiThread(this::invalidate);
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(this.getWidth() / 2, this.getHeight() / 2);

		canvas.drawPath(mCirclePath, mCirclePaint);
		canvas.drawPath(mCircleProgressPath, mCircleProgressGlowPaint);
		canvas.drawPath(mCircleProgressPath, mCircleProgressPaint);

		canvas.drawPath(mCirclePath, mCircleFillPaint);

        int zeroPoint = -(getWidth()/2);

        int topLineWidth = ((int) mTopTextPaint.measureText(topLine));
        int x = zeroPoint + (getWidth()/2) - (topLineWidth/2);
        int y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  2.666666667, getResources().getDisplayMetrics());
        canvas.drawText(topLine, x, y, mTopTextPaint);

        y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)   16.500000000, getResources().getDisplayMetrics());

        int bottomLineWidth = ((int) mBottomTextPaint.measureText(bottomLine));
        x = zeroPoint + (getWidth()/2) - (bottomLineWidth/2);
        canvas.drawText(bottomLine, x, y, mBottomTextPaint);
	}

	/**
	 * Get the progress of the CircularSeekBar.
	 * @return The progress of the CircularSeekBar.
	 */
	public int getProgress() {
		return Math.round((float)mMax * mProgressDegrees / mTotalCircleDegrees);
	}

	/**
	 * Set the progress of the CircularSeekBar.
	 * If the progress is the same, then any listener will not receive a onProgressChanged event.
	 * @param progress The progress to set the CircularSeekBar to.
	 */
	public void setProgress(int progress) {
        finalMProgress = progress;
        mProgress = 0;

      /*  if (mProgress != progress) {
            mProgress = progress;
            if (mOnCircularSeekBarChangeListener != null) {
                mOnCircularSeekBarChangeListener.onProgressChanged(this, progress, false);
            }*/

            recalculateAll();
            invalidate();
        //}
    }

    public void animateProgress(final int progress) {
        /*final Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < progress; i++) {
                    setProgress(i);
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread t = new Thread(r);
        t.start();*/
    }

	private void setProgressBasedOnAngle(float angle) {
		mPointerPosition = angle;
		calculateProgressDegrees();
		mProgress = Math.round((float)mMax * mProgressDegrees / mTotalCircleDegrees);
	}

	private void recalculateAll() {
		calculateTotalDegrees();
		calculatePointerAngle();
		calculateProgressDegrees();

		initRects();

		initPaths();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		if (mMaintainEqualCircle) {
			int min = Math.min(width, height);
			setMeasuredDimension(min, min);
		} else {
			setMeasuredDimension(width, height);
		}

		// Set the circle width and height based on the view for the moment
		mCircleHeight = (float)height / 2f - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);
		mCircleWidth = (float)width / 2f - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);

		// If it is not set to use custom
		if (mCustomRadii) {
			// Check to make sure the custom radii are not out of the view. If they are, just use the view values
			if ((mCircleYRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth) < mCircleHeight) {
				mCircleHeight = mCircleYRadius - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);
			}

			if ((mCircleXRadius - mCircleStrokeWidth - mPointerRadius - mPointerHaloBorderWidth) < mCircleWidth) {
				mCircleWidth = mCircleXRadius - mCircleStrokeWidth - mPointerRadius - (mPointerHaloBorderWidth * 1.5f);
			}
		}

		if (mMaintainEqualCircle) { // Applies regardless of how the values were determined
			float min = Math.min(mCircleHeight, mCircleWidth);
			mCircleHeight = min;
			mCircleWidth = min;
		}

		recalculateAll();
	}

    public void setTopLine(String topLine) {
        this.topLine = topLine;
        invalidate();
    }

    public void setBottomLine(String bottomLine) {
        this.bottomLine = bottomLine;
        invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {
		final TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircularSeekBar, defStyle, 0);
		initAttributes(attrArray);
		attrArray.recycle();
		initPaints();
	}

    private final Activity mainActivity;

	public CircularSeekBar(Context context) {
		super(context);

        mainActivity = (Activity) context;

		init(null, 0);
	}

	public CircularSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
        mainActivity = (Activity) context;
		init(attrs, 0);
	}

	public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        mainActivity = (Activity) context;
		init(attrs, defStyle);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable("PARENT", superState);
		state.putInt("MAX", mMax);
		state.putInt("PROGRESS", mProgress);
		state.putInt("mCircleColor", mCircleColor);
		state.putInt("mCircleProgressColor", mCircleProgressColor);
		state.putInt("mPointerColor", mPointerColor);
		state.putInt("mPointerHaloColor", mPointerHaloColor);
		state.putInt("mPointerHaloColorOnTouch", mPointerHaloColorOnTouch);
		state.putInt("mPointerAlpha", mPointerAlpha);
		state.putInt("mPointerAlphaOnTouch", mPointerAlphaOnTouch);
        state.putBoolean("lockEnabled", lockEnabled);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable("PARENT");
		super.onRestoreInstanceState(superState);

		mMax = savedState.getInt("MAX");
		mProgress = savedState.getInt("PROGRESS");
		mCircleColor = savedState.getInt("mCircleColor");
		mCircleProgressColor = savedState.getInt("mCircleProgressColor");
		mPointerColor = savedState.getInt("mPointerColor");
		mPointerHaloColor = savedState.getInt("mPointerHaloColor");
		mPointerHaloColorOnTouch = savedState.getInt("mPointerHaloColorOnTouch");
		mPointerAlpha = savedState.getInt("mPointerAlpha");
		mPointerAlphaOnTouch = savedState.getInt("mPointerAlphaOnTouch");
        lockEnabled = savedState.getBoolean("lockEnabled");

		initPaints();

		recalculateAll();
	}

	/**
	 * Sets the circle progress color.
	 * @param color the color of the circle progress
	 */
	public void setCircleProgressColor(int color) {
		mCircleProgressColor = color;
		mCircleProgressPaint.setColor(mCircleProgressColor);
		invalidate();
	}

	/**
	 * Set the max of the CircularSeekBar.
	 * If the new max is less than the current progress, then the progress will be set to zero.
	 * If the progress is changed as a result, then any listener will receive a onProgressChanged event.
	 * @param max The new max for the CircularSeekBar.
	 */
	public void setMax(int max) {
		if (!(max <= 0)) { // Check to make sure it's greater than zero
			if (max <= mProgress) {
				mProgress = 0; // If the new max is less than current progress, set progress to zero
			}
			mMax = max;

			recalculateAll();
			invalidate();
		}
	}

    /**
	 * Get the current max of the CircularSeekBar.
	 * @return Synchronized integer value of the max.
	 */
	public synchronized int getMax() {
		return mMax;
	}


}
