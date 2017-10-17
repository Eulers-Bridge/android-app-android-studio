package com.eulersbridge.isegoria;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Anthony on 01/04/2015.
 */
public class PersonalitySliderBar extends View {
    private int parentWidth;
    private int parentHeight;

    private int x;
    private int y;

    private String answer = "Niether";
    private final ArrayList<PersonalityPoint> points = new ArrayList<>();

    private int score;

    public PersonalitySliderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        x = (parentWidth/2);
        y = (parentHeight/2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF6D65"));
        paint.setStrokeWidth(4);
        canvas.drawLine(0, (parentHeight/2), (parentWidth/3), (parentHeight/2), paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#8694A3"));
        paint.setStrokeWidth(4);
        canvas.drawLine((parentWidth/3), (parentHeight/2), (parentWidth/3)*2, (parentHeight/2), paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#60C353"));
        paint.setStrokeWidth(4);
        canvas.drawLine((parentWidth/3)*2, (parentHeight/2), (parentWidth/3)*3,
                (parentHeight/2), paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine(2, (parentHeight/2)-10, 2, (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint(2, (parentHeight/2), "Disagree Strongly"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine((parentWidth/3), (parentHeight/2)-10, (parentWidth/3), (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint((parentWidth/3), (parentHeight/2), "Disagree Moderately"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine((parentWidth/3)/2, (parentHeight/2)-10, (parentWidth/3)/2, (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint((parentWidth/3)/2, (parentHeight/2), "Disagree a Little"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine(((parentWidth/3)/2)*3, (parentHeight/2)-10, ((parentWidth/3)/2)*3, (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint(((parentWidth/3)/2)*3, (parentHeight/2), "Neither"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine(((parentWidth/3)/2)*4, (parentHeight/2)-10, ((parentWidth/3)/2)*4, (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint(((parentWidth/3)/2)*4, (parentHeight/2), "Agree a Little"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine(((parentWidth/3)/2)*5, (parentHeight/2)-10, ((parentWidth/3)/2)*5, (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint(((parentWidth/3)/2)*5, (parentHeight/2), "Agree Moderately"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#313E4D"));
        paint.setStrokeWidth(4);
        canvas.drawLine((((parentWidth/3)/2)*6)-2, (parentHeight/2)-10, (((parentWidth/3)/2)*6)-2, (parentHeight/2)+10, paint);
        points.add(new PersonalityPoint((((parentWidth/3)/2)*6)-2, (parentHeight/2), "Agree Strongly"));

        paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(4);
        paint.setAntiAlias(true);
        canvas.drawCircle(x, y, 20, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#4A90E2"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setAntiAlias(true);
        canvas.drawCircle(x, y, 20, paint);

        paint = new Paint();
        paint.setColor(Color.parseColor("#738192"));
        paint.setTextSize(16);
        paint.setAntiAlias(true);
        canvas.drawText(answer,
                (parentWidth/2)-((paint.measureText(answer))/2), (parentHeight/2)+50, paint);
    }

    private void snapToPoint(int x, int y) {
        int currentDistance = 1000;
        PersonalityPoint currentPoint = null;
        int index = -1;

        for(int i=0; i<points.size(); i++) {
            PersonalityPoint point = points.get(i);

            if(Math.abs(point.getX() - x) < currentDistance) {
                currentPoint = point;
                index = i;
                currentDistance = Math.abs(point.getX() - x);
            }
        }

        setScore(index+1);
        this.x = currentPoint.getX();
        this.answer = currentPoint.getAnswer();
        invalidate();
    }

    public int getScore() {
        return score;
    }

    private void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = (int) event.getX();
        snapToPoint((int) event.getX(), (int) event.getY());

        invalidate();
        return true;
    }
}
