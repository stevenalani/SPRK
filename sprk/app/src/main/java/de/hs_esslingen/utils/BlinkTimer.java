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
    private static int time = 1;
    private static List<Timer> timer = new ArrayList<>();
    public BlinkTimer(final int times, final int delay, final ConvenienceRobot mRobot, final int[] color) {
        while(time <= times) {
            Timer acTimer = new Timer();
            acTimer.schedule(new TimerTask() {
                public void run() {
                    float r = (float)color[0] / 255 * (float)1 / time;
                    float g = (float)color[1] / 255 * (float)1 / time;
                    float b = (float)color[2] / 255 * (float)1 / time;
                    mRobot.setLed(r, g, b);
                    Log.i("Color","r: "+r+ " g:"+g+" b"+b);
                }
            }, delay + delay * time);
            timer.add(acTimer);
            time++;
        }
        timer = new ArrayList<>();
        time = 0;
    }
}
