package com.example.myapplication.Entities;

import android.util.Log;

import com.example.myapplication.Directions;

import java.lang.reflect.Type;

/**
 * Cell contains information of each cell. Each cell is binded to a view in the GridRecyclerAdapter.
 * Changing the state of the cell will allow you to alter the display in the grid
 *
 * A cell can be empty, obstacle, or robot and will have its corresponding attributes altered when changing from one type to another
 * The image id of the cell corresponds to a resource in drawable. This is mapped using the helper Class ImageMapper. Please refer and update accordingly
 */
public class Cell {
    public enum Types{
        EMPTY,
        OBSTACLE,
        R_TOP_LEFT,
        R_TOP_CENTER,
        R_TOP_RIGHT,
        R_MID_LEFT,
        R_MID_CENTER,
        R_MID_RIGHT,
        R_BTM_LEFT,
        R_BTM_CENTER,
        R_BTM_RIGHT
    }
    private Types type;
    private boolean isRevealed;
    private int x;
    private int y;
    private int obstacleId; // obstacle number, in order of obstacle placement
    private int imageId; // corresponding image tagged to, GridRecyclerAdapter calls ImageMapper with this value to get the correct image
    private Directions direction;

    // Create empty cell
    public Cell(int x, int y){
        this.type = Types.EMPTY;
        this.isRevealed = false;
        this.x = x;
        this.y = y;
        this.imageId = 0;
        this.direction = Directions.NORTH;
    }

    // Get list index of cell
    public int getPos(int x, int y){
        int floor = -y+19;
        return floor*20+x;
    }

    public void setEmpty(){
        this.type = Types.EMPTY;
        this.obstacleId = 0;
        this.imageId = 0;
        this.direction = Directions.NORTH;
    }

    // Create obstacle cell from empty cell
    public void setObstacleHidden(int obstacleId, Directions direction){
        this.type = Types.OBSTACLE;
        this.obstacleId = obstacleId;
        this.direction = direction;
        this.imageId = 10;
    }

    public void revealObstacle(int imageId){
        isRevealed = true;
        this.imageId = imageId;
    }

    public void setTraversed(){
        this.isRevealed = true;
        this.type = Types.EMPTY;
        this.imageId = 42;
    }

    public void setRobot(Types type, Directions direction){
        if(type == Types.EMPTY || type == Types.OBSTACLE){
            return;
        }
        this.type = type;
        this.isRevealed = true;
        this.direction = direction;
        switch(type){
            case R_TOP_LEFT: imageId = 1; break;
            case R_TOP_CENTER: imageId = 2; break;
            case R_TOP_RIGHT: imageId = 3; break;
            case R_MID_LEFT: imageId = 4; break;
            case R_MID_CENTER: imageId = 5; break;
            case R_MID_RIGHT: imageId = 6; break;
            case R_BTM_LEFT: imageId = 7; break;
            case R_BTM_CENTER: imageId = 8; break;
            case R_BTM_RIGHT: imageId = 9; break;
            default:return;
        }
    }

    public void rotateDirection(){ // Clockwise
        if (direction.equals(Directions.NORTH)) {
            direction = Directions.EAST;
        } else if(direction.equals(Directions.EAST)){
            direction = Directions.SOUTH;
        }else if(direction.equals(Directions.SOUTH)){
            direction = Directions.WEST;
        }else if(direction.equals(Directions.WEST)){
            direction = Directions.NORTH;
        }
    }

    // Getters and Setters
    public int getX() {return x;}
    public int getY() {return y;}
    public int getImageId(){return imageId;}
    public boolean isRevealed() {return isRevealed;}
    public void setIsRevealed(boolean revealed) {isRevealed = revealed;}
    public Types getType() {return type;}
    public Directions getDirection(){return direction;}
    public void setDirection(Directions direction){this.direction = direction;}
    public int getObstacleId(){return obstacleId;}
    public boolean isObstacle(){
        if(type.equals(Types.OBSTACLE)){return true;}
        return false;
    }
    public boolean isRobot(){
        if(type.equals(Types.R_BTM_CENTER)||type.equals(Types.R_BTM_LEFT)||type.equals(Types.R_BTM_RIGHT)||type.equals(Types.R_MID_CENTER)||type.equals(Types.R_MID_RIGHT)||
                type.equals(Types.R_MID_LEFT)||type.equals(Types.R_TOP_CENTER)||type.equals(Types.R_TOP_LEFT)||type.equals(Types.R_TOP_RIGHT)){
            return true;
        }
        return false;
    }
    public boolean isEmpty(){
        if(type.equals(Types.EMPTY)){return true;}
        return false;
    }
}
