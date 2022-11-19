package com.example.detectcarplate.model;

import android.graphics.RectF;

public class CharacterDetect {
    public int x; //toa do diem x
    public int y; //toa do diem y
    public String character;
    public RectF location;
    public CharacterDetect(int x, int y, String character, RectF location) {
        this.x = x;
        this.y = y;
        this.character = character;
        this.location = location;
    }
    public CharacterDetect(){};

    public CharacterDetect(CharacterDetect c) {
        this.x = c.x;
        this.y =c.y;
        this.character = c.character;
        this.location = c.location;
    };

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public String getCharacter() {
        return character;
    }
    public RectF getLocation() {
        return location;
    }
    public void setX(int x) {this.x = x; };
    public void setY(int y) {this.y = y; };
    public void setCharacter(String character) {this.character = character;};
    public void setLocation(RectF location) {this.location = location;};
}