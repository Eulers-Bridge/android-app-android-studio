package com.eulersbridge.isegoria.util.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import com.eulersbridge.isegoria.R
import java.util.*

open class BaseSliderBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    protected var parentWidth: Int = 0
        private set
    protected var parentHeight: Int = 0
        private set

    private var y: Int = 0

    private val circleRadius = 20
    private val circleStrokeWidth = 4
    protected val horizontalPadding = (circleRadius + 6 * circleStrokeWidth) / 2

    internal val points = ArrayList<SliderBarPoint>()

    private var dragX = -1
    private var currentPoint: SliderBarPoint? = null

    private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val circleX: Int
        get() = if (dragX >= 0) {
            dragX
        } else {
            currentPoint!!.x
        }

    var score: Int
        get() = points.indexOf(currentPoint) + 1
        set(score) {
            currentPoint = points[score - 1]
        }

    init {
        setupPaints()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = object : ViewOutlineProvider() {
                @SuppressLint("NewApi")
                override fun getOutline(view: View, outline: Outline) {
                    val x = circleX
                    outline.setOval(
                        x - circleRadius,
                        y - circleRadius,
                        x + circleRadius,
                        y + circleRadius
                    )
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = View.MeasureSpec.getSize(widthMeasureSpec) - horizontalPadding
        parentHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        this.setMeasuredDimension(parentWidth, parentHeight)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        y = parentHeight / 2
    }

    protected fun addPoint(point: SliderBarPoint) {
        points.add(point)
    }

    protected open fun setupPaints() {
        circleFillPaint.apply {
            color = ContextCompat.getColor(context, R.color.white)
            style = Paint.Style.FILL
            strokeWidth = circleStrokeWidth.toFloat()
        }

        circleStrokePaint.apply {
            color = ContextCompat.getColor(context, R.color.lightBlue)
            style = Paint.Style.STROKE
            strokeWidth = circleStrokeWidth.toFloat()
        }

        textPaint.color = ContextCompat.getColor(context, R.color.slider_text)

        // Convert text size from SP (screen density dependent) to pixels
        val pixelSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 12f,
            resources.displayMetrics
        ).toInt()

        textPaint.textSize = pixelSize.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        if (currentPoint == null)
            currentPoint = points[(points.size - 1) / 2] // Centre point

        val lineY = parentHeight / 2

        val circleX = circleX

        canvas.drawCircle(
            circleX.toFloat(),
            y.toFloat(),
            (circleRadius + 8).toFloat(),
            circleFillPaint
        )
        canvas.drawCircle(
            circleX.toFloat(),
            y.toFloat(),
            circleRadius.toFloat(),
            circleStrokePaint
        )

        val answer = currentPoint!!.answer
        canvas.drawText(
            answer, (parentWidth - textPaint.measureText(answer)) / 2, (lineY + 50).toFloat(),
            textPaint
        )
    }

    /**
     * Snap to the closest notch/point once the user has released the slider / finished dragging
     */
    private fun snapToPoint(x: Int) {
        points.minBy {
            Math.abs(it.x - x)

        }?.let {
            currentPoint = it
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        /* Calls the super implementation, which generates an AccessibilityEvent
        and calls the onClick() listener on the view, if any */
        super.performClick()

        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x.toInt()

        // Let the user drag the slider, and snap to a point once they let go
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            dragX = -1
            snapToPoint(x)

            performClick()

        } else if (x >= horizontalPadding && x <= parentWidth - horizontalPadding) {
            dragX = x
            invalidate()

            return true // Interested in any more events in this gesture (to wait for touch up/cancel)
        }

        return false // Not interested in any further events in this gesture
    }
}
