package com.eulersbridge.isegoria.vote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class Signature(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val STROKE_WIDTH_PX = 5f
        private const val HALF_STROKE_WIDTH_PX = STROKE_WIDTH_PX / 2
    }

    operator fun MotionEvent.component1() = x
    operator fun MotionEvent.component2() = y

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private var lastTouchX = 0.toFloat()
    private var lastTouchY = 0.toFloat()
    private val dirtyRect = RectF()

    /*private val mContent: LinearLayout? = null
    private var mBitmap: Bitmap? = null
    private val mypath: File? = null*/

    init {
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeWidth = STROKE_WIDTH_PX
        }
    }

    /*fun save(v: View) {
        Log.v(javaClass.simpleName, "Width: " + v.width)
        Log.v(javaClass.simpleName, "Height: " + v.height)

        if (mBitmap == null)
            mBitmap = Bitmap.createBitmap(mContent!!.width, mContent.height, Bitmap.Config.RGB_565)

        val canvas = Canvas(mBitmap!!)
        try {
            val mFileOutStream = FileOutputStream(mypath!!)

            v.draw(canvas)
            mBitmap!!.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream)
            mFileOutStream.flush()
            mFileOutStream.close()

            //String url = Images.Media.insertImage(getContentResolver(), mBitmap, "title", null);
            //Log.v(getClass().getSimpleName(),"url: " + url);

        } catch (e: Exception) {
            Log.v(javaClass.simpleName, e.toString())
        }
    }*/

    fun clear() {
        path.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val (x, y) = event

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastTouchX = x
                lastTouchY = y
                return true
            }

            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                resetDirtyRect(x, y)
                val historySize = event.historySize
                for (i in 0 until historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                }

                path.lineTo(x, y)
            }

            else -> return false
        }

        invalidate(
            (dirtyRect.left - HALF_STROKE_WIDTH_PX).toInt(),
            (dirtyRect.top - HALF_STROKE_WIDTH_PX).toInt(),
            (dirtyRect.right + HALF_STROKE_WIDTH_PX).toInt(),
            (dirtyRect.bottom + HALF_STROKE_WIDTH_PX).toInt()
        )

        lastTouchX = x
        lastTouchY = y

        return true
    }

    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX
        }

        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY
        }
    }

    private fun resetDirtyRect(eventX: Float, eventY: Float) {
        dirtyRect.apply {
            left = Math.min(lastTouchX, eventX)
            right = Math.max(lastTouchX, eventX)
            top = Math.min(lastTouchY, eventY)
            bottom = Math.max(lastTouchY, eventY)
        }
    }
}