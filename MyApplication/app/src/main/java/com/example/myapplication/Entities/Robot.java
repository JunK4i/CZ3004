package com.example.myapplication.Entities;

import com.example.myapplication.Directions;

/**
 * Class that stores a Robot's direction, coordinates and status. Robot exists in the ArenaGrid
 */
public class Robot{
    private Directions direction;
    private int x;
    private int y;
    private String status;

    public Robot(Directions direction, int x, int y){
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.status = "ready";
    }

    public Directions getDirection() {return direction;}
    public void setDirection(Directions direction) {this.direction = direction;}
    public int getX() {return x;}
    public void setX(int x) {this.x = x;}
    public int getY() {return y;}
    public void setY(int y) {this.y = y;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
}
