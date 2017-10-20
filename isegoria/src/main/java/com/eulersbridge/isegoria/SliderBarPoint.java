package com.eulersbridge.isegoria;

/**
 * Created by Anthony on 02/04/2015.
 */
class SliderBarPoint {
    private final int x;
    private final int y;
    private final String answer;

    SliderBarPoint(int x, int y, String answer) {
        this.x = x;
        this.y = y;
        this.answer = answer;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    String getAnswer() {
        return answer;
    }
}
