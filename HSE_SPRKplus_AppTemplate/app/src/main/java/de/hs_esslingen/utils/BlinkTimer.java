package de.hs_esslingen.utils;

import android.util.Log;

import com.orbotix.ConvenienceRobot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Steven on 24.05.2017.
 */

public class BlinkTimer{
    public BlinkTimer(final int time,final int delay, final ConvenienceRobot mRobot, final int[] color) {
            Timer acTimer = new Timer();
            acTimer.schedule(new TimerTask() {
                public void run() {
                    if(time % 2 != 0){
                        float r = (float)color[0] / 255;
                        float g = (float)color[1] / 255;
                        float b = (float)color[2] / 255;
                        mRobot.setLed(r, g, b);
                        Log.i("Color","r: "+r+ " g:"+g+" b"+b);
                    }else {
                        float r = (float) color[0] / 255 * (float) 1 / time;
                        float g = (float) color[1] / 255 * (float) 1 / time;
                        float b = (float) color[2] / 255 * (float) 1 / time;
                        mRobot.setLed(r, g, b);
                        Log.i("Color","r: "+r+ " g:"+g+" b"+b);
                    }


                }
            }, delay + delay * time);
    }
}
