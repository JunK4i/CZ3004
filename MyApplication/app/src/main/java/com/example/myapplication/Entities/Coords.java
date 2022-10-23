package com.example.myapplication.Entities;

import com.example.myapplication.Directions;
import com.example.myapplication.Entities.Cell;

/**
 * Data Class used to hold coordinate and obstacleId information. Used for obstacles
 */
public class Coords {
    private int x;
    private int y;
    private int obstacleId;
    public Coords(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {return x;}
    public void setX(int x) {this.x = x;}

    public int getY() {return y;}
    public void setY(int y) {this.y = y;}

}
