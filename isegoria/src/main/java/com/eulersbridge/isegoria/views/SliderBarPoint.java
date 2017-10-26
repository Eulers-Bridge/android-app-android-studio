package com.eulersbridge.isegoria.views;

/**
 * Created by Anthony on 02/04/2015.
 */
public class SliderBarPoint {
    private final int x;
    private final int y;
    private final String answer;

    public SliderBarPoint(int x, int y, String answer) {
        this.x = x;
        this.y = y;
        this.answer = answer;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    String getAnswer() {
        return answer;
    }
}
