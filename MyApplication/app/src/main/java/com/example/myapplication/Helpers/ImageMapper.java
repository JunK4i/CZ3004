package com.example.myapplication.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;

import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

/**
 * This class contains hashmap which maps a resource string name to an integer ID.
 * It also contains helper functions to help get the image resource, name or drawable directly
 */
public class ImageMapper {
    public static final HashMap<Integer,String> idURIMap = new HashMap<Integer,String>()
    {
        {
            put(0, "empty");
            put(1, "robo_top_left");
            put(2, "robo_top_center");
            put(3,"robo_top_right");
            put(4,"robo_mid_left");
            put(5, "robo_mid_center");
            put(6, "robo_mid_right");
            put(7, "robo_btm_left");
            put(8, "robo_btm_center");
            put(9, "robo_btm_right");
            put(10, "hidden_obstacle");
            put(11, "one");
            put(12, "two");
            put(13, "three");
            put(14, "four");
            put(15, "five");
            put(16, "six");
            put(17, "seven");
            put(18, "eight");
            put(19, "nine");
            put(20, "alphabet_a");
            put(21, "alphabet_b");
            put(22, "alphabet_c");
            put(23, "alphabet_d");
            put(24, "alphabet_e");
            put(25, "alphabet_f");
            put(26, "alphabet_g");
            put(27, "alphabet_h");
            put(28, "alphabet_s");
            put(29, "alphabet_t");
            put(30, "alphabet_u");
            put(31, "alphabet_v");
            put(32, "alphabet_w");
            put(33, "alphabet_x");
            put(34, "alphabet_y");
            put(35, "alphabet_z");
            put(36, "up_arrow");
            put(37, "down_arrow");
            put(38, "right_arrow");
            put(39, "left_arrow");
            put(40, "stop");
            put(41, "bullseye");
            put(42, "revealed");
            put(43, "up");
            put(44, "down");
            put(45, "left");
            put(46, "right");
        }
    };

    Context ctx;

    public ImageMapper( Context context){
        ctx = context;
    } //init with context

    public int getImageResource (int id){
        String uri = "@drawable/"+idURIMap.get(id);
        return ctx.getResources().getIdentifier(uri,null,ctx.getPackageName());
    }

    public String getName(int id){
        return idURIMap.get(id);
    }
    public Drawable getDrawable(int id){
        int imageResource = getImageResource(id);
        return ResourcesCompat.getDrawable(ctx.getResources(), imageResource, null);
    }
}
