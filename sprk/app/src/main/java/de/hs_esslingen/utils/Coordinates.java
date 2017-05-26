package de.hs_esslingen.utils;

import java.sql.Timestamp;

/**
 * Created by Steven on 24.05.2017.
 */

public class Coordinates{
    public Timestamp updated;
    public Coordinates(float x,float y) {
        this.x = x;
        this.y = y;
        updated = new Timestamp(System.currentTimeMillis());
        //Log.i("TestDrive",updated.toString());
    }
    public float x;
    public float y;

    @Override
    public String toString() {
        return "Coordinates{" +
                "updated=" + updated +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
