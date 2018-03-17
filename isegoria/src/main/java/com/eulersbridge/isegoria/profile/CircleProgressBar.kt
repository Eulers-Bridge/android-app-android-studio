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

package com.eulersbridge.isegoria.profile

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.FloatRange
import android.support.annotation.Px
import android.support.annotation.RequiresApi
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.animation.doOnEnd
import androidx.graphics.component1
import androidx.graphics.component2
import androidx.graphics.component3
import androidx.graphics.component4
import androidx.os.bundleOf
import com.eulersbridge.isegoria.R

class CircleProgressBar : View {

    companion object {
        /**
         * The font size of the top text (SP).
         */
        private const val TOP_TEXT_FONT_SIZE_SP = 21f

        /**
         * The font size of the bottom text (SP).
         */
        private const val BOTTOM_TEXT_FONT_SIZE_SP = 11f

        /**
         * The thickness of the circle (DP).
         */
        private const val THICKNESS_DP = 6

        /**
         * The duration of the fill/progress animation in milliseconds.
         */
        private const val ANIMATION_DURATION_MS = 750

        /**
         * Default (fallback) base color for the drawn circle.
         */
        @ColorInt
        private const val DEFAULT_BASE_COLOR = Color.LTGRAY

        /**
         * Default (fallback) base color for the fill (progress) of the drawn circle.
         */
        @ColorInt
        private const val DEFAULT_FILLED_COLOR = Color.GREEN
    }

    private lateinit var bounds: RectF
    private lateinit var circlePaint: Paint

    internal var topText: String? = null
    private lateinit var topTextPaint: Paint

    internal var bottomText: String? = null
    private lateinit var bottomTextPaint: Paint

    /**
     * The current value represented by the progress bar.
     *
     * @see .getProgress
     */
    private var value: Int = 0

    /**
     * The maximum value of the progress bar.
     */
    internal var maximumValue: Int = 0
    set(value) {
        if (value < 0) {
            Log.w(javaClass.simpleName, "Set maximum value set to unsupported value (< 0)")
        }

        field = value
        invalidateIfNecessary()
    }

    private var animator: ValueAnimator? = null
    private var fillEndAngle = -90f
    private var animatedAlpha = 0

    /**
     * Base color of the drawn progress circle.
     */
    @ColorInt
    private var baseColor: Int = 0

    /**
     * Color of the fill (progress) of the drawn circle.
     */
    @ColorInt
    private var filledColor: Int = 0
        set(value) {
            field = value
            invalidateIfNecessary()
        }

    private val density: Float
        get() = if (isInEditMode) 1.5f else resources.displayMetrics.density

    /**
     * @return Font size of the top text (PX).
     */
    private val topTextFontSize: Float
        get() = TOP_TEXT_FONT_SIZE_SP * density

    /**
     * @return Font size of the bottom text (PX).
     */
    private val bottomTextFontSize: Float
        get() = BOTTOM_TEXT_FONT_SIZE_SP * density

    /**
     * @return Thickness of the circle & progress bar (PX).
     */
    @get:Px private val thickness: Int
        get() = Math.round(THICKNESS_DP * density)

    /**
     * @return The sweep (end) angle of the fill path of the circle, in degrees.
     */
    private val sweepAngle: Float
        @FloatRange(from = 0.0, to = 360.0) get() = progress * 360f

    /**
     * @return A float representing the progress made, between 0 and 1 (inclusive)
     * [`value` divided by `maximumValue`].
     */
    private val progress: Float
        @FloatRange(from = 0.0, to = 1.0) get() = getProgress(
            value.toFloat(),
            maximumValue.toFloat()
        )

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs, defStyle)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyle,
        defStyleRes
    ) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context?, attributeSet: AttributeSet?, defStyle: Int) {
        if (!isInEditMode && attributeSet != null && context != null) {
            val attrArray = context
                .theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.CircleProgressBar,
                defStyle,
                0
            )

            try {
                baseColor = attrArray.getColor(R.styleable.CircleProgressBar_base_color, 0)
                filledColor = attrArray.getColor(R.styleable.CircleProgressBar_progress_color, 0)
                topText = attrArray.getString(R.styleable.CircleProgressBar_top_line_text)
                bottomText = attrArray.getString(R.styleable.CircleProgressBar_bottom_line_text)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            attrArray.recycle()
        }

        if (baseColor == 0)
            baseColor = DEFAULT_BASE_COLOR

        if (filledColor == 0)
            filledColor = DEFAULT_FILLED_COLOR

        // Paint re-used for both base stroke & stroke to fill progress of circle
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.apply {
            style = Paint.Style.STROKE
            color = baseColor
            strokeWidth = thickness.toFloat()
            strokeCap = Paint.Cap.ROUND
        }

        topTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        topTextPaint.apply {
            color = Color.BLACK
            textSize = topTextFontSize
            isFakeBoldText = true
        }

        bottomTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bottomTextPaint.apply {
            color = Color.GRAY
            textSize = bottomTextFontSize
        }

        if (context != null && !isInEditMode)
            topTextPaint.typeface = ResourcesCompat.getFont(context, R.font.museo)

        if (isInEditMode) {
            value = 3
            maximumValue = 10

            topText = "3"
            bottomText = "10"
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        circlePaint.strokeWidth = thickness.toFloat()
        topTextPaint.textSize = topTextFontSize
        bottomTextPaint.textSize = bottomTextFontSize

        invalidate()
    }

    public override fun onSaveInstanceState() =
        bundleOf(
            "superState" to super.onSaveInstanceState(),
            "value" to value,
            "maximumValue" to maximumValue,
            "topText" to topText,
            "bottomText" to bottomText,
            "baseColor" to baseColor,
            "filledColor" to filledColor
        )

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var updatedState = state

        (state as? Bundle)?.let {
            value = it.getInt("value")
            maximumValue = it.getInt("maximumValue")
            topText = it.getString("topText")
            bottomText = it.getString("bottomText")
            baseColor = it.getInt("baseColor")
            filledColor = it.getInt("filledColor")

            updatedState = it.getParcelable("superState")
        }

        super.onRestoreInstanceState(updatedState)
    }

    private fun invalidateIfNecessary() {
        if (maximumValue != 0)
            invalidate()
    }

    fun setValue(value: Int) {
        setValue(value, false)
    }

    /**
     * Updates the current value of the progress bar, and therefore its progress, and forces
     * the progress bar to be redrawn.
     * @param value The new value of the progress bar.
     * @param animate If true, animates progress from empty (0%) to its current value.
     */
    fun setValue(value: Int, animate: Boolean) {

        val oldProgress = progress
        val newProgress = getProgress(value.toFloat(), maximumValue.toFloat())

        val shouldRedraw = oldProgress != newProgress

        this.value = value

        if (animate && shouldRedraw) {
            val propertyRadius = PropertyValuesHolder.ofInt("alpha", 0, 255)
            val propertyRotate = PropertyValuesHolder.ofFloat("angle", 0.0f, sweepAngle)

            animator = ValueAnimator()
            animator?.apply {
                setValues(propertyRadius, propertyRotate)
                duration = ANIMATION_DURATION_MS.toLong()
                interpolator = DecelerateInterpolator()

                doOnEnd { animator = null }

                addUpdateListener {
                    animatedAlpha = it.getAnimatedValue("alpha") as Int
                    fillEndAngle = it.getAnimatedValue("angle") as Float

                    invalidateIfNecessary()
                }

                start()
            }

        } else {
            animator?.cancel()
            animator = null

            if (shouldRedraw) {
                fillEndAngle = sweepAngle
                invalidateIfNecessary()
            }
        }
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getProgress(value: Float, total: Float): Float {
        if (value == 0f || total == 0f) return 0.0f

        val progress = value / total

        // 0 <= progress <= 1
        return Math.min(Math.max(progress, 0.0f), 1.0f)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // getLeft(), getTop(), etc. are 0 until this point.
        if (!this::bounds.isInitialized) {
            val halfThickness = (thickness / 2).toFloat()

            // `(THICKNESS_PX / 2)` as path stroke is centred, rather than outside or inside
            bounds = RectF(
                halfThickness,
                halfThickness,
                width - halfThickness,
                height - halfThickness
            )
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        val halfThickness = (thickness / 2).toFloat()

        // `(THICKNESS_PX / 2)` as path stroke is centred, rather than outside or inside
        bounds = RectF(
            halfThickness,
            halfThickness,
            w - halfThickness,
            h - halfThickness
        )

        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**
     * Draws a text String (`text`) on a Canvas (`canvas`) using a given Paint style (`paint`),
     * centred horizontally at y co-ordinate `y`.
     * @param canvas The Canvas to draw text on.
     * @param text The String text to draw on the canvas (`canvas`)
     * @param y The y co-ordinate to draw the text String at on the canvas.
     * @param paint The Paint to use when drawing text.
     */
    private fun drawText(canvas: Canvas, text: String, viewWidth: Float, y: Float, paint: Paint) {
        val textWidth = paint.measureText(text)
        val x = (viewWidth - textWidth) / 2

        canvas.drawText(text, x, y, paint)
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the base circle
        circlePaint.color = baseColor
        circlePaint.alpha = 128 // 255 = 100% opacity, or fully opaque
        canvas.drawArc(bounds, 0f, 360f, false, circlePaint)

        val (_,_,right,bottom) = bounds

        if (value > 0) {
            /* If progress has been made (value is > 0), draw an arc with the current progress
                inside the circle */
            circlePaint.color = filledColor
            circlePaint.alpha = animatedAlpha
            canvas.drawArc(bounds, -90f, fillEndAngle, false, circlePaint)
        }

        val thickness = thickness.toFloat()

        topText?.takeUnless { it.isBlank() }?.let {
            val y = thickness * 6
            drawText(canvas, it, right, y, topTextPaint)
        }

        bottomText?.takeUnless { it.isBlank() }?.let {
            val y = bottom - thickness * 3
            drawText(canvas, it, right, y, bottomTextPaint)
        }
    }
}