package com.example.myapplication.Entities;

import android.util.Log;

import com.example.myapplication.Directions;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * ArenaGrid contains data and functions of items that interact within the grid such as
 * obstacles, cells, robot
 */
public class ArenaGrid {
    private List<Cell> cells; // List of Cell objects which will be binded to views in the GridRecyclerAdapter
    private int size;
    private Robot robot; // store cells which make up the robot
    private HashMap<Integer, Coords> obstacles; // store where the obstacles are located
    private HashMap<Integer, Coords> discoveredObstacles; // store obstacles that are discovered
    private List<Integer> obstacleList = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8)); // list of unsassigned obstacles

    public ArenaGrid(int size){
        this.size = size;
        cells = new ArrayList<>();
        obstacles = new HashMap<>();
        discoveredObstacles = new HashMap<>();
        int y_max = size-1;
        for (int i = 0; i < size * size; i++){
            int x = i % size;
            int y = (Math.floorDiv(i,size)-y_max)*(-1);
            cells.add(new Cell(x, y));
        }
        spawnRobot(1,1);
    }

    /***********************************CELL FUNCTIONS********************************************/

    public List<Cell> getCells() {
        return cells;
    }
    public Cell cellAt(int x, int y){
        int floor = -y+19;
        int pos =  floor*20+x;
        return cells.get(pos);
    }
    public boolean inGrid(int x, int y){
        if(x>=0 && y>=0 && x<size && y<size){return true;}
        return false;
    }

    /***********************************OBSTACLE FUNCTIONS********************************************/

    public void spawnObstacle(int x, int y){
        if(!obstacleList.isEmpty()){
            int obstacleId = getNextObstacleId();
            obstacleList.remove(0);
            Coords obstacle = new Coords(x, y);
            cellAt(x,y).setObstacleHidden(obstacleId, Directions.NORTH);
            obstacles.put(obstacleId,obstacle);
        }
    }
    public void setObstacle(int x, int y, Directions dir){
        if(!obstacleList.isEmpty()){
            int obstacleId = getNextObstacleId();
            obstacleList.remove(0);
            Coords obstacle = new Coords(x, y);
            cellAt(x,y).setObstacleHidden(obstacleId, dir);
            obstacles.put(obstacleId,obstacle);
            Log.d("set",""+x+y+dir);
        }
    }
    public void moveObstacle(int xFrom, int yFrom, int xTo, int yTo){
        Cell cellFrom = cellAt(xFrom,yFrom); // get source cell
        cellAt(xTo,yTo).setObstacleHidden(cellFrom.getObstacleId(),cellFrom.getDirection()); // set destination
        Coords coords = new Coords(xTo,yTo);
        obstacles.put(cellFrom.getObstacleId(),coords); // update obstacle list
        cellFrom.setEmpty(); // update source cell
    }
    public int getNextObstacleId(){
        if(obstacleList.isEmpty()){
            return 0;
        }
        Collections.sort(obstacleList);
        int id = obstacleList.get(0);
        return id;
    }
    public void deleteObstacleAt(int x, int y){
        int obstacleId = cellAt(x,y).getObstacleId();
        cellAt(x,y).setEmpty();
        obstacles.remove(obstacleId);
        obstacleList.add(obstacleId);
    }
    public String discoverObstacleNo(int obstacleId, int imageId){
        // handle cell data
        Coords obstacle = obstacles.get(obstacleId);
        Cell cell  = cellAt(obstacle.getX(),obstacle.getY());
        cell.revealObstacle(imageId);
        String dir = ArenaGame1.dirToString(cell.getDirection());
        // handle game data - to keep track
        discoveredObstacles.put(obstacleId,obstacle);
        Log.d("discover", "Obstacle "+obstacleId+" ("+obstacle.getX()+","+obstacle.getY()+" "+dir+" found");
        return "Obstacle "+obstacleId+": ("+obstacle.getX()+","+obstacle.getY()+") "+dir+" \nImage ID: " +imageId;
    }

    public Boolean checkGameFinish(){
        if(discoveredObstacles.size() == obstacles.size()){return true;}
        return false;
    }

    public String getMapString(){
        String message = "";
        Integer count = 0;
        Log.d("convertMapString","list: "+ obstacleList.toString());
        for (int i=1; i<9;i++){ // loop through all possible obstacle numbers
            // If obstacle has been assigned
            if(!obstacleList.contains(Integer.valueOf(i))){ // if obstacle exists, add it to the string, increment count
                count +=1;
                int X = obstacles.get(i).getX();
                int Y = obstacles.get(i).getY();
                Directions dir =  cellAt(X,Y).getDirection();
                message = message.concat("" + X + " " + Y + " " + ArenaGame1.dirToInt(dir) + " ");
            }
        }
        message = message.trim(); // remove space
        String result = "MAP,"+count.toString()+" ";
        if(message!=""){
            result = result.concat(message+"#"); // append count and obstacle number into front of string
        }
        Log.d("convertMapString", result);
        return result;
    }

    public HashMap<Integer,Coords> getObstacles(){
        return obstacles;
    }

    /***********************************ROBO FUNCTIONS********************************************/

    public void spawnRobot(int x, int y){
        Directions dir = Directions.NORTH;
        int top = y+1;
        int btm = y-1;
        int left = x-1;
        int right = x+1;
        robot = new Robot(dir, x, y);
        cellAt(left,top).setRobot(Cell.Types.R_TOP_LEFT, dir);
        cellAt(x,top).setRobot(Cell.Types.R_TOP_CENTER, dir);
        cellAt(right,top).setRobot(Cell.Types.R_TOP_RIGHT, dir);
        cellAt(left,y).setRobot(Cell.Types.R_MID_LEFT, dir);
        cellAt(x,y).setRobot(Cell.Types.R_MID_CENTER, dir);
        cellAt(right,y).setRobot(Cell.Types.R_MID_RIGHT, dir);
        cellAt(left,btm).setRobot(Cell.Types.R_BTM_LEFT, dir);
        cellAt(x,btm).setRobot(Cell.Types.R_BTM_CENTER, dir);
        cellAt(right,btm).setRobot(Cell.Types.R_BTM_RIGHT, dir);
    }
    public Robot getRobot(){return robot;}
    public void setRobot(int x, int y, Directions dir){
        Log.d("setRobot", ""+x+y+dir);
        int btm, top, left, right;
        robot.setY(y);
        robot.setX(x);
        robot.setDirection(dir);
        switch (dir){
            case NORTH:
                btm = y-1;
                top = y+1;
                left = x-1;
                right = x+1;
                cellAt(left,top).setRobot(Cell.Types.R_TOP_LEFT, dir);
                cellAt(x,top).setRobot(Cell.Types.R_TOP_CENTER, dir);
                cellAt(right,top).setRobot(Cell.Types.R_TOP_RIGHT, dir);
                cellAt(left,y).setRobot(Cell.Types.R_MID_LEFT, dir);
                cellAt(x,y).setRobot(Cell.Types.R_MID_CENTER, dir);
                cellAt(right,y).setRobot(Cell.Types.R_MID_RIGHT, dir);
                cellAt(left,btm).setRobot(Cell.Types.R_BTM_LEFT, dir);
                cellAt(x,btm).setRobot(Cell.Types.R_BTM_CENTER, dir);
                cellAt(right,btm).setRobot(Cell.Types.R_BTM_RIGHT, dir);
                break;
            case SOUTH:
                btm = y+1;
                top = y-1;
                left = x-1;
                right = x+1;
                cellAt(right,top).setRobot(Cell.Types.R_TOP_LEFT, dir);
                cellAt(x,top).setRobot(Cell.Types.R_TOP_CENTER, dir);
                cellAt(left,top).setRobot(Cell.Types.R_TOP_RIGHT, dir);
                cellAt(right,y).setRobot(Cell.Types.R_MID_LEFT, dir);
                cellAt(x,y).setRobot(Cell.Types.R_MID_CENTER, dir);
                cellAt(left,y).setRobot(Cell.Types.R_MID_RIGHT, dir);
                cellAt(right,btm).setRobot(Cell.Types.R_BTM_LEFT, dir);
                cellAt(x,btm).setRobot(Cell.Types.R_BTM_CENTER, dir);
                cellAt(left,btm).setRobot(Cell.Types.R_BTM_RIGHT, dir);
                break;
            case EAST: //flip x and y coords
                btm = x-1;
                top = x+1;
                left = y+1;
                right = y-1;
                Log.d("setRobot","East"+btm+top+left+right);
                cellAt(top,left).setRobot(Cell.Types.R_TOP_LEFT, dir);
                cellAt(top,y).setRobot(Cell.Types.R_TOP_CENTER, dir);
                cellAt(top,right).setRobot(Cell.Types.R_TOP_RIGHT, dir);
                cellAt(x,left).setRobot(Cell.Types.R_MID_LEFT, dir);
                cellAt(x,y).setRobot(Cell.Types.R_MID_CENTER, dir);
                cellAt(x,right).setRobot(Cell.Types.R_MID_RIGHT, dir);
                cellAt(btm,left).setRobot(Cell.Types.R_BTM_LEFT, dir);
                cellAt(btm,y).setRobot(Cell.Types.R_BTM_CENTER, dir);
                cellAt(btm,right).setRobot(Cell.Types.R_BTM_RIGHT, dir);
                break;
            case WEST:
                btm = x+1;
                top = x-1;
                left = y-1;
                right = y+1;
                cellAt(top,left).setRobot(Cell.Types.R_TOP_LEFT, dir);
                cellAt(top,y).setRobot(Cell.Types.R_TOP_CENTER, dir);
                cellAt(top,right).setRobot(Cell.Types.R_TOP_RIGHT, dir);
                cellAt(x,left).setRobot(Cell.Types.R_MID_LEFT, dir);
                cellAt(x,y).setRobot(Cell.Types.R_MID_CENTER, dir);
                cellAt(x,right).setRobot(Cell.Types.R_MID_RIGHT, dir);
                cellAt(btm,left).setRobot(Cell.Types.R_BTM_LEFT, dir);
                cellAt(btm,y).setRobot(Cell.Types.R_BTM_CENTER, dir);
                cellAt(btm,right).setRobot(Cell.Types.R_BTM_RIGHT, dir);
        }
    }

    public void roboForward(){
        Log.d("forward","");
        Cell cell1, cell2, cell3; // These are placeholders for the frontier cells of the car, to check for collision
        int x = robot.getX();
        int y = robot.getY();
        int a=0,b=0,c=0;
        int cx = 0; // cx and cy are used to store coords for the centre-frontier cell. Used to check if it is still inGrid()
        int cy = 0;
        Directions dir = robot.getDirection();
        try{
            switch (dir){
                case NORTH:
                    y = y+1;
                    a = x+1;
                    b = x-1;
                    c = y+1;
                    Log.d("roboForward","coords "+y+c);
                    cell1 = cellAt(x,c);
                    cell2 = cellAt(a,c);
                    cell3 = cellAt(b,c);
                    cx = x;
                    cy = c;
                    break;
                case SOUTH:
                    y = y-1;
                    a = x+1;
                    b = x-1;
                    c = y-1;
                    cell1 = cellAt(x,c);
                    cell2 = cellAt(a,c);
                    cell3 = cellAt(b,c);
                    cx = x;
                    cy = c;
                    break;
                case EAST:
                    x = x+1;
                    a = y-1;
                    b = y+1;
                    c = x+1;
                    cell1 = cellAt(c,y);
                    cell2 = cellAt(c,a);
                    cell3 = cellAt(c,b);
                    cx = c;
                    cy = y;
                    break;
                case WEST:
                    x = x-1;
                    a = y-1;
                    b = y+1;
                    c = x-1;
                    cell1 = cellAt(c,y);
                    cell2 = cellAt(c,a);
                    cell3 = cellAt(c,b);
                    cx = c;
                    cy = y;
                    break;
                default:
                    cell1 = cellAt(x,y);
                    cell2 = cellAt(x,y);
                    cell3 = cellAt(x,y);
                    break;
            }
            Log.d("roboForward","cell1:"+cell1.getX()+","+cell1.getY()+" "+dir+" "+cell1.isEmpty());
            Log.d("roboForward","cell2:"+cell2.getX()+","+cell2.getY()+" "+dir+" "+cell2.isEmpty());
            Log.d("roboForward","cell3:"+cell3.getX()+","+cell3.getY()+" "+dir+" "+cell3.isEmpty());
            if(!(cell1.isEmpty() && cell2.isEmpty() && cell3.isEmpty())){
                Log.d("movement", "cell occupied");
            } else if(!inGrid(cx,cy)){
                Log.d("movement","out of grid");
            } else{
                setRobot(x,y,dir);
                robot.setX(x);
                robot.setY(y);
                robot.setDirection(dir);
                switch(dir){
                    case NORTH:
                        cellAt(x,y-2).setTraversed();
                        cellAt(a,y-2).setTraversed();
                        cellAt(b,y-2).setTraversed();
                        break;
                    case SOUTH:
                        cellAt(x,y+2).setTraversed();
                        cellAt(a,y+2).setTraversed();
                        cellAt(b,y+2).setTraversed();
                        break;
                    case EAST:
                        cellAt(x-2,y).setTraversed();
                        cellAt(x-2,a).setTraversed();
                        cellAt(x-2,b).setTraversed();
                        break;
                    case WEST:
                        cellAt(x+2,y).setTraversed();
                        cellAt(x+2,a).setTraversed();
                        cellAt(x+2,b).setTraversed();
                        break;
                }
         }
        }catch(Exception e){
            Log.e("roboForward", "e "+e);
        }
    }

    public void roboReverse(){
        Log.d("reverse","");
        Cell cell1, cell2, cell3; // keep track of frontier cells to check for obstruction
        int x = robot.getX();
        int y = robot.getY();
        int a=0,b=0,c=0;
        int cx=0; // cx and cy are used to store coords for the centre-frontier cell. Used to check if it is still inGrid()
        int cy=0;
        Directions dir = robot.getDirection();
        try{
            switch (dir){
                case NORTH: //move south
                    y = y-1; // shift middle
                    a = x+1; // right
                    b = x-1; // left
                    c = y-1; // frontier y axis
                    Log.d("roboReverse","coords "+y+c);
                    cell1 = cellAt(x,c);
                    cell2 = cellAt(a,c);
                    cell3 = cellAt(b,c);
                    cx = x;
                    cy = c;
                    break;
                case SOUTH:
                    y = y+1;
                    a = x+1;
                    b = x-1;
                    c = y+1;
                    cell1 = cellAt(x,c);
                    cell2 = cellAt(a,c);
                    cell3 = cellAt(b,c);
                    cx = x;
                    cy = c;
                    break;
                case EAST:   // <- movement
                    x = x-1; // shift middle x axis
                    a = y+1; // right
                    b = y-1; // left
                    c = x-1; // frontier x axis
                    cell1 = cellAt(c,y);
                    cell2 = cellAt(c,a);
                    cell3 = cellAt(c,b);
                    cx = c;
                    cy = y;
                    break;
                case WEST:
                    x = x+1;
                    a = y+1;
                    b = y-1;
                    c = x+1;
                    cell1 = cellAt(c,y);
                    cell2 = cellAt(c,a);
                    cell3 = cellAt(c,b);
                    cx = c;
                    cy = y;
                    break;
                default:
                    cell1 = cellAt(x,y);
                    cell2 = cellAt(x,y);
                    cell3 = cellAt(x,y);
                    break;
            }
            Log.d("roboReverse","cell1:"+cell1.getX()+","+cell1.getY()+" "+dir+" "+cell1.isEmpty());
            Log.d("roboReverse","cell2:"+cell2.getX()+","+cell2.getY()+" "+dir+" "+cell2.isEmpty());
            Log.d("roboReverse","cell3:"+cell3.getX()+","+cell3.getY()+" "+dir+" "+cell3.isEmpty());
            if(!(cell1.isEmpty() && cell2.isEmpty() && cell3.isEmpty())){
                Log.d("movement", "cell occupied");
            }
            else if(!inGrid(cx,cy)){
                Log.d("movement","out of grid");
            }
            else{
                setRobot(x,y,dir);
                robot.setX(x);
                robot.setY(y);
                robot.setDirection(dir);
                switch(dir){
                    case NORTH:
                        cellAt(x,y+2).setTraversed();
                        cellAt(a,y+2).setTraversed();
                        cellAt(b,y+2).setTraversed();
                        break;
                    case SOUTH:
                        cellAt(x,y-2).setTraversed();
                        cellAt(a,y-2).setTraversed();
                        cellAt(b,y-2).setTraversed();
                        break;
                    case WEST:
                        cellAt(x-2,y).setTraversed();
                        cellAt(x-2,a).setTraversed();
                        cellAt(x-2,b).setTraversed();
                        break;
                    case EAST:
                        cellAt(x+2,y).setTraversed();
                        cellAt(x+2,a).setTraversed();
                        cellAt(x+2,b).setTraversed();
                        break;
                }
            }
        }catch(Exception e){
            Log.e("roboForward", "e "+e);
        }
    }

    public void roboTurnRight(){
        Log.d("turn Right","");
        int x = robot.getX();
        int y = robot.getY();
        Directions dir = robot.getDirection();
        Log.d("beforeTurn", ""+x+y+dir);
        switch(dir){
            case NORTH:
                dir = Directions.EAST;
                break;
            case EAST:
                dir = Directions.SOUTH;
                break;
            case SOUTH:
                dir = Directions.WEST;
                break;
            case WEST:
                dir = Directions.NORTH;
                break;
        }
        setRobot(x,y,dir);
        Log.d("afterTurn", ""+x+y+dir);
    }

    public void roboTurnLeft(){
        int x = robot.getX();
        int y = robot.getY();
        Directions dir = robot.getDirection();
        switch(dir){
            case NORTH:
                dir = Directions.WEST;
                break;
            case EAST:
                dir = Directions.NORTH;
                break;
            case SOUTH:
                dir = Directions.EAST;
                break;
            case WEST:
                dir = Directions.SOUTH;
        }
        setRobot(x,y,dir);
    }

    public void setRobotTraversed(){
        int x = robot.getX();
        int y = robot.getY();
        cellAt(x-1,y).setTraversed();
        cellAt(x-1,y+1).setTraversed();
        cellAt(x-1,y-1).setTraversed();
        cellAt(x,y).setTraversed();
        cellAt(x,y+1).setTraversed();
        cellAt(x,y-1).setTraversed();
        cellAt(x+1,y).setTraversed();
        cellAt(x+1,y+1).setTraversed();
        cellAt(x+1,y-1).setTraversed();
    }
}
