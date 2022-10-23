package com.example.myapplication;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.ClipData;
import android.content.ClipDescription;

import android.animation.Animator;

import android.content.Context;
import android.content.pm.PackageManager;


import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.GridRecyclerAdapter;
import com.example.myapplication.Entities.AppViewModel;
import com.example.myapplication.Entities.ArenaGame1;
import com.example.myapplication.Entities.Cell;
import com.example.myapplication.Fragments.FragmentBluetooth;
import com.example.myapplication.Fragments.FragmentChat;
import com.example.myapplication.Fragments.FragmentExecute;
import com.example.myapplication.Fragments.FragmentSettings;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Main Activity contains the grid, status displays, robot buttons, sprites and fragment window for the different tabs.
 */
public class MainActivity extends AppCompatActivity{
    private RecyclerView gridRecyclerView;
    private GridRecyclerAdapter gridRecyclerAdapter;
    private ArenaGame1 updatedGame1;
    private ImageView btnBluetooth, btnChat, btnSettings, btnExecute;
    private ImageButton btnUp, btnDown, btnLeft, btnRight;
    private TextView roboStatus,roboTarget, roboDir, roboX,roboY;
    private AppViewModel viewModel;
    private ListView listView;

    private final int BLUETOOTH_CONNECT_REQUEST = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ViewModel to share information with fragments
        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        // Animate car sprite
        final ImageView car = (ImageView)findViewById(R.id.car);
        ObjectAnimator translate = ObjectAnimator.ofFloat(car, View.TRANSLATION_X, 2800);
        translate.setRepeatCount(1000);
        translate.setDuration(2000);
        translate.start();

        //Add listeners to Fragment Buttons
        btnBluetooth = findViewById(R.id.btnBluetooth);
        btnChat = findViewById(R.id.btnChat);
        btnSettings = findViewById(R.id.btnSettings);
        btnExecute = findViewById(R.id.btnExecute);

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new FragmentBluetooth());
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new FragmentChat());
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new FragmentSettings());
            }
        });
        btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new FragmentExecute());
            }
        });

        //Add listeners to movement buttons
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updatedGame1.isManual()){ // MANUAL SEND TO RPI/MDPTOOL. USE CHAT UTIL TO SEND TO RPI. RPI will read # as of line
                        updatedGame1.writeToChatUtil("SF#");
                        updatedGame1.getArenaGrid().roboForward();
                        viewModel.getMutableLiveGame1().setValue(updatedGame1);
                }
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updatedGame1.isManual()){
                        updatedGame1.writeToChatUtil("SF#");
                        updatedGame1.getArenaGrid().roboTurnRight();
                        viewModel.getMutableLiveGame1().setValue(updatedGame1);
                }
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updatedGame1.isManual()){
                        updatedGame1.writeToChatUtil("SF#");
                        updatedGame1.getArenaGrid().roboTurnLeft();
                        viewModel.getMutableLiveGame1().setValue(updatedGame1);
                    }
                }

        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updatedGame1.isManual()){
                        updatedGame1.writeToChatUtil("SX#");
                        updatedGame1.getArenaGrid().roboReverse();
                        viewModel.getMutableLiveGame1().setValue(updatedGame1);
                    }
                }

        });

        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatedGame1.writeToChatUtil("SF010#");
            }
        });

        // Initialise game
        ArenaGame1 game1 = new ArenaGame1(20, this); // Intialise obstacle game
        viewModel.getMutableLiveGame1().setValue(game1); // store instance in viewModal
        updatedGame1 = viewModel.getMutableLiveGame1().getValue();
        // Robo Status
        roboStatus = findViewById(R.id.roboStatusView);
        roboTarget = findViewById(R.id.roboTarget);
        roboDir = findViewById(R.id.roboDirection);
        roboX = findViewById(R.id.roboX);
        roboY = findViewById(R.id.roboY);

        // Listen and update latest game
        viewModel.getMutableLiveGame1().observe(this,liveGame1Instance->{
            updatedGame1 = liveGame1Instance;
            roboStatus.setText(updatedGame1.getArenaGrid().getRobot().getStatus());
            roboTarget.setText("Target: "+updatedGame1.getTarget());
            roboDir.setText(updatedGame1.dirToString(updatedGame1.getArenaGrid().getRobot().getDirection()));
            roboX.setText(""+updatedGame1.getArenaGrid().getRobot().getX());
            roboY.setText(""+updatedGame1.getArenaGrid().getRobot().getY());

        });

        // Create Grid
        gridRecyclerView = findViewById(R.id.gridRecycleView);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(this, 20));
        gridRecyclerAdapter = new GridRecyclerAdapter(viewModel.getMutableLiveGame1(), this);
        gridRecyclerView.setAdapter(gridRecyclerAdapter);
        gridRecyclerView.setNestedScrollingEnabled(false); // Disable grid scrolling

        // Setup ListView
        listView = findViewById(R.id.list_view);
        listView.setAdapter(updatedGame1.getDiscoveredAdapter());

        ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
        // handles the different drag events for the obstacleSetting block
        mainLayout.setOnDragListener((v, event)->{
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:
                    if(event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){return true;}
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED: // Enters initial bounding box
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION: // Subsequent to action_drag_entered, still inside
                    return true;
                case DragEvent.ACTION_DRAG_EXITED: // Move outside bounding box again
                    return true;
                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0); // Gets the item containing the dragged data.
                    CharSequence dragData = item.getText(); // Gets the tag from the item.
                    Pattern colon = Pattern.compile(":");
                    String[] check = colon.split(dragData);
                    if(check[0].equals("obstacleFromCell")){
                        Pattern comma = Pattern.compile(",");
                        String[] coords = comma.split(check[1]);
                        // Get source coords
                        Integer sourceX = Integer.parseInt(coords[0]);
                        Integer sourceY = Integer.parseInt(coords[1]);
                        ArenaGame1 latestGame = viewModel.getMutableLiveGame1().getValue();
                        Cell cell = viewModel.getMutableLiveGame1().getValue().getArenaGrid().cellAt(sourceX,sourceY);
                        Toast.makeText(this, "Delete "+"["+cell.getX()+","+cell.getY()+"]", Toast.LENGTH_SHORT).show();
                        if(cell.getType().equals(Cell.Types.OBSTACLE)){
                            updatedGame1.getArenaGrid().deleteObstacleAt(cell.getX(), cell.getY());
                            viewModel.getMutableLiveGame1().setValue(updatedGame1);
                        }
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                // An unknown action type was received.
                default:
                    Log.e("DragDrop Example","Unknown action type received by View.OnDragListener.");
                    break;
            }
            return false;
        });

        //Ask BT permission
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "btAdapter not supported");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_REQUEST);
            Log.e("MainActivity", "ask btconnect permission");
            return;
        }
        bluetoothAdapter.enable(); // Last Resort. This enables the device bluetooth without user permission. API 33 onwards deprecated. Intended only for "power manager" apps.

    }

    // Replace fragment on btn click
    private void replaceFragment(Fragment varFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, varFragment);
        fragmentTransaction.commit();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_CONNECT_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Bt connect permission", "given");
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}