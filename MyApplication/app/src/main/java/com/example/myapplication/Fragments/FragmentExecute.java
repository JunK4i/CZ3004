package com.example.myapplication.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.myapplication.Entities.AppViewModel;
import com.example.myapplication.Entities.ArenaGame1;
import com.example.myapplication.R;

/**
 * Timer exists here. The time only runs while the fragment is opened, and current time offset will automatically be saved/stored in
 * the game. This allows the timer to be resumed once the user comes back to this page.
 */
public class FragmentExecute extends Fragment {
    AppViewModel viewModel;
    ArenaGame1 updatedGame1;
    View view;
    private Chronometer countDown, countDown2;
    private Button buttonReset, buttonReset2,buttonStartPause, buttonStartPause2;
    private Boolean running = false, running2 = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_execute, container, false);
        return view;
    }

    @Override
    public void onDestroyView() {
        if(running){
            countDown.stop();
            long pauseOffset = SystemClock.elapsedRealtime() - countDown.getBase();
            updatedGame1.setPauseOffset(pauseOffset);
            running = false;
            buttonStartPause.setText("Run");
        }
        if(running2){
            countDown2.stop();
            long pauseOffset2 = SystemClock.elapsedRealtime() - countDown2.getBase();
            updatedGame1.setPauseOffset2(pauseOffset2);
            running2 = false;
            buttonStartPause2.setText("Run");
        }
        super.onDestroyView();
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        countDown = view.findViewById(R.id.text_view_countdown);
        countDown2 = view.findViewById(R.id.text_view_countdown2);
        buttonReset = view.findViewById(R.id.button_reset);
        buttonReset2 = view.findViewById(R.id.button_reset2);
        buttonStartPause = view.findViewById(R.id.button_start_pause);
        buttonStartPause2 = view.findViewById(R.id.button_start_pause2);

        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        updatedGame1 = viewModel.getMutableLiveGame1().getValue();

        countDown.setBase(SystemClock.elapsedRealtime() - updatedGame1.getPauseOffset());
        countDown2.setBase(SystemClock.elapsedRealtime() - updatedGame1.getPauseOffset2());

        viewModel.getMutableLiveGame1().observe(getViewLifecycleOwner(), liveGame1Instance->{
            updatedGame1 = liveGame1Instance;
            if(updatedGame1.getPauseOffset()!=0){
                countDown.setBase(SystemClock.elapsedRealtime() - updatedGame1.getPauseOffset());
            } else if (updatedGame1.getPauseOffset2()!=0){
                countDown2.setBase(SystemClock.elapsedRealtime() - updatedGame1.getPauseOffset2());
            }
            if(updatedGame1.isComplete()){
                pauseChronometer(1);
            }
        });

        buttonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!running){
                    startChronometer(1);
                } else{
                    pauseChronometer(1);
                }
            }
        });
        buttonStartPause2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!running2){
                    startChronometer(2);
                } else{
                    pauseChronometer(2);
                }
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetChronometer(1);
            }
        });
        buttonReset2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetChronometer(2);
            }
        });

    }

    public void startChronometer(int type){
        if(type == 1){
            if(!running){
                countDown.setBase(SystemClock.elapsedRealtime() - updatedGame1.getPauseOffset());
                updatedGame1.setPauseOffset(0);
                countDown.start();
                running = true;
                buttonStartPause.setText("Pause");
                updatedGame1.writeToChatUtil("START,IMAGE#");

            }
        } else if(type ==2){
            if(!running2){
                countDown2.setBase(SystemClock.elapsedRealtime() - updatedGame1.getPauseOffset2());
                updatedGame1.setPauseOffset2(0);
                countDown2.start();
                running2 = true;
                buttonStartPause2.setText("Pause");
                updatedGame1.writeToChatUtil("START,FAST#");
            }
        }
    }

    public void pauseChronometer(int type){
        if(type == 1){
            if(running){
                countDown.stop();
                long pauseOffset = SystemClock.elapsedRealtime() - countDown.getBase();
                updatedGame1.setPauseOffset(pauseOffset);
                running = false;
                buttonStartPause.setText("Run");
            }
        } else{
            if(running2){
                countDown2.stop();
                long pauseOffset2 = SystemClock.elapsedRealtime() - countDown2.getBase();
                updatedGame1.setPauseOffset2(pauseOffset2);
                running2 = false;
                buttonStartPause2.setText("Run");
            }
        }
    }

    public void resetChronometer(int type){
        if(type == 1){
            countDown.setBase(SystemClock.elapsedRealtime());
            updatedGame1.setPauseOffset(0);
        } else{
            countDown2.setBase(SystemClock.elapsedRealtime());
            updatedGame1.setPauseOffset2(0);
        }
    }

}