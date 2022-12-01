package com.example.detectcarplate.model;

import android.graphics.RectF;

public class CharacterDetect {
    public int x; //toa do diem x
    public int y; //toa do diem y
    public String character;
    public RectF location;
    public Float conf;
    public CharacterDetect(int x, int y, String character, RectF location, Float conf) {
        this.x = x;
        this.y = y;
        this.character = character;
        this.location = location;
        this.conf = conf;
    }
    public CharacterDetect(){};

    public CharacterDetect(CharacterDetect c) {
        this.x = c.x;
        this.y =c.y;
        this.character = c.character;
        this.location = c.location;
        this.conf = c.conf;
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
    public Float getConf() { return conf; }
    public void setX(int x) {this.x = x; };
    public void setY(int y) {this.y = y; };
    public void setCharacter(String character) {this.character = character;};
    public void setLocation(RectF location) {this.location = location;};
    public void setConf(Float conf) {this.conf = conf;};
}