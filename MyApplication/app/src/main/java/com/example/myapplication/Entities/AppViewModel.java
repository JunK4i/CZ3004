package com.example.myapplication.Entities;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Entities.ArenaGame1;

/**
 * Stores data that is shared between fragments and their host activity
 * It is lifecycle aware, and can be used from anywhere to set and retrieve live data
 * https://developer.android.com/guide/fragments/communicate
 *
 * MutableLiveData has a few functions:
 * postValue(backend thread), setValue() use for frontend changes to live data
 * getValue() to get the current instance of the data
 * observe is used to constantly modify the variable whenever the MutableLiveData is updated
 */
public class AppViewModel extends ViewModel {
    private MutableLiveData<ArenaGame1> currentLiveGame1;
        public MutableLiveData<ArenaGame1> getMutableLiveGame1(){
            if(currentLiveGame1 == null){ // if no existing game init a new live game
                currentLiveGame1 = new MutableLiveData<ArenaGame1>();
            }
            return currentLiveGame1;
    }
}
