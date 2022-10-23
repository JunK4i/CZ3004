package com.example.myapplication.Fragments;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Entities.AppViewModel;
import com.example.myapplication.Entities.ArenaGame1;
import com.example.myapplication.R;

/**
 * Settings fragment contains functions to set up the map with obstacles, reset map and bluetooth connection, set manual
 */
public class FragmentSettings extends Fragment{
    View view;
    private AppViewModel viewModel; //Contains Mutable LiveData of game instance
    private ArenaGame1 game1; // used to store latest instance of game1
    private int obstacleId; // current obstacle size +1
    private Button btnSendMap, btnAddObs;
    private NumberPicker xSelect,ySelect,dirSelect;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        // Game instance from main activity
        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        game1 = viewModel.getMutableLiveGame1().getValue(); // get value at this instance
        obstacleId = game1.getArenaGrid().getNextObstacleId();
        btnSendMap = view.findViewById(R.id.button);
        btnAddObs = view.findViewById(R.id.btnAddObs);
        xSelect = view.findViewById(R.id.xSelect);
        ySelect = view.findViewById(R.id.ySelect);
        dirSelect = view.findViewById(R.id.dirSelect);

        xSelect.setMinValue(0);
        xSelect.setMaxValue(19);
        ySelect.setMinValue(0);
        ySelect.setMaxValue(19);
        dirSelect.setMinValue(0);
        dirSelect.setMaxValue(3);

        TextView obstacleSettings = view.findViewById(R.id.obstacleSettings);
        // set observe to update game1 instance whenever viewModel receives an update
        viewModel.getMutableLiveGame1().observe(getViewLifecycleOwner(), liveGame1Instance->{
            game1 = liveGame1Instance;
            obstacleId = game1.getArenaGrid().getNextObstacleId();
            obstacleSettings.setText(""+obstacleId);
        });

        btnAddObs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("addObs", ""+xSelect.getValue()+ySelect.getValue()+ArenaGame1.intToDir(dirSelect.getValue()));
                game1.getArenaGrid().setObstacle(xSelect.getValue(),ySelect.getValue(),ArenaGame1.intToDir(dirSelect.getValue()));
                viewModel.getMutableLiveGame1().setValue(game1);
            }
        });

        btnSendMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("sendMap", ""+game1.getArenaGrid().getMapString());
                game1.writeToChatUtil(""+game1.getArenaGrid().getMapString());
            }
        });


        Button resetBluetooth = view.findViewById(R.id.btnResetBluetooth);
        resetBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game1.resetChatUtil();
                viewModel.getMutableLiveGame1().setValue(game1);
            }
        });

        Button resetGame1 = view.findViewById(R.id.btnResetGame1);
        resetGame1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                game1.resetGame();
                viewModel.getMutableLiveGame1().setValue(game1);
            }
        });


        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d("save", "out");
                game1.saveMap();
                viewModel.getMutableLiveGame1().setValue(game1);
            }
        });

        Button btnLoad = view.findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d("load", "out");
                game1.resetArenaGrid();
                game1.loadMap();
                viewModel.getMutableLiveGame1().setValue(game1);
            }
        });

        Switch manualSwitch = view.findViewById(R.id.manualSwitch);
        manualSwitch.setChecked(true);
        manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    manualSwitch.setChecked(true);
                    game1.setManual(true);
                    viewModel.getMutableLiveGame1().setValue(game1);
                } else{
                    manualSwitch.setChecked(false);
                    game1.setManual(false);
                    viewModel.getMutableLiveGame1().setValue(game1);
                }
            }
        });

        // Drag and Drop
        // https://developer.android.com/develop/ui/views/touch-and-input/drag-drop#drophelper
        obstacleSettings.setText(""+obstacleId);
        obstacleSettings.setTextColor(Color.WHITE);
        obstacleSettings.setBackgroundResource(R.drawable.hidden_obstacle);
        obstacleSettings.setTag("obstacleFromFragment");

        // Set listener that initialises a drag event
        obstacleSettings.setOnLongClickListener(v->{
            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
            ClipData dragData = new ClipData(
                    (CharSequence) v.getTag(),
                    new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item);
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(obstacleSettings);
            v.startDragAndDrop(
                    dragData,  // The data to be dragged
                    myShadow,  // The drag shadow builder
                    this,
                    0);
            return true;
        });

        // Listener for when a drag event has started
        obstacleSettings.setOnDragListener((v, event)->{
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:
                    if(event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
                        if(event.getClipDescription().getLabel().toString() == obstacleSettings.getTag()) { // if the event started from this item
                            v.setBackgroundColor(Color.BLUE);
                            v.invalidate();
                        }
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED: // Enters initial bounding box
                    v.setBackgroundColor(Color.GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION: // Subsequent to action_drag_entered, still inside
                    return true;
                case DragEvent.ACTION_DRAG_EXITED: // Move outside bounding box again
                    v.setBackgroundColor(Color.BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    // Invalidates the view to force a redraw.
                    if (event.getResult()) {
                        obstacleId = game1.getArenaGrid().getNextObstacleId();
                        if(obstacleId==0){
                            obstacleSettings.setText("X");
                        } else{
                            obstacleSettings.setText(""+obstacleId);
                        }
                        obstacleSettings.setBackgroundResource(R.drawable.hidden_obstacle);
                        v.invalidate();
                    } else {
                        obstacleSettings.setBackgroundResource(R.drawable.hidden_obstacle);
                        v.invalidate();
                    }
                    return true;

                // An unknown action type was received.
                default:
                    Log.e("DragDrop Example","Unknown action type received by View.OnDragListener.");
                    break;
            }
            return false;
        });
    }
}