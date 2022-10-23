package com.example.myapplication.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Entities.AppViewModel;
import com.example.myapplication.Entities.ArenaGame1;
import com.example.myapplication.R;

import java.util.regex.Pattern;

/**
 * This fragment is meant for additional debug tools.
 * Contains a "simulate write from bluetooth device", to help test addReadMessage function in ArenaGame1 to ensure that
 * All respective cases in the addRead function is functioning properly. Note that this flow does not involve bluetooth.
 */
public class FragmentChat extends Fragment {
    AppViewModel viewModel;
    ArenaGame1 updatedGame1;
    View view;
    EditText edEnterMessage;
    TextView edCreateMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Init viewModel
        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        // Get updatedGame1 from viewModel
        updatedGame1 = viewModel.getMutableLiveGame1().getValue();
        viewModel.getMutableLiveGame1().observe(getViewLifecycleOwner(), liveGame1Instance->{
            updatedGame1 = liveGame1Instance;
        });

        edCreateMessage = view.findViewById(R.id.edCreateMessage);
        edEnterMessage = view.findViewById(R.id.edEnterMessage);
        Button send = view.findViewById(R.id.btnSend);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edEnterMessage.getText().toString();

                String temp_read = updatedGame1.getTempReadMsg(); // this get the leftover from prev
                temp_read = temp_read.concat(message); // add it to the new msg
                Pattern pound = Pattern.compile("#");
                String[] inputs = pound.split(temp_read); // separate by pound
                if (inputs.length != 0) {
                    if(!temp_read.equals("")) {
                        if (temp_read.charAt(temp_read.length() - 1) == '#') {
                            updatedGame1.addReadMessage(inputs[0]);
                            for (int i = 1; i < inputs.length; i++) {
                                updatedGame1.addReadMessage(inputs[i]);
                            }
                            updatedGame1.setTempReadMsg("");
                        } else {
                            if(!(inputs.length == 1)){
                                updatedGame1.addReadMessage(inputs[0]);
                            }
                            for (int i = 1; i < inputs.length - 1; i++) {
                                updatedGame1.addReadMessage(inputs[i]);
                            }
                            updatedGame1.setTempReadMsg(inputs[inputs.length - 1]);
                        }
                    }
                }
                viewModel.getMutableLiveGame1().setValue(updatedGame1);
                edEnterMessage.setText("");
                edCreateMessage.setText(""+updatedGame1.getTempReadMsg());
            }
        });
        // if last char is #, send everything, else save last index
    }
}