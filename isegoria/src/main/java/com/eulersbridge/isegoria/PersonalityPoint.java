package com.eulersbridge.isegoria;

/**
 * Created by Anthony on 02/04/2015.
 */
class PersonalityPoint {
    private int x;
    private int y;
    private String answer;

    public PersonalityPoint(int x, int y, String answer) {
        this.x = x;
        this.y = y;
        this.answer = answer;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
