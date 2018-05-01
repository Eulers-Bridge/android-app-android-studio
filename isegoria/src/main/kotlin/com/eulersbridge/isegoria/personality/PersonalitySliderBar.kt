package com.eulersbridge.isegoria.personality

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.ui.BaseSliderBar
import com.eulersbridge.isegoria.util.ui.SliderBarPoint

class PersonalitySliderBar constructor(context: Context, attrs: AttributeSet) : BaseSliderBar(context, attrs) {

    private val disagreePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centrePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val agreePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var notchPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        disagreePaint.color = ContextCompat.getColor(context, R.color.lightRed)
        disagreePaint.strokeWidth = 4f

        centrePaint.color = ContextCompat.getColor(context, R.color.lightGrey)
        centrePaint.strokeWidth = 4f

        agreePaint.color = ContextCompat.getColor(context, R.color.lightGreen)
        agreePaint.strokeWidth = 4f

        notchPaint.color = ContextCompat.getColor(context, R.color.barBackground)
        notchPaint.strokeWidth = 4f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        createPoints()
    }

    private fun createPoints() {
        val parentWidth = parentWidth
        val pointY = parentHeight / 2

        addPoint(
            SliderBarPoint(
                horizontalPadding,
                pointY,
                resources.getString(R.string.personality_slider_disagree_strongly)
            )
        )
        addPoint(
            SliderBarPoint(
                parentWidth / 6,
                pointY,
                resources.getString(R.string.personality_slider_disagree_moderately)
            )
        )
        addPoint(
            SliderBarPoint(
                parentWidth / 6 * 2,
                pointY,
                resources.getString(R.string.personality_slider_disagree_a_little)
            )
        )
        addPoint(
            SliderBarPoint(
                parentWidth / 6 * 3,
                pointY,
                resources.getString(R.string.personality_slider_neither)
            )
        )
        addPoint(
            SliderBarPoint(
                parentWidth / 6 * 4,
                pointY,
                resources.getString(R.string.personality_slider_agree_a_little)
            )
        )
        addPoint(
            SliderBarPoint(
                parentWidth / 6 * 5,
                pointY,
                resources.getString(R.string.personality_slider_agree_moderately)
            )
        )
        addPoint(
            SliderBarPoint(
                parentWidth - horizontalPadding,
                pointY,
                resources.getString(R.string.personality_slider_agree_strongly)
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        val lineY = parentHeight / 2

        // Disagree segment
        canvas.drawLine(
            horizontalPadding.toFloat(),
            lineY.toFloat(),
            (parentWidth / 3).toFloat(),
            lineY.toFloat(),
            disagreePaint
        )

        // Neutral/weak segment
        canvas.drawLine(
            (parentWidth / 3).toFloat(), lineY.toFloat(),
            (parentWidth / 3 * 2).toFloat(), lineY.toFloat(), centrePaint
        )

        // Agree segment
        canvas.drawLine(
            (parentWidth / 3 * 2).toFloat(), lineY.toFloat(),
            (parentWidth - horizontalPadding).toFloat(), lineY.toFloat(), agreePaint
        )

        // Notches
        for (point in points) {
            canvas.drawLine(
                point.x.toFloat(),
                (point.y - 10).toFloat(),
                point.x.toFloat(),
                (point.y + 10).toFloat(),
                notchPaint
            )
        }

        // Have superclass draw circle on top
        super.onDraw(canvas)
    }
}
