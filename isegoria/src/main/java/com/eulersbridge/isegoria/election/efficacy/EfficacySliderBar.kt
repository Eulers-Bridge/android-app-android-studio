package com.eulersbridge.isegoria.election.efficacy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.ui.BaseSliderBar
import com.eulersbridge.isegoria.util.ui.SliderBarPoint
import java.util.*

class EfficacySliderBar(context: Context, attrs: AttributeSet) : BaseSliderBar(context, attrs) {

    private val paints = ArrayList<Paint>()
    private val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val lineColours = intArrayOf(
            ContextCompat.getColor(context, R.color.lightBlue),
            ContextCompat.getColor(context, R.color.self_efficacy_slider_2),
            ContextCompat.getColor(context, R.color.self_efficacy_slider_3),
            ContextCompat.getColor(context, R.color.lightRed)
        )

        for (i in 0..lineColours.lastIndex) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = lineColours[i]
            paint.strokeWidth = 4f
            paints + paint
        }

        notchPaint.color = ContextCompat.getColor(context, R.color.barBackground)
        notchPaint.strokeWidth = 4f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        createPoints()
    }

    private fun createPoints() {
        if (points.size != 5) {
            val parentWidth = parentWidth
            val pointY = parentHeight / 2

            addPoint(
                SliderBarPoint(
                    horizontalPadding,
                    pointY,
                    resources.getString(R.string.self_efficacy_slider_not_at_all)
                )
            )
            addPoint(
                SliderBarPoint(
                    parentWidth / 4,
                    pointY,
                    resources.getString(R.string.self_efficacy_slider_unlikely)
                )
            )
            addPoint(
                SliderBarPoint(
                    parentWidth / 4 * 2,
                    pointY,
                    resources.getString(R.string.self_efficacy_slider_neutral)
                )
            )
            addPoint(
                SliderBarPoint(
                    parentWidth / 4 * 3,
                    pointY,
                    resources.getString(R.string.self_efficacy_slider_likely)
                )
            )
            addPoint(
                SliderBarPoint(
                    parentWidth - horizontalPadding,
                    pointY,
                    resources.getString(R.string.self_efficacy_slider_completely)
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        val lineY = parentHeight / 2

        points.forEachIndexed { i, point ->
            // Draw coloured line
            if (i < points.size - 1) {
                canvas.drawLine(
                    point.x.toFloat(),
                    lineY.toFloat(),
                    (point.x + parentWidth / 4).toFloat(),
                    lineY.toFloat(),
                    paints[i]
                )
            }

            // Draw notch
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
