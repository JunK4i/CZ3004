package com.example.myapplication.Entities;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.myapplication.ChatUtils;
import com.example.myapplication.Directions;
import com.example.myapplication.R;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ArenaGame1 contains all shared information regarding the image recognition task.
 * ArenaGame1 will be stored as MutableLiveData in the AppViewModel. This allows data to be shared and updated on different fragments.
 * Data that should persist outside of the fragment as well as throughout the app should live here.
 */
public class ArenaGame1 {
    private ArenaGrid arenaGrid; // ArenaGrid of {size} will be contain cells, obstacles and all functions require to manipulate the state of the game
    private int size;
    private ChatUtils chatUtils;  // Initialise in bluetooth fragment, contains the functions of the bluetooth connection thread. Other classes will access the chatUtils instance to send and read messages
    private ArrayAdapter<String> adapterReadChat, adapterWriteChat; // Adapter instances used to control string displays on bt fragment
    private ArrayAdapter<String> adapterDiscoveredObstacle, adapterStatus; // Adapter instances used to control string displays on main
    private boolean isManual; // Manual Movement Boolean
    private String temp_read_msg = ""; // Stores incomplete read information from bluetooth. Concat with next incoming message
    private Context context; // Main activity context
    private Boolean isComplete; // is Game 1 complete
    private long pauseOffset, pauseOffset2; // Timer offsets from execute fragment
    private String target; // Latest target
    private HashMap<Integer, Coords> saveMap; // Coords of obstacles to save
    private HashMap<Integer, Directions> saveMapDir; // Dir of obstacles to save

    public ArenaGame1(int size, Context context){ // Init
        this.context= context;
        this.size = size;
        arenaGrid = new ArenaGrid(size);
        adapterReadChat = new ArrayAdapter<String>(context, R.layout.message_layout);
        adapterWriteChat = new ArrayAdapter<String>(context, R.layout.message_layout);
        adapterStatus = new ArrayAdapter<String>(context, R.layout.message_layout);
        adapterDiscoveredObstacle = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        isManual = true;
        isComplete = false;
        saveMap = new HashMap<Integer,Coords>();
        saveMapDir = new HashMap<Integer,Directions>();
    }

    /***********************************RESET FUNCTIONS********************************************/

    // Init new arenaGrid
    public void resetArenaGrid(){
        arenaGrid = new ArenaGrid(size);
    }

    // Reset except bluetooth conntection
    public void resetGame(){
        arenaGrid = new ArenaGrid(size);
        adapterReadChat.clear();
        adapterWriteChat.clear();
        adapterStatus.clear();
        adapterDiscoveredObstacle.clear();
        isManual = true;
        isComplete = false;
        target = null;
        pauseOffset = 0;
        pauseOffset2 = 0;
        temp_read_msg = "";
    }

    // Clear bt chats chats and disconnect all threads
    public void resetChatUtil(){
        adapterReadChat.clear();
        adapterWriteChat.clear();
        adapterStatus.clear();
        chatUtils.stop();
        chatUtils = null;
    }

    /***********************************GETTERS SETTERS********************************************/

    public ArenaGrid getArenaGrid() {return arenaGrid;}
    public Context getContext(){return this.context;}
    public boolean isManual(){return isManual;}
    public void setManual(boolean isManual){this.isManual = isManual;}
    public String getTempReadMsg(){return temp_read_msg;}
    public void setTempReadMsg(String temp_read_msg){this.temp_read_msg = temp_read_msg;}
    public Boolean isComplete(){return isComplete;}
    public void setIsComplete(Boolean isComplete){this.isComplete = isComplete;}
    public long getPauseOffset(){
        Log.d("get offset1",""+pauseOffset);
        return pauseOffset;
    }
    public void setPauseOffset(long pauseOffset){
        Log.d("set offset1",""+pauseOffset);
        this.pauseOffset = pauseOffset;
    }
    public long getPauseOffset2(){
        Log.d("get offset2",""+pauseOffset2);
        return pauseOffset2;
    }
    public void setPauseOffset2(long pauseOffset2){
        Log.d("set offset2",""+pauseOffset2);
        this.pauseOffset2 = pauseOffset2;
    }
    public ArrayAdapter getDiscoveredAdapter(){return adapterDiscoveredObstacle;}
    public void setTarget(String target){this.target = target;}
    public String getTarget(){return target;}
    public void saveMap(){
        Log.d("save","");
        HashMap<Integer,Coords> obstacles = arenaGrid.getObstacles();
        for (Map.Entry<Integer,Coords> entry: obstacles.entrySet()){
            Coords coords= entry.getValue();
            Directions dir = arenaGrid.cellAt(coords.getX(),coords.getY()).getDirection();
            saveMap.put(entry.getKey(), entry.getValue());
            saveMapDir.put(entry.getKey(), dir);
        }
        return;
    }
    public void loadMap(){
        if(this.saveMap!=null){
            for(Map.Entry<Integer,Coords> entry: saveMap.entrySet()){
                Directions dir = saveMapDir.get(entry.getKey());
                Coords coords = entry.getValue();
                arenaGrid.setObstacle(coords.getX(),coords.getY(),dir);
            }
        }
        return;
    }

    /***********************************CHAT UTIL FUNCTIONS*****************************************/

    public ChatUtils getChatUtils(){return chatUtils;}
    public ArrayAdapter getWriteAdapter(){return adapterWriteChat;}
    public ArrayAdapter getReadAdapter(){return adapterReadChat;}
    public ArrayAdapter getStatusAdapter(){return adapterStatus;}
    public void setBtStatus(String status){
        adapterStatus.clear();
        adapterStatus.add(status);
    }
    public void initChatUtils(Handler handler){
        chatUtils = new ChatUtils(handler);
    }
    public boolean isChatUtilsInit(){
        if(chatUtils == null){return false;}
        return true;
    }

    // Called by handler in bluetooth fragment
    // Add to chat display, for messages that is read from BT
    public void addReadMessage(String message){
        try{
            // Remove none text noise from message
            message = message.trim();
            message = message.replaceAll("\t","");
            message = message.replaceAll("\n", "");
            message = message.replaceAll("\\uFEFF", "");
            message = message.replaceAll("\\p{C}", "");
            adapterReadChat.insert(message,0); // insert message from top
            Log.d("addRead",""+message);
            Pattern comma = Pattern.compile(",");
            String[] messages = comma.split(message);
            switch(messages[0]){
                case "TARGET":
                    String result = arenaGrid.discoverObstacleNo(Integer.parseInt(this.target),Integer.valueOf(messages[1]));
                    Log.d("addRead","Target"+messages);
                    Log.d("addRead","Result"+result);
                    adapterDiscoveredObstacle.insert(result,0);
                    isComplete = arenaGrid.checkGameFinish();
                    break;
                case "ROBOT":
                    Log.d("addRead","Robot: raw "+messages[1]+","+messages[2]+","+messages[3]);
                    int x = Math.floorDiv(Integer.valueOf(messages[1]),10);
                    int y = Math.floorDiv(Integer.valueOf(messages[2]),10);
                    int intDir = Integer.parseInt(messages[3].trim());
                    Directions dir = intToDir(intDir);
                    Log.d("addRead","Robot: "+x+","+y+","+dirToString(dir));
                    moveRobot(x,y,dir);
                    break;
                case "STATUS":
                    Log.d("addRead","Status: messages1 "+messages[1]);
                    arenaGrid.getRobot().setStatus(messages[1]);
                    Pattern colon = Pattern.compile(":");
                    String[] target = colon.split(messages[1]);
                    Log.d("addRead","Status: target0 "+target[0]);
                    Log.d("addRead","Status: target1 "+target[1]);
                    if(target[0].equals("Looking For Target ")){
                        Log.d("addRead", "Status: HI "+target[1]);
                        this.setTarget(target[1]);
                    }
                    break;
                case "STOP":
                    isComplete = true;
                case "\n":
                    break;
            }
        }catch(Exception e){
            Log.e("error",""+e);
        }
    }

    // Write to bluetooth device flow:
    // Event triggers calls writeToChatUtil->chatUtils will send the "sent to BT" message back through handler in bt fragment
    // Bt fragment calls addWriteMessage to display the "sent to BT" message on screen

    // Write message to display
    public void addWriteMessage(String message){
        Log.d("writeMsg","Message "+message);
        adapterWriteChat.insert(message,0);
    }

    // Write message to bluetooth device
    public void writeToChatUtil(String message){
        if(isChatUtilsInit()){
            Log.d("writeToChatUtil","in if: "+message);
            chatUtils.write(message.getBytes());
        }
        Log.d("writeToChatUtil","message: "+message);
    }

    /***********************************UTILITY FUNCTIONS*****************************************/

    public static String dirToString(Directions dir){
        switch (dir){
            case NORTH: return "NORTH";
            case SOUTH:return "SOUTH";
            case EAST:return "EAST";
            case WEST:return "WEST";
            case NULL:return "NULL";
        }
        return "NULL";
    }

    public static int dirToInt(Directions dir){
        switch (dir){
            case NORTH: return 0;
            case SOUTH:return 2;
            case EAST:return 1;
            case WEST:return 3;
        }
        return -1;
    }

    public static Directions intToDir(int dir){
        switch (dir){
            case 0: return Directions.NORTH;
            case 1: return Directions.EAST;
            case 2: return Directions.SOUTH;
            case 3: return Directions.WEST;
        }
        return Directions.NULL;
    }

    // Move robot and set traversed cells
    public void moveRobot(int newX, int newY, Directions newDir){
        Robot robot = arenaGrid.getRobot();
        int x = robot.getX();
        int y = robot.getY();
        Directions currDir = robot.getDirection();
        int deltaX = newX - x;
        int deltaY = newY - y;
        // Account for diagonal movement, need to set traversed twice
        if((deltaX==1 && deltaY==1)||(deltaX==1 && deltaY==-1)||(deltaX==-1 && deltaY==-1)||(deltaX==-1 && deltaY==1)){
            arenaGrid.setRobotTraversed();
            switch (currDir){
                case WEST:
                case EAST:
                    arenaGrid.setRobot(x,newY,currDir);
                    arenaGrid.setRobotTraversed();
                    arenaGrid.setRobot(newX,newY,newDir);
                    break;
                case SOUTH:
                case NORTH:
                    arenaGrid.setRobot(newX,y,currDir);
                    arenaGrid.setRobotTraversed();
                    arenaGrid.setRobot(newX,newY,newDir);
                    break;
            }
        } else{
            arenaGrid.setRobotTraversed();
            arenaGrid.setRobot(newX,newY,newDir);
        }
    }
}
